package fr.insee.kraftwerk.core.extradata.reportingdata;


import java.sql.Date;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.Group;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.DateUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class ReportingDataParser {

	private int maxStates = 0;
	private int maxAttempts = 0;
	Group reportingGroup ;

	protected void integrateReportingDataIntoUE(SurveyRawData surveyRawData, ReportingData reportingData, boolean withAllReportingData) {
		this.maxStates = countMaxStates(reportingData);
		this.maxAttempts = countMaxAttempts(reportingData);
		reportingGroup = new Group(Constants.REPORTING_DATA_GROUP_NAME, Constants.ROOT_GROUP_NAME);
		surveyRawData.getVariablesMap().putGroup(reportingGroup);
		createReportingVariables(surveyRawData);
		addReportingValues(surveyRawData, reportingData, withAllReportingData);
	}

	private void createReportingVariables(SurveyRawData surveyRawData) {
		Variable variableInterviewer = new Variable(Constants.INTERVIEWER_ID_NAME,
				surveyRawData.getVariablesMap().getReportingDataGroup(), VariableType.STRING, "20");
		surveyRawData.getVariablesMap().putVariable(variableInterviewer);
		Variable variableOrganization = new Variable(Constants.ORGANIZATION_UNIT_ID_NAME,
				surveyRawData.getVariablesMap().getReportingDataGroup(), VariableType.STRING, "50");
		surveyRawData.getVariablesMap().putVariable(variableOrganization);
		surveyRawData.getVariablesMap().putVariable(new Variable(Constants.ADRESS_RGES_NAME,
				surveyRawData.getVariablesMap().getReportingDataGroup(), VariableType.STRING, "2"));
		surveyRawData.getVariablesMap().putVariable(new Variable(Constants.ADRESS_NUMFA_NAME,
				surveyRawData.getVariablesMap().getReportingDataGroup(), VariableType.STRING, "6"));
		surveyRawData.getVariablesMap().putVariable(new Variable(Constants.ADRESS_SSECH_NAME,
				surveyRawData.getVariablesMap().getReportingDataGroup(), VariableType.STRING, "2"));
		surveyRawData.getVariablesMap().putVariable(new Variable(Constants.ADRESS_LE_NAME,
				surveyRawData.getVariablesMap().getReportingDataGroup(), VariableType.STRING, "1"));
		surveyRawData.getVariablesMap().putVariable(new Variable(Constants.ADRESS_EC_NAME,
				surveyRawData.getVariablesMap().getReportingDataGroup(), VariableType.STRING, "1"));
		surveyRawData.getVariablesMap().putVariable(new Variable(Constants.ADRESS_BS_NAME,
				surveyRawData.getVariablesMap().getReportingDataGroup(), VariableType.STRING, "1"));
		surveyRawData.getVariablesMap().putVariable(new Variable(Constants.ADRESS_NOI_NAME,
				surveyRawData.getVariablesMap().getReportingDataGroup(), VariableType.STRING, "2"));
		surveyRawData.getVariablesMap().putVariable(new Variable(Constants.ADRESS_ID_STAT_INSEE,
				surveyRawData.getVariablesMap().getReportingDataGroup(), VariableType.STRING, "15"));
		for (int k = 1; k <= this.maxStates; k++) {
			Variable variableListStates = new Variable(Constants.STATE_SUFFIX_NAME + "_" + k,
					surveyRawData.getVariablesMap().getReportingDataGroup(), VariableType.STRING, "50");
			surveyRawData.getVariablesMap().putVariable(variableListStates);
			Variable variableListStatesDates = new Variable(Constants.STATE_SUFFIX_NAME + "_" + k + "_DATE",
					surveyRawData.getVariablesMap().getReportingDataGroup(), VariableType.DATE, "50");
			surveyRawData.getVariablesMap().putVariable(variableListStatesDates);
		}
		Variable variableLastState = new Variable(Constants.LAST_STATE_NAME,
				surveyRawData.getVariablesMap().getReportingDataGroup(), VariableType.STRING, "50");
		surveyRawData.getVariablesMap().putVariable(variableLastState);
		surveyRawData.getVariablesMap().putVariable(new Variable(Constants.OUTCOME_NAME,
				surveyRawData.getVariablesMap().getReportingDataGroup(), VariableType.STRING, "50"));
		surveyRawData.getVariablesMap().putVariable(new Variable(Constants.OUTCOME_DATE,
				surveyRawData.getVariablesMap().getReportingDataGroup(), VariableType.DATE, "50"));
				surveyRawData.getVariablesMap().putVariable(new Variable(Constants.NUMBER_ATTEMPTS_NAME,
				surveyRawData.getVariablesMap().getReportingDataGroup(), VariableType.STRING, "4"));
		surveyRawData.getVariablesMap().putVariable(new Variable(Constants.LAST_ATTEMPT_DATE,
				surveyRawData.getVariablesMap().getReportingDataGroup(),  VariableType.DATE, "50"));
		for (int i = 1; i <= this.maxAttempts; i++) {
			Variable variableListAttempts = new Variable(Constants.OUTCOME_ATTEMPT_SUFFIX_NAME + "_" + i,
					reportingGroup, VariableType.STRING, "50");
			surveyRawData.getVariablesMap().putVariable(variableListAttempts);
			Variable variableListAttemptsDates = new Variable(Constants.OUTCOME_ATTEMPT_SUFFIX_NAME + "_" + i + "_DATE",
					reportingGroup, VariableType.DATE, "50");
			surveyRawData.getVariablesMap().putVariable(variableListAttemptsDates);
		}
		Variable variableIdentification = new Variable(Constants.IDENTIFICATION_NAME,
				surveyRawData.getVariablesMap().getReportingDataGroup(), VariableType.STRING, "50");
		surveyRawData.getVariablesMap().putVariable(variableIdentification);
		Variable variableAccess = new Variable(Constants.ACCESS_NAME,
				surveyRawData.getVariablesMap().getReportingDataGroup(), VariableType.STRING, "50");
		surveyRawData.getVariablesMap().putVariable(variableAccess);
		Variable variableSituation = new Variable(Constants.SITUATION_NAME,
				surveyRawData.getVariablesMap().getReportingDataGroup(), VariableType.STRING, "50");
		surveyRawData.getVariablesMap().putVariable(variableSituation);
		Variable variableCategory = new Variable(Constants.CATEGORY_NAME,
				surveyRawData.getVariablesMap().getReportingDataGroup(), VariableType.STRING, "50");
		surveyRawData.getVariablesMap().putVariable(variableCategory);
		Variable variableOccupant = new Variable(Constants.OCCUPANT_NAME,
				surveyRawData.getVariablesMap().getReportingDataGroup(), VariableType.STRING, "50");
		surveyRawData.getVariablesMap().putVariable(variableOccupant);
	}

	private void addReportingValues(SurveyRawData surveyRawData, ReportingData reportingData, boolean withAllReportingData) {
		for (int i = 0; i < reportingData.getListReportingDataUE().size(); i++) {
			ReportingDataUE reportingDataUE = reportingData.getListReportingDataUE().get(i);
			QuestionnaireData questionnaire =  surveyRawData.getQuestionnaires().stream().filter(questionnaireToSearch -> reportingDataUE
					.getIdentifier().equals(questionnaireToSearch.getIdentifier())).findAny().orElse(null);
			if (questionnaire == null && !withAllReportingData) {
				return ;
			}
			addReportingDataUEToQuestionnaire(surveyRawData, reportingDataUE, questionnaire);
		}
	}

	private void addReportingDataUEToQuestionnaire(SurveyRawData surveyRawData, ReportingDataUE reportingDataUE,
			QuestionnaireData questionnaire) {
		if (questionnaire == null) {
			questionnaire = new QuestionnaireData();
			questionnaire.setIdentifier(reportingDataUE.getIdentifier());
			surveyRawData.addQuestionnaire(questionnaire);
			log.info("Missing questionnaire for reporting data: {}.", reportingDataUE.getIdentifier() );
		}
		if (reportingDataUE.getInterviewerId() != null)
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.INTERVIEWER_ID_NAME, reportingDataUE.getInterviewerId());
		if (reportingDataUE.getOrganizationUnitId() != null)
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.ORGANIZATION_UNIT_ID_NAME,
					reportingDataUE.getOrganizationUnitId());
		if (reportingDataUE.getInseeSampleIdentifier() != null) {
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.ADRESS_RGES_NAME, reportingDataUE.getInseeSampleIdentifier().getRges());
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.ADRESS_NUMFA_NAME, reportingDataUE.getInseeSampleIdentifier().getNumfa());
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.ADRESS_SSECH_NAME, reportingDataUE.getInseeSampleIdentifier().getSsech());
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.ADRESS_LE_NAME, reportingDataUE.getInseeSampleIdentifier().getLe());
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.ADRESS_EC_NAME, reportingDataUE.getInseeSampleIdentifier().getEc());
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.ADRESS_BS_NAME, reportingDataUE.getInseeSampleIdentifier().getBs());
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.ADRESS_NOI_NAME, reportingDataUE.getInseeSampleIdentifier().getNoi());
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.ADRESS_ID_STAT_INSEE, reportingDataUE.getInseeSampleIdentifier().getIdStatInsee());
		}
		if (!reportingDataUE.getStates().isEmpty()) {
			addStates(reportingDataUE, questionnaire);
		}
		if (reportingDataUE.getContactOutcome() != null) {
			addContactOutcome(reportingDataUE, questionnaire);
		}
		if (!reportingDataUE.getContactAttempts().isEmpty()) {
			addContactAttempts(reportingDataUE, questionnaire);
		}
		if (reportingDataUE.getIdentification() != null){
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.IDENTIFICATION_NAME, reportingDataUE.getIdentification());
		}
		if (reportingDataUE.getAccess() != null){
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.ACCESS_NAME, reportingDataUE.getAccess());
		}
		if (reportingDataUE.getSituation() != null){
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.SITUATION_NAME, reportingDataUE.getSituation());
		}
		if (reportingDataUE.getCategory() != null){
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.CATEGORY_NAME, reportingDataUE.getCategory());
		}
		if (reportingDataUE.getOccupant() != null){
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.OCCUPANT_NAME, reportingDataUE.getOccupant());
		}
	}

	private void addContactAttempts(ReportingDataUE reportingDataUE, QuestionnaireData questionnaire) {
		for (int k = 1; k <= reportingDataUE.getContactAttempts().size(); k++) {
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.OUTCOME_ATTEMPT_SUFFIX_NAME + "_" + k,
					ContactAttemptType.getAttemptType(reportingDataUE.getContactAttempts().get(k - 1).getStatus()));
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.OUTCOME_ATTEMPT_SUFFIX_NAME + "_" + k + "_DATE",
					DateUtils.formatDateToString(reportingDataUE.getContactAttempts().get(k - 1).getDate()));
		}
		questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.LAST_ATTEMPT_DATE,
				DateUtils.formatDateToString(getLastContactAttempt(reportingDataUE).getDate()));
	}

	private void addContactOutcome(ReportingDataUE reportingDataUE, QuestionnaireData questionnaire) {
		ContactOutcome contactOutcome = reportingDataUE.getContactOutcome();
		questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.OUTCOME_NAME, contactOutcome.getOutcomeType());
		if (contactOutcome.getDateEndContact() != 0L) {
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.OUTCOME_DATE, DateUtils.formatDateToString(new Date(contactOutcome.getDateEndContact())));
		}
		questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.NUMBER_ATTEMPTS_NAME,
					Integer.toString(contactOutcome.getTotalNumberOfContactAttempts()));
	}

	private void addStates(ReportingDataUE reportingDataUE, QuestionnaireData questionnaire) {
		for (int k = 1; k <= reportingDataUE.size(); k++) {
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.STATE_SUFFIX_NAME + "_" + k,
					StateType.getStateType((reportingDataUE.getStates().get(k - 1)).getStateType()));
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.STATE_SUFFIX_NAME + "_" + k + "_DATE",
					DateUtils.formatDateToString(new Date((reportingDataUE.getStates().get(k - 1)).getTimestamp())));
		}
		questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.LAST_STATE_NAME,
				StateType.getStateType(
						(reportingDataUE.getStates().get(reportingDataUE.getStates().size() - 1))
								.getStateType()));
	}

	private ContactAttempt getLastContactAttempt(ReportingDataUE reportingDataUE) {
		return reportingDataUE.getContactAttempts().get(reportingDataUE.getContactAttempts().size()-1);
	}

	public int countMaxStates(ReportingData reportingData) {
		return reportingData.getListReportingDataUE().stream().mapToInt(ue -> ue.getStates().size()).max().getAsInt();
	}

	public int countMaxAttempts(ReportingData reportingData) {
		return reportingData.getListReportingDataUE().stream().mapToInt(ue -> ue.getContactAttempts().size()).max()
				.getAsInt();
	}
}
