package fr.insee.kraftwerk.core.sequence;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.data.model.VariableModel;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.extradata.paradata.Paradata;
import fr.insee.kraftwerk.core.extradata.paradata.ParadataParser;
import fr.insee.kraftwerk.core.rawdata.GroupData;
import fr.insee.kraftwerk.core.rawdata.GroupInstance;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlExecute;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
public class BuildBindingsSequenceGenesis {

	VtlExecute vtlExecute;
	FileUtilsInterface fileUtilsInterface;
	KraftwerkExecutionContext kraftwerkExecutionContext;

	public BuildBindingsSequenceGenesis(FileUtilsInterface fileUtilsInterface,
										KraftwerkExecutionContext kraftwerkExecutionContext
	) {
		vtlExecute = new VtlExecute(fileUtilsInterface, kraftwerkExecutionContext);
		this.fileUtilsInterface = fileUtilsInterface;
		this.kraftwerkExecutionContext = kraftwerkExecutionContext;
	}

	public void buildVtlBindings(String dataMode, VtlBindings vtlBindings, Map<String, MetadataModel> metadataModels, List<SurveyUnitUpdateLatest> surveyUnits, Path specsDirectory) throws KraftwerkException {
		SurveyRawData data = new SurveyRawData();

		/* Step 2.0 : Read the DDI file (and Lunatic Json for missing variables) to get survey variables */
		data.setMetadataModel(metadataModels.get(dataMode));

		/* Step 2.1 : Fill the data object with the survey answers file */
		//TODO To be deported in another place in the code later at refactor step
		List<SurveyUnitUpdateLatest> surveyUnitsFiltered = surveyUnits.stream().filter(surveyUnit -> dataMode.equals(surveyUnit.getMode().getModeName())).toList();
		for(SurveyUnitUpdateLatest surveyUnit : surveyUnitsFiltered) {
			QuestionnaireData questionnaire = new QuestionnaireData();
			questionnaire.setIdentifier(surveyUnit.getInterrogationId());
			data.getIdSurveyUnits().add(surveyUnit.getInterrogationId());

			GroupInstance answers = questionnaire.getAnswers();

			// Add surveyUnit/response variables to answers
			answers.putValue(Constants.SURVEY_UNIT_IDENTIFIER_NAME, surveyUnit.getUsualSurveyUnitId());
			answers.putValue(Constants.VALIDATION_DATE_NAME, surveyUnit.getValidationDate() != null ?
					surveyUnit.getValidationDate().format(DateTimeFormatter.ofPattern(Constants.VALIDATION_DATE_FORMAT))
					: null
			);
			answers.putValue(Constants.QUESTIONNAIRE_STATE_NAME, surveyUnit.getQuestionnaireState());

			// Add collected/external variables of the surveyUnit/response
			addVariablesToGroupInstance(surveyUnit.getCollectedVariables(), answers, data, questionnaire);
			addVariablesToGroupInstance(surveyUnit.getExternalVariables(), answers, data, questionnaire);

			// Add variables states for absent variables
			if(kraftwerkExecutionContext.isAddStates()){
				fillNullVariableStates(answers, data.getMetadataModel());
			}

			data.getQuestionnaires().add(questionnaire);
		}

		/* Step 2.2 : Get paradata for the survey */
		parseParadata(dataMode, data, specsDirectory, fileUtilsInterface);

		/* Step 2.3 : Convert data object to a VTL Dataset */
		data.setDataMode(dataMode);
		vtlExecute.convertToVtlDataset(data, dataMode, vtlBindings);
	}

	private void addVariablesToGroupInstance(List<VariableModel> variables,
											 GroupInstance groupInstance, //Answers from questionnaireData
											 SurveyRawData data,
											 QuestionnaireData questionnaire
	) {
		for (VariableModel variable : variables) {
			if (variable.getScope().equals(Constants.ROOT_GROUP_NAME)) {
				groupInstance.putValue(variable.getVarId(), variable.getValue());
				if (kraftwerkExecutionContext.isAddStates()){
					groupInstance.putValue(
							variable.getVarId() + Constants.VARIABLE_STATE_SUFFIX_NAME,
							getVariableStateString(variable)
					);
				}
				continue;
			}
			addGroupVariables(data.getMetadataModel(), variable, questionnaire.getAnswers());
		}
	}

	private void addGroupVariables(MetadataModel metadataModel,
								   VariableModel variableModel,
								   GroupInstance groupInstance
	) {
		String variableName = variableModel.getVarId();
		if (metadataModel.getVariables().hasVariable(variableName)) {
			String groupName = metadataModel.getVariables().getVariable(variableName).getGroupName();
			GroupData groupData = groupInstance.getSubGroup(groupName);
			groupData.putValue(
					variableModel.getValue(),
					variableName,
					variableModel.getIteration() - 1
			);
			if (kraftwerkExecutionContext.isAddStates()){
				groupData.putValue(
						getVariableStateString(variableModel),
						variableName + Constants.VARIABLE_STATE_SUFFIX_NAME,
						variableModel.getIteration() - 1
				);
			}
		}
	}

	/**
	 * Defines what to write in the variable state field
	 */
	private String getVariableStateString(VariableModel variableModel) {
		return variableModel.getState() == null ? ""
				: variableModel.getState().toString();
	}

	/**
	 * TO USE ONLY IF ADDSTATES IS TRUE
	 * Goes through all variables of a metadataModel to add an empty string to the variable state field
	 * of SurveyUnitUpdateLatest variables
	 */
	private void fillNullVariableStates(GroupInstance rootGroupInstance, MetadataModel metadataModel) {
		fillNullVariableStatesForGroupInstance(rootGroupInstance, metadataModel);
		for(String metadataModelSubGroupName : metadataModel.getSubGroupNames()){
			//Will create one instance if not exists
			rootGroupInstance.getSubGroup(metadataModelSubGroupName).getInstance(0);
			for(GroupInstance subGroupInstance : rootGroupInstance.getSubGroup(metadataModelSubGroupName).getInstances()) {
				fillNullVariableStatesForGroupInstance(
						subGroupInstance,
						metadataModel
				);
			}
		}
	}

	private void fillNullVariableStatesForGroupInstance(GroupInstance groupInstance,
														MetadataModel metadataModel) {
		//Iterate through variables present in metadataModel for the group but not in groupInstance
		for(Map.Entry<String, Variable> absentVariableEntry :
				metadataModel.getVariables().getVariables().entrySet().stream().filter(
						variableEntry ->
								!groupInstance.getVariableNames().contains(variableEntry.getKey())
								&& groupInstance.getGroupName().equals(variableEntry.getValue().getGroupName())
				).toList()) {
			String absentVariableName = absentVariableEntry.getKey();
			groupInstance.putValue(
					absentVariableName + Constants.VARIABLE_STATE_SUFFIX_NAME,
					""
			);
		}
	}

	private void parseParadata(String dataMode, SurveyRawData data, Path specsDirectory, FileUtilsInterface fileUtilsInterface) throws NullException {
		Path paraDataPath = specsDirectory.resolve(dataMode+Constants.PARADATA_FOLDER);
		if (fileUtilsInterface.isFileExists(paraDataPath.toString())) {
			ParadataParser paraDataParser = new ParadataParser(fileUtilsInterface);
			Paradata paraData = new Paradata(paraDataPath);
			paraDataParser.parseParadata(paraData, data);
		}
	}

}
