Feature: Do we save correctly all data from Genesis?

  Scenario Outline: Genesis Data export
    Given Step 0 : We have some survey in directory "<CampaignId>"
    And We have a collected variable "<VariableName>" in a document with CampaignId "<CampaignId>", InterrogationId "<InterrogationId>" with value "<Value>"
    And We have a external variable "<VariableName>" in a document with CampaignId "<CampaignId>", InterrogationId "<InterrogationId>" with value "<Value>"
    When We use the Genesis service with campaignId "<CampaignId>"
    Then We check if the CSV format is correct
    And In root csv output file we should have "<Value>" for survey unit "<InterrogationId>", column "<VariableName>"
    And In root parquet output file we should have "<Value>" for survey unit "<InterrogationId>", column "<VariableName>"
    And Step 2 : We check root output file has 2 lines and 152 variables
    Examples:
      | CampaignId     |  InterrogationId | VariableName | Value |
      | TEST-TABLEAUX  |  AUTO11000       | TABLEAU2A11  | 4     |

  Scenario Outline: Genesis Data export by questionnaire
    Given Step 0 : We have some survey in directory "<QuestionnaireModelId>"
    And We have a collected variable "<VariableName>" in a document with QuestionnaireModelId "<QuestionnaireModelId>", InterrogationId "<InterrogationId>" with value "<Value>"
    And We have a external variable "<VariableName>" in a document with QuestionnaireModelId "<QuestionnaireModelId>", InterrogationId "<InterrogationId>" with value "<Value>"
    When We use the Genesis service with questionnaireModelId "<QuestionnaireModelId>"
    Then We check if the CSV format is correct
    And In root csv output file we should have "<Value>" for survey unit "<InterrogationId>", column "<VariableName>"
    And In root parquet output file we should have "<Value>" for survey unit "<InterrogationId>", column "<VariableName>"
    And Step 2 : We check root output file has 2 lines and 152 variables
    Examples:
      | QuestionnaireModelId     |  InterrogationId | VariableName | Value |
      | TEST-TABLEAUX            |  AUTO11000       | TABLEAU2A11  | 4     |