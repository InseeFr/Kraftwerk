package fr.insee.kraftwerk.extradata.reportingdata;

import fr.insee.kraftwerk.Constants;
import fr.insee.kraftwerk.metadata.Variable;
import fr.insee.kraftwerk.metadata.VariableType;
import fr.insee.kraftwerk.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.rawdata.SurveyRawData;

public abstract class ReportingDataParser {
	
	
	protected void integrateReportingDataIntoUE(SurveyRawData surveyRawData, ReportingData reportingData) {
		for (int k = 1; k <= max(reportingData); k++) {
			Variable variableListStates = new Variable(
					Constants.STATE_SUFFIX_NAME + "_" + k, surveyRawData.getVariablesMap().getRootGroup(), VariableType.STRING);
			surveyRawData.getVariablesMap().putVariable(variableListStates);

			for (int i = 0; i < reportingData.getListReportingDataUE().size(); i++) {

				ReportingDataUE reportingDataUE = reportingData.getListReportingDataUE().get(i);
				QuestionnaireData questionnaire = null;
				questionnaire = surveyRawData.getQuestionnaires().stream()
						.filter(questionnaireToSearch -> reportingDataUE.getIdentifier()
								.equals(questionnaireToSearch.getIdentifier()))
						.findAny().orElse(null);

				if (reportingDataUE.getStates().size() > k - 1 && questionnaire != null) {
					questionnaire.getAnswers().putValue(variableListStates.getName(),
							StateType.getStateType(reportingDataUE.getStates().get(k - 1).getStateType()));
				}
			}
		}
		Variable variableLastState = new Variable(
				Constants.LAST_STATE_NAME, surveyRawData.getVariablesMap().getRootGroup(), VariableType.STRING);
		surveyRawData.getVariablesMap().putVariable(variableLastState);

		for (int i = 0; i < reportingData.getListReportingDataUE().size(); i++) {

			ReportingDataUE reportingDataUE = reportingData.getListReportingDataUE().get(i);
			QuestionnaireData questionnaire = null;
			if (reportingDataUE.getStates().size() > 0) {
				questionnaire = surveyRawData.getQuestionnaires().stream()
						.filter(questionnaireToSearch -> reportingDataUE.getIdentifier()
								.equals(questionnaireToSearch.getIdentifier()))
						.findAny().orElse(null);
				if (questionnaire != null){
					questionnaire.getAnswers().putValue(variableLastState.getName(), StateType.getStateType(
							reportingDataUE.getStates().get(reportingDataUE.getStates().size() - 1).getStateType()));
				}
			}
		}
	}
	
	public int max(ReportingData reportingData) {
		return reportingData.getListReportingDataUE().stream().mapToInt(ue -> ue.getStates().size()).max().getAsInt();
	}

}
