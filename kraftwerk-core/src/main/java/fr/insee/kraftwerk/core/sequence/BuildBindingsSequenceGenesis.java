package fr.insee.kraftwerk.core.sequence;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.data.model.ExternalVariable;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.data.model.VariableState;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.extradata.paradata.Paradata;
import fr.insee.kraftwerk.core.extradata.paradata.ParadataParser;
import fr.insee.kraftwerk.core.extradata.reportingdata.CSVReportingDataParser;
import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingData;
import fr.insee.kraftwerk.core.extradata.reportingdata.XMLReportingDataParser;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.core.rawdata.GroupData;
import fr.insee.kraftwerk.core.rawdata.GroupInstance;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlExecute;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class BuildBindingsSequenceGenesis {

	VtlExecute vtlExecute;
	FileUtilsInterface fileUtilsInterface;

	public BuildBindingsSequenceGenesis(FileUtilsInterface fileUtilsInterface) {
		vtlExecute = new VtlExecute(fileUtilsInterface);
		this.fileUtilsInterface = fileUtilsInterface;
	}

	public void buildVtlBindings(String dataMode, VtlBindings vtlBindings, Map<String, MetadataModel> metadataModels, List<SurveyUnitUpdateLatest> surveyUnits, Path inDirectory) throws KraftwerkException {
		SurveyRawData data = new SurveyRawData();

		/* Step 2.0 : Read the DDI file (and Lunatic Json for missing variables) to get survey variables */
		data.setMetadataModel(metadataModels.get(dataMode));

		/* Step 2.1 : Fill the data object with the survey answers file */
		// To be deported in another place in the code later at refactor step
		List<SurveyUnitUpdateLatest> surveyUnitsFiltered = surveyUnits.stream().filter(surveyUnit -> dataMode.equals(surveyUnit.getMode().getModeName())).toList();
		for(SurveyUnitUpdateLatest surveyUnit : surveyUnitsFiltered) {
			QuestionnaireData questionnaire = new QuestionnaireData();
			questionnaire.setIdentifier(surveyUnit.getIdUE());
			data.getIdSurveyUnits().add(surveyUnit.getIdUE());

			GroupInstance answers = questionnaire.getAnswers();
			for (VariableState variableState : surveyUnit.getVariablesUpdate()){
				if (variableState.getIdLoop().equals(Constants.ROOT_GROUP_NAME)){
					// Not clean : deal with arrays (for now always a single value in array)
					if (!variableState.getValues().isEmpty()){
						answers.putValue(variableState.getIdVar(), variableState.getValues().getFirst());
					}
				} else {
					addGroupVariables(data.getMetadataModel(), variableState.getIdVar(), questionnaire.getAnswers(), variableState);
				}
			}

			for (ExternalVariable extVar : surveyUnit.getExternalVariables()){
				// The external are always in root group name
				if (!extVar.getValues().isEmpty()){
					answers.putValue(extVar.getIdVar(), extVar.getValues().getFirst());
				}
			}

			data.getQuestionnaires().add(questionnaire);
		}

		/* Step 2.2 : Get paradata for the survey */
		parseParadata(dataMode, data, inDirectory, fileUtilsInterface);

		/* Step 2.3 : Get reportingData for the survey */
		parseReportingData(dataMode, data, inDirectory, fileUtilsInterface);

		/* Step 2.4a : Convert data object to a VTL Dataset */
		data.setDataMode(dataMode);
		vtlExecute.convertToVtlDataset(data, dataMode, vtlBindings);
	}

	private void parseParadata(String dataMode, SurveyRawData data, Path inDirectory, FileUtilsInterface fileUtilsInterface) throws NullException {
		Path paraDataPath = inDirectory.resolve(dataMode+Constants.PARADATA_FOLDER);
		if (fileUtilsInterface.isFileExists(paraDataPath.toString())) {
			ParadataParser paraDataParser = new ParadataParser(fileUtilsInterface);
			Paradata paraData = new Paradata(paraDataPath);
			paraDataParser.parseParadata(paraData, data);
		}
	}

	private void parseReportingData(String dataMode, SurveyRawData data, Path inDirectory, FileUtilsInterface fileUtilsInterface) throws KraftwerkException {
		Path reportingDataFile = inDirectory.resolve(dataMode+Constants.REPORTING_DATA_FOLDER);
		if (fileUtilsInterface.isFileExists(reportingDataFile.toString())) {
			List<String> listFiles = fileUtilsInterface.listFileNames(reportingDataFile.toString());
			for (String file : listFiles) {
				ReportingData reportingData = new ReportingData(reportingDataFile.resolve(file));
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

	private void addGroupVariables(MetadataModel models, String variableName, GroupInstance answers, VariableState variableState) {
		if (models.getVariables().hasVariable(variableName)) {
			String groupName = models.getVariables().getVariable(variableName).getGroupName();
			GroupData groupData = answers.getSubGroup(groupName);
			groupData.putValue(variableState.getValues().getFirst(), variableName, variableState.getIdLoop());
		}
	}

}
