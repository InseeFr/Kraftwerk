package fr.insee.kraftwerk.core.extradata.reportingdata;

import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import java.sql.Date;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ReportingDataParser {
  private static final Logger log = LoggerFactory.getLogger(ReportingDataParser.class);
  
  public int maxStates = 0;
  
  public int maxAttempts = 0;
  
  protected void integrateReportingDataIntoUE(SurveyRawData surveyRawData, ReportingData reportingData) {
    this.maxStates = maxStates(reportingData);
    this.maxAttempts = maxAttempts(reportingData);
    createReportingVariables(surveyRawData, reportingData);
    addReportingValues(surveyRawData, reportingData);
  }
  
  private void createReportingVariables(SurveyRawData surveyRawData, ReportingData reportingData) {
    Variable variableInterviewer = new Variable("IDENQ", 
        surveyRawData.getVariablesMap().getRootGroup(), VariableType.STRING, "20");
    surveyRawData.getVariablesMap().putVariable(variableInterviewer);
    Variable variableOrganization = new Variable("ORGANIZATION_UNIT_ID", 
        surveyRawData.getVariablesMap().getRootGroup(), VariableType.STRING, "50");
    surveyRawData.getVariablesMap().putVariable(variableOrganization);
    surveyRawData.getVariablesMap().putVariable(new Variable("RGES", 
          surveyRawData.getVariablesMap().getRootGroup(), VariableType.STRING, "2"));
    surveyRawData.getVariablesMap().putVariable(new Variable("NUMFA", 
          surveyRawData.getVariablesMap().getRootGroup(), VariableType.STRING, "6"));
    surveyRawData.getVariablesMap().putVariable(new Variable("SSECH", 
          surveyRawData.getVariablesMap().getRootGroup(), VariableType.STRING, "2"));
    surveyRawData.getVariablesMap().putVariable(new Variable("LE", 
          surveyRawData.getVariablesMap().getRootGroup(), VariableType.STRING, "1"));
    surveyRawData.getVariablesMap().putVariable(new Variable("EC", 
          surveyRawData.getVariablesMap().getRootGroup(), VariableType.STRING, "1"));
    surveyRawData.getVariablesMap().putVariable(new Variable("BS", 
          surveyRawData.getVariablesMap().getRootGroup(), VariableType.STRING, "1"));
    surveyRawData.getVariablesMap().putVariable(new Variable("NOI", 
          surveyRawData.getVariablesMap().getRootGroup(), VariableType.STRING, "2"));
    for (int k = 1; k <= this.maxStates; k++) {
      Variable variableListStates = new Variable("STATE_" + k, 
          surveyRawData.getVariablesMap().getRootGroup(), VariableType.STRING, "50");
      surveyRawData.getVariablesMap().putVariable(variableListStates);
    } 
    Variable variableLastState = new Variable("LAST_STATE", 
        surveyRawData.getVariablesMap().getRootGroup(), VariableType.STRING, "50");
    surveyRawData.getVariablesMap().putVariable(variableLastState);
    surveyRawData.getVariablesMap().putVariable(new Variable("OUTCOME", 
          surveyRawData.getVariablesMap().getRootGroup(), VariableType.STRING, "50"));
    if (!surveyRawData.getVariablesMap().hasVariable("JOURENQ"))
      surveyRawData.getVariablesMap().putVariable(new Variable("JOURENQ", 
            surveyRawData.getVariablesMap().getRootGroup(), VariableType.STRING, "2")); 
    if (!surveyRawData.getVariablesMap().hasVariable("MOISENQ"))
      surveyRawData.getVariablesMap().putVariable(new Variable("MOISENQ", 
            surveyRawData.getVariablesMap().getRootGroup(), VariableType.STRING, "2")); 
    if (!surveyRawData.getVariablesMap().hasVariable("ANNEENQ"))
      surveyRawData.getVariablesMap().putVariable(new Variable("ANNEENQ", 
            surveyRawData.getVariablesMap().getRootGroup(), VariableType.STRING, "4")); 
    surveyRawData.getVariablesMap().putVariable(new Variable("NUMBER_CONTACT_ATTEMPTS", 
          surveyRawData.getVariablesMap().getRootGroup(), VariableType.STRING, "4"));
    for (int i = 1; i <= this.maxAttempts; i++) {
      Variable variableListAttempts = new Variable("ATTEMPT_" + i, 
          surveyRawData.getVariablesMap().getRootGroup(), VariableType.STRING, "50");
      surveyRawData.getVariablesMap().putVariable(variableListAttempts);
    } 
  }
  
  private void addReportingValues(SurveyRawData surveyRawData, ReportingData reportingData) {
    for (int i = 0; i < reportingData.getListReportingDataUE().size(); i++) {
      ReportingDataUE reportingDataUE = reportingData.getListReportingDataUE().get(i);
      QuestionnaireData questionnaire = null;
      questionnaire = surveyRawData.getQuestionnaires().stream().filter(questionnaireToSearch -> reportingDataUE.getIdentifier().equals(questionnaireToSearch.getIdentifier()))
        .findAny().orElse(null);
      if (questionnaire != null) {
        if (reportingDataUE.getInterviewerId() != null)
          questionnaire.getAnswers().putValue("IDENQ", 
              reportingDataUE.getInterviewerId()); 
        if (reportingDataUE.getOrganizationUnitId() != null)
          questionnaire.getAnswers().putValue("ORGANIZATION_UNIT_ID", 
              reportingDataUE.getOrganizationUnitId()); 
        if (reportingDataUE.getInseeSampleIdentiers() != null) {
          questionnaire.getAnswers().putValue("RGES", 
              String.format("%02d", new Object[] { Integer.valueOf(Integer.parseInt(reportingDataUE.getInseeSampleIdentiers().getRges())) }));
          questionnaire.getAnswers().putValue("NUMFA", 
              String.format("%06d", new Object[] { Integer.valueOf(Integer.parseInt(reportingDataUE.getInseeSampleIdentiers().getNumfa())) }));
          questionnaire.getAnswers().putValue("SSECH", 
              String.format("%02d", new Object[] { Integer.valueOf(Integer.parseInt(reportingDataUE.getInseeSampleIdentiers().getSsech())) }));
          questionnaire.getAnswers().putValue("LE", 
              reportingDataUE.getInseeSampleIdentiers().getLe());
          questionnaire.getAnswers().putValue("EC", 
              reportingDataUE.getInseeSampleIdentiers().getEc());
          questionnaire.getAnswers().putValue("BS", 
              reportingDataUE.getInseeSampleIdentiers().getBs());
          questionnaire.getAnswers().putValue("NOI", 
              String.format("%02d", new Object[] { Integer.valueOf(Integer.parseInt(reportingDataUE.getInseeSampleIdentiers().getNoi())) }));
        } 
        if (reportingDataUE.getStates().size() > 0) {
          for (int k = 1; k <= reportingDataUE.getStates().size(); k++)
            questionnaire.getAnswers().putValue("STATE_" + k, 
                StateType.getStateType(((State)reportingDataUE.getStates().get(k - 1)).getStateType())); 
          questionnaire.getAnswers().putValue("LAST_STATE", StateType.getStateType((
                (State)reportingDataUE.getStates().get(reportingDataUE.getStates().size() - 1)).getStateType()));
        } 
        if (reportingDataUE.getContactOutcome() != null) {
          questionnaire.getAnswers().putValue("OUTCOME", 
              reportingDataUE.getContactOutcome().getOutcomeType());
          if ((questionnaire.getAnswers().getValue("JOURENQ") == null || 
            questionnaire.getAnswers().getValue("MOISENQ") == null || 
            questionnaire.getAnswers().getValue("ANNEENQ") == null) && 
            reportingDataUE.getContactOutcome().getDateEndContact() != 0L) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(reportingDataUE.getContactOutcome().getDateEndContact()));
            questionnaire.getAnswers().putValue("JOURENQ", 
                Integer.toString(calendar.get(5)));
            questionnaire.getAnswers().putValue("MOISENQ", 
                String.format("%02d", new Object[] { Integer.valueOf(calendar.get(2) + 1) }));
            questionnaire.getAnswers().putValue("ANNEENQ", 
                Integer.toString(calendar.get(1)));
          } 
          if (reportingDataUE.getContactOutcome() != null)
            questionnaire.getAnswers().putValue("NUMBER_CONTACT_ATTEMPTS", 
                Integer.toString(reportingDataUE.getContactOutcome().getTotalNumberOfContactAttempts())); 
        } 
        if (reportingDataUE.getContactAttempts().size() > 0)
          for (int k = 1; k <= reportingDataUE.getContactAttempts().size(); k++)
            questionnaire.getAnswers().putValue("ATTEMPT_" + k, 
                
                ContactAttemptType.getAttemptType(((ContactAttempt)reportingDataUE.getContactAttempts().get(k - 1)).getStatus()));  
      } else {
        log.info(String.format("Missing questionnaire for reporting data: %s.", new Object[] { reportingDataUE.getIdentifier() }));
      } 
    } 
  }
  
  public int maxStates(ReportingData reportingData) {
    return reportingData.getListReportingDataUE().stream().mapToInt(ue -> ue.getStates().size()).max().getAsInt();
  }
  
  public int maxAttempts(ReportingData reportingData) {
    return reportingData.getListReportingDataUE().stream().mapToInt(ue -> ue.getContactAttempts().size()).max()
      .getAsInt();
  }
}
