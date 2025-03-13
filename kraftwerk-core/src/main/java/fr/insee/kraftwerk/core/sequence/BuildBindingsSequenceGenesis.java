package fr.insee.kraftwerk.core.sequence;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.data.model.VariableModel;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.extradata.paradata.Paradata;
import fr.insee.kraftwerk.core.extradata.paradata.ParadataParser;
import fr.insee.kraftwerk.core.extradata.reportingdata.CSVReportingDataParser;
import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingData;
import fr.insee.kraftwerk.core.extradata.reportingdata.XMLReportingDataParser;
import fr.insee.kraftwerk.core.rawdata.GroupData;
import fr.insee.kraftwerk.core.rawdata.GroupInstance;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlExecute;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class BuildBindingsSequenceGenesis {

	VtlExecute vtlExecute;
	FileUtilsInterface fileUtilsInterface;

	public BuildBindingsSequenceGenesis(FileUtilsInterface fileUtilsInterface) {
		vtlExecute = new VtlExecute(fileUtilsInterface);
		this.fileUtilsInterface = fileUtilsInterface;
	}

	public void buildVtlBindings(String dataMode, VtlBindings vtlBindings, Map<String, MetadataModel> metadataModels, List<SurveyUnitUpdateLatest> surveyUnits, Path specsDirectory) throws KraftwerkException {
		SurveyRawData data = new SurveyRawData();

		/* Step 2.0 : Read the DDI file (and Lunatic Json for missing variables) to get survey variables */
		data.setMetadataModel(metadataModels.get(dataMode));

		/* Step 2.1 : Fill the data object with the survey answers file */
		// To be deported in another place in the code later at refactor step
		List<SurveyUnitUpdateLatest> surveyUnitsFiltered = surveyUnits.stream().filter(surveyUnit -> dataMode.equals(surveyUnit.getMode().getModeName())).toList();
		for(SurveyUnitUpdateLatest surveyUnit : surveyUnitsFiltered) {
			QuestionnaireData questionnaire = new QuestionnaireData();
			questionnaire.setIdentifier(surveyUnit.getInterrogationId());
			data.getIdSurveyUnits().add(surveyUnit.getInterrogationId());

			GroupInstance answers = questionnaire.getAnswers();

			addVariablesToGroupInstance(surveyUnit.getCollectedVariables(), answers, data, questionnaire);
			addVariablesToGroupInstance(surveyUnit.getExternalVariables(), answers, data, questionnaire);

			data.getQuestionnaires().add(questionnaire);
		}

		/* Step 2.2 : Get paradata for the survey */
		parseParadata(dataMode, data, specsDirectory, fileUtilsInterface);

		/* Step 2.3 : Get reportingData for the survey */
		parseReportingData(dataMode, data, specsDirectory, fileUtilsInterface);

		/* Step 2.4a : Convert data object to a VTL Dataset */
		data.setDataMode(dataMode);
		vtlExecute.convertToVtlDataset(data, dataMode, vtlBindings);
	}

	private void addVariablesToGroupInstance(List<VariableModel> surveyUnit, GroupInstance answers, SurveyRawData data, QuestionnaireData questionnaire) {
		for (VariableModel collectedVariables : surveyUnit) {
			if (collectedVariables.getScope().equals(Constants.ROOT_GROUP_NAME)) {
				answers.putValue(collectedVariables.getVarId(), collectedVariables.getValue());
			} else {
				addGroupVariables(data.getMetadataModel(), collectedVariables.getVarId(), questionnaire.getAnswers(), collectedVariables);
			}
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

	private void parseReportingData(String dataMode, SurveyRawData data, Path inDirectory, FileUtilsInterface fileUtilsInterface) throws KraftwerkException {
		Path reportingDataFile = inDirectory.resolve(dataMode+Constants.REPORTING_DATA_FOLDER);
		log.info("Try to read reporting data from {}", reportingDataFile.toString());
		if (fileUtilsInterface.isFileExists(reportingDataFile.toString())) {
			List<String> listFiles = fileUtilsInterface.listFileNames(reportingDataFile.toString());
			for (String file : listFiles) {
				ReportingData reportingData = new ReportingData(reportingDataFile.resolve(file), new ArrayList<>());
				if (file.contains(".xml")) {
					XMLReportingDataParser xMLReportingDataParser = new XMLReportingDataParser(fileUtilsInterface);
					xMLReportingDataParser.parseReportingData(reportingData, data, true);

				} else if (file.contains(".csv")) {
					CSVReportingDataParser cSVReportingDataParser = new CSVReportingDataParser(fileUtilsInterface);
					cSVReportingDataParser.parseReportingData(reportingData, data, true);
				}
			}
		}
	}

	private void addGroupVariables(MetadataModel models, String variableName, GroupInstance answers, VariableModel variableModel) {
		if (models.getVariables().hasVariable(variableName)) {
			String groupName = models.getVariables().getVariable(variableName).getGroupName();
			GroupData groupData = answers.getSubGroup(groupName);
			groupData.putValue(variableModel.getValue(), variableName, variableModel.getIteration() - 1);
		}
	}

}
