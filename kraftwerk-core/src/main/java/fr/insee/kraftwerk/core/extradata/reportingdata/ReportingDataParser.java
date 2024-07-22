package fr.insee.kraftwerk.core.extradata.reportingdata;


import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.Group;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.DateUtils;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.extern.log4j.Log4j2;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public abstract class ReportingDataParser {

	Group reportingGroup;
	private int maxStates = 0;
	private int maxAttempts = 0;
	private int maxComments = 0;
	protected FileUtilsInterface fileUtilsInterface;

	public ReportingDataParser(FileUtilsInterface fileUtilsInterface) {
		this.fileUtilsInterface = fileUtilsInterface;
	}

	protected void integrateReportingDataIntoUE(SurveyRawData surveyRawData, ReportingData reportingData,
												boolean withAllReportingData, FileUtilsInterface fileUtilsInterface) {
		this.fileUtilsInterface = fileUtilsInterface;
		this.maxStates = countMaxStates(reportingData);
		this.maxAttempts = countMaxAttempts(reportingData);
		this.maxComments = countMaxComments(reportingData);
		reportingGroup = new Group(Constants.REPORTING_DATA_GROUP_NAME, Constants.ROOT_GROUP_NAME);
		surveyRawData.getMetadataModel().putGroup(reportingGroup);
		createReportingVariables(surveyRawData);
		addReportingValues(surveyRawData, reportingData, withAllReportingData);
	}

	private void createReportingVariables(SurveyRawData surveyRawData) {
		Group reportingDataGroup = surveyRawData.getMetadataModel().getReportingDataGroup();

		Variable variableInterviewer = new Variable(Constants.INTERVIEWER_ID_NAME, reportingDataGroup,
				VariableType.STRING, "20");
		surveyRawData.putVariable(variableInterviewer);
		Variable variableOrganization = new Variable(Constants.ORGANIZATION_UNIT_ID_NAME, reportingDataGroup,
				VariableType.STRING, "50");
		surveyRawData.putVariable(variableOrganization);
		surveyRawData
				.putVariable(new Variable(Constants.ADRESS_RGES_NAME, reportingDataGroup, VariableType.STRING, "2"));
		surveyRawData
				.putVariable(new Variable(Constants.ADRESS_NUMFA_NAME, reportingDataGroup, VariableType.STRING, "6"));
		surveyRawData
				.putVariable(new Variable(Constants.ADRESS_SSECH_NAME, reportingDataGroup, VariableType.STRING, "2"));
		surveyRawData.putVariable(new Variable(Constants.ADRESS_LE_NAME, reportingDataGroup, VariableType.STRING, "1"));
		surveyRawData.putVariable(new Variable(Constants.ADRESS_EC_NAME, reportingDataGroup, VariableType.STRING, "1"));
		surveyRawData.putVariable(new Variable(Constants.ADRESS_BS_NAME, reportingDataGroup, VariableType.STRING, "1"));
		surveyRawData
				.putVariable(new Variable(Constants.ADRESS_NOI_NAME, reportingDataGroup, VariableType.STRING, "2"));
		surveyRawData.putVariable(
				new Variable(Constants.ADRESS_ID_STAT_INSEE, reportingDataGroup, VariableType.STRING, "15"));
		for (int k = 1; k <= this.maxStates; k++) {
			Variable variableListStates = new Variable(Constants.STATE_SUFFIX_NAME + "_" + k, reportingDataGroup,
					VariableType.STRING, "50");
			surveyRawData.putVariable(variableListStates);
			Variable variableListStatesDates = new Variable(Constants.STATE_SUFFIX_NAME + "_" + k + "_DATE",
					reportingDataGroup, VariableType.DATE, "50");
			surveyRawData.putVariable(variableListStatesDates);
		}
		Variable variableLastState = new Variable(Constants.LAST_STATE_NAME, reportingDataGroup, VariableType.STRING,
				"50");
		surveyRawData.putVariable(variableLastState);
		surveyRawData.putVariable(new Variable(Constants.OUTCOME_NAME, reportingDataGroup, VariableType.STRING, "50"));
		surveyRawData.putVariable(new Variable(Constants.OUTCOME_DATE, reportingDataGroup, VariableType.DATE, "50"));
		surveyRawData.putVariable(
				new Variable(Constants.NUMBER_ATTEMPTS_NAME, reportingDataGroup, VariableType.STRING, "4"));
		surveyRawData
				.putVariable(new Variable(Constants.LAST_ATTEMPT_DATE, reportingDataGroup, VariableType.DATE, "50"));
		for (int i = 1; i <= this.maxAttempts; i++) {
			Variable variableListAttempts = new Variable(Constants.OUTCOME_ATTEMPT_SUFFIX_NAME + "_" + i,
					reportingGroup, VariableType.STRING, "50");// TODO reportingDataGroup or reportingGroup ??
			surveyRawData.putVariable(variableListAttempts);
			Variable variableListAttemptsDates = new Variable(Constants.OUTCOME_ATTEMPT_SUFFIX_NAME + "_" + i + "_DATE",
					reportingGroup, VariableType.DATE, "50");
			surveyRawData.putVariable(variableListAttemptsDates);
		}
		Variable variableIdentification = new Variable(Constants.IDENTIFICATION_NAME, reportingDataGroup,
				VariableType.STRING, "50");
		surveyRawData.putVariable(variableIdentification);
		Variable variableAccess = new Variable(Constants.ACCESS_NAME, reportingDataGroup, VariableType.STRING, "50");
		surveyRawData.putVariable(variableAccess);
		Variable variableSituation = new Variable(Constants.SITUATION_NAME, reportingDataGroup, VariableType.STRING,
				"50");
		surveyRawData.putVariable(variableSituation);
		Variable variableCategory = new Variable(Constants.CATEGORY_NAME, reportingDataGroup, VariableType.STRING,
				"50");
		surveyRawData.putVariable(variableCategory);
		Variable variableOccupant = new Variable(Constants.OCCUPANT_NAME, reportingDataGroup, VariableType.STRING,
				"50");
		surveyRawData.putVariable(variableOccupant);
		Variable variableOutcomeSpotting = new Variable(Constants.OUTCOME_SPOTTING, reportingDataGroup, VariableType.STRING,
				"50");
		surveyRawData.putVariable(variableOutcomeSpotting);
		for (int i = 1; i <= this.maxComments; i++) {
			Variable variableListCommentTypes = new Variable(Constants.COMMENT_PREFIX_NAME + "_TYPE_" + i,
					reportingGroup, VariableType.STRING, "50");
			surveyRawData.putVariable(variableListCommentTypes);
			Variable variableListComments = new Variable(Constants.COMMENT_PREFIX_NAME + "_" + i, reportingGroup,
					VariableType.STRING, "50");
			surveyRawData.putVariable(variableListComments);
		}

		Variable variableSurveyValidationDate = new Variable(Constants.REPORTING_DATA_SURVEY_VALIDATION_NAME,
				surveyRawData.getMetadataModel().getReportingDataGroup(), VariableType.DATE, "50");
		surveyRawData.getMetadataModel().getVariables().putVariable(variableSurveyValidationDate);
	}

	private void addReportingValues(SurveyRawData surveyRawData, ReportingData reportingData,
			boolean withAllReportingData) {
		List<String> missingQuestionnaireIds = new ArrayList<>();
		for (int i = 0; i < reportingData.getListReportingDataUE().size(); i++) {
			ReportingDataUE reportingDataUE = reportingData.getListReportingDataUE().get(i);
			QuestionnaireData questionnaire = surveyRawData.getQuestionnaires().stream()
					.filter(questionnaireToSearch -> reportingDataUE.getIdentifier()
							.equals(questionnaireToSearch.getIdentifier()))
					.findAny().orElse(null);
			if (questionnaire == null && !withAllReportingData) {
				return;
			}
			addReportingDataUEToQuestionnaire(surveyRawData, reportingDataUE, questionnaire, missingQuestionnaireIds);
		}
		// We log the lists of missing questionnaires on one line only
		if (!missingQuestionnaireIds.isEmpty()){
			StringBuilder missingQuestionnaireIdsString=new StringBuilder();
			for (String missingQuestionnaireId : missingQuestionnaireIds) {
				missingQuestionnaireIdsString.append(missingQuestionnaireId).append(" ");
			}
			log.info("Missing questionnaire for reporting data: {}.", missingQuestionnaireIdsString.toString());
		}
	}

	private void addReportingDataUEToQuestionnaire(SurveyRawData surveyRawData, ReportingDataUE reportingDataUE,
			QuestionnaireData questionnaire, List<String> missingQuestionnaireIds) {
		if (questionnaire == null) {
			questionnaire = new QuestionnaireData();
			questionnaire.setIdentifier(reportingDataUE.getIdentifier());
			surveyRawData.addQuestionnaire(questionnaire);
			missingQuestionnaireIds.add(reportingDataUE.getIdentifier());
		}
		// TODO Find another way than Constants.REPORTING_DATA_PREFIX_NAME +
		// reportingDataUE.getIdentifier() to fill the identifier field
		if (reportingDataUE.getInterviewerId() != null)
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.INTERVIEWER_ID_NAME, reportingDataUE.getInterviewerId());
		if (reportingDataUE.getOrganizationUnitId() != null)
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.ORGANIZATION_UNIT_ID_NAME, reportingDataUE.getOrganizationUnitId());
		if (reportingDataUE.getInseeSampleIdentifier() != null) {
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.ADRESS_RGES_NAME, reportingDataUE.getInseeSampleIdentifier().getRges());
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.ADRESS_NUMFA_NAME, reportingDataUE.getInseeSampleIdentifier().getNumfa());
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.ADRESS_SSECH_NAME, reportingDataUE.getInseeSampleIdentifier().getSsech());
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.ADRESS_LE_NAME, reportingDataUE.getInseeSampleIdentifier().getLe());
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.ADRESS_EC_NAME, reportingDataUE.getInseeSampleIdentifier().getEc());
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.ADRESS_BS_NAME, reportingDataUE.getInseeSampleIdentifier().getBs());
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.ADRESS_NOI_NAME, reportingDataUE.getInseeSampleIdentifier().getNoi());
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.ADRESS_ID_STAT_INSEE,
							reportingDataUE.getInseeSampleIdentifier().getIdStatInsee());
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
		if (reportingDataUE.getIdentification() != null) {
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.IDENTIFICATION_NAME, reportingDataUE.getIdentification().getIdentification());
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.ACCESS_NAME, reportingDataUE.getIdentification().getAccess());
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.SITUATION_NAME, reportingDataUE.getIdentification().getSituation());
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.CATEGORY_NAME, reportingDataUE.getIdentification().getCategory());
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.OCCUPANT_NAME, reportingDataUE.getIdentification().getOccupant());
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.OUTCOME_SPOTTING, reportingDataUE.getIdentification().getOutcomeSpotting());
		}
		if (!reportingDataUE.getComments().isEmpty()) {
			addComments(reportingDataUE, questionnaire);
		}
		if(reportingDataUE.getSurveyValidationDateTimeStamp() != null){
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier()).putValue(Constants.REPORTING_DATA_SURVEY_VALIDATION_NAME,
					DateUtils.formatLongToString(reportingDataUE.getSurveyValidationDateTimeStamp()));
		}
	}

	private void addContactAttempts(ReportingDataUE reportingDataUE, QuestionnaireData questionnaire) {
		for (int k = 1; k <= reportingDataUE.getContactAttempts().size(); k++) {
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.OUTCOME_ATTEMPT_SUFFIX_NAME + "_" + k, ContactAttemptType
							.getAttemptType(reportingDataUE.getContactAttempts().get(k - 1).getStatus()));
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.OUTCOME_ATTEMPT_SUFFIX_NAME + "_" + k + "_DATE",
							DateUtils.formatDateToString(reportingDataUE.getContactAttempts().get(k - 1).getDate()));
		}
		questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
				.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
				.putValue(Constants.LAST_ATTEMPT_DATE,
						DateUtils.formatDateToString(getLastContactAttempt(reportingDataUE).getDate()));
	}

	private void addContactOutcome(ReportingDataUE reportingDataUE, QuestionnaireData questionnaire) {
		ContactOutcome contactOutcome = reportingDataUE.getContactOutcome();
		questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
				.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
				.putValue(Constants.OUTCOME_NAME, contactOutcome.getOutcomeType());
		if (contactOutcome.getDateEndContact() != 0L) {
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.OUTCOME_DATE,
							DateUtils.formatDateToString(new Date(contactOutcome.getDateEndContact())));
		}
		questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
				.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
				.putValue(Constants.NUMBER_ATTEMPTS_NAME,
						Integer.toString(contactOutcome.getTotalNumberOfContactAttempts()));
	}

	private void addStates(ReportingDataUE reportingDataUE, QuestionnaireData questionnaire) {
		for (int k = 1; k <= reportingDataUE.size(); k++) {
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.STATE_SUFFIX_NAME + "_" + k,
							StateType.getStateType((reportingDataUE.getStates().get(k - 1)).getStateType()));
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.STATE_SUFFIX_NAME + "_" + k + "_DATE", DateUtils
							.formatDateToString(new Date((reportingDataUE.getStates().get(k - 1)).getTimestamp())));
		}
		questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
				.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
				.putValue(Constants.LAST_STATE_NAME, StateType.getStateType(
						(reportingDataUE.getStates().getLast()).getStateType()));
	}

	private void addComments(ReportingDataUE reportingDataUE, QuestionnaireData questionnaire) {
		for (int k = 1; k <= reportingDataUE.getComments().size(); k++) {
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.COMMENT_PREFIX_NAME + "_TYPE_" + k,
							reportingDataUE.getComments().get(k - 1).getType());
			questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
					.getInstance(Constants.REPORTING_DATA_PREFIX_NAME + reportingDataUE.getIdentifier())
					.putValue(Constants.COMMENT_PREFIX_NAME + "_" + k,
							reportingDataUE.getComments().get(k - 1).getValue());
		}
	}

	private ContactAttempt getLastContactAttempt(ReportingDataUE reportingDataUE) {
		return reportingDataUE.getContactAttempts().getLast();
	}

	public int countMaxStates(ReportingData reportingData) {
		if(reportingData.getListReportingDataUE().isEmpty()){
			return 0;
		}
		return reportingData.getListReportingDataUE().stream().mapToInt(ue -> ue.getStates().size()).max().getAsInt();
	}

	public int countMaxAttempts(ReportingData reportingData) {
		if(reportingData.getListReportingDataUE().isEmpty()){
			return 0;
		}
		return reportingData.getListReportingDataUE().stream().mapToInt(ue -> ue.getContactAttempts().size()).max()
				.getAsInt();
	}

	public int countMaxComments(ReportingData reportingData) {
		if(reportingData.getListReportingDataUE().isEmpty()){
			return 0;
		}
		return reportingData.getListReportingDataUE().stream().mapToInt(ue -> ue.getComments().size()).max().getAsInt();
	}
}
