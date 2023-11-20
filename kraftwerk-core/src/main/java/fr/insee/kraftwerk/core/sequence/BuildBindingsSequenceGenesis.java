package fr.insee.kraftwerk.core.sequence;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.data.model.ExternalVariable;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.data.model.VariableState;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.rawdata.GroupData;
import fr.insee.kraftwerk.core.rawdata.GroupInstance;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlExecute;

import java.util.List;
import java.util.Map;

public class BuildBindingsSequenceGenesis {

	VtlExecute vtlExecute;

	public BuildBindingsSequenceGenesis() {
		vtlExecute = new VtlExecute();
	}

	public void buildVtlBindings(String dataMode, VtlBindings vtlBindings, Map<String, VariablesMap> metadataVariables, List<SurveyUnitUpdateLatest> surveyUnits) {
		SurveyRawData data = new SurveyRawData();

		/* Step 2.0 : Read the DDI file (and Lunatic Json for missing variables) to get survey variables */
		data.setVariablesMap(metadataVariables.get(dataMode));

		/* Step 2.1 : Fill the data object with the survey answers file */
		// To be deported in another place in the code later at refactor step
		List<SurveyUnitUpdateLatest> surveyUnitsFiltered = surveyUnits.stream().filter(surveyUnit -> surveyUnit.getMode().getModeName().equals(dataMode)).toList();
		for(SurveyUnitUpdateLatest surveyUnit : surveyUnitsFiltered) {
			QuestionnaireData questionnaire = new QuestionnaireData();
			questionnaire.setIdentifier(surveyUnit.getIdUE());
			data.getIdSurveyUnits().add(surveyUnit.getIdUE());

			GroupInstance answers = questionnaire.getAnswers();
			for (VariableState variableState : surveyUnit.getVariablesUpdate()){
				if (variableState.getIdLoop().equals(Constants.ROOT_GROUP_NAME)){
					// Not clean : deal with arrays (for now always a single value in array)
					if (!variableState.getValues().isEmpty()) answers.putValue(variableState.getIdVar(), variableState.getValues().get(0));
				} else {
					addGroupVariables(data.getVariablesMap(), variableState.getIdVar(), questionnaire.getAnswers(), variableState);
				}
			}

			for (ExternalVariable extVar : surveyUnit.getExternalVariables()){
				// The external are always in root group name
				if (!extVar.getValues().isEmpty()) answers.putValue(extVar.getIdVar(), extVar.getValues().get(0));
			}

			data.getQuestionnaires().add(questionnaire);
		}

//		/* Step 2.2 : Get paradata for the survey */
//		parseParadata(modeInputs, data);
//
//		/* Step 2.3 : Get reportingData for the survey */
//		parseReportingData(modeInputs, data);

		/* Step 2.4a : Convert data object to a VTL Dataset */
		data.setDataMode(dataMode);
		vtlExecute.convertToVtlDataset(data, dataMode, vtlBindings);
	}

//	private void parseParadata(ModeInputs modeInputs, SurveyRawData data) throws NullException {
//		Path paraDataFolder = modeInputs.getParadataFolder();
//		if (paraDataFolder != null) {
//			ParadataParser paraDataParser = new ParadataParser();
//			Paradata paraData = new Paradata(paraDataFolder);
//			paraDataParser.parseParadata(paraData, data);
//		}
//	}
//
//	private void parseReportingData(ModeInputs modeInputs, SurveyRawData data) throws NullException {
//		Path reportingDataFile = modeInputs.getReportingDataFile();
//		if (reportingDataFile != null) {
//			ReportingData reportingData = new ReportingData(reportingDataFile);
//			if (reportingDataFile.toString().contains(".xml")) {
//				XMLReportingDataParser xMLReportingDataParser = new XMLReportingDataParser();
//				xMLReportingDataParser.parseReportingData(reportingData, data, withAllReportingData);
//
//			} else if (reportingDataFile.toString().contains(".csv")) {
//				CSVReportingDataParser cSVReportingDataParser = new CSVReportingDataParser();
//				cSVReportingDataParser.parseReportingData(reportingData, data, withAllReportingData);
//			}
//		}
//	}

	private void addGroupVariables(VariablesMap variables, String variableName, GroupInstance answers, VariableState variableState) {
		if (variables.hasVariable(variableName)) {
			String groupName = variables.getVariable(variableName).getGroupName();
			GroupData groupData = answers.getSubGroup(groupName);
			groupData.putValue(variableState.getValues().get(0), variableName, variableState.getIdLoop());
		}
	}

}
