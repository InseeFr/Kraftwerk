Feature: Output Data Encryption

  Scenario Outline: Do we encrypt data if asked in main service
    Given Step 0 : We have some survey in directory "<Directory>"
    And We want to encrypt output data at the end of process
    When Step 1 : We launch main service
    And We archive and encrypt the outputs
    Then We should not be able to read the output zip without decryption
    And We should be able to decrypt the zip and read the csv inside

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns

      |Directory                        |
      |SAMPLETEST-DATAONLY-v1           |
      |SAMPLETEST-PAPERDATA-v1          |

  Scenario Outline: Do we encrypt data if asked in file by file service
    Given Step 0 : We have some survey in directory "<Directory>"
    And We want to encrypt output data at the end of process
    When Step 1 : We launch main service file by file
    And We archive and encrypt the outputs
    Then We should not be able to read the output zip without decryption
    And We should be able to decrypt the zip and read the csv inside

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns

      |Directory                        |
      |SAMPLETEST-DATAONLY-v1           |
      |SAMPLETEST-PAPERDATA-v1          |

  Scenario Outline: Do we encrypt data if asked in genesis service
    Given Step 0 : We have some survey in directory "<CampaignId>"
    And We have a collected variable "<VariableName>" in a document with CampaignId "<CampaignId>", InterrogationId "<InterrogationId>" with value "<Value>"
    And We have a external variable "<VariableName>" in a document with CampaignId "<CampaignId>", InterrogationId "<InterrogationId>" with value "<Value>"
    And We want to encrypt output data at the end of genesis process
    When We use the Genesis service with campaignId "<CampaignId>"
    And We archive and encrypt the outputs
    Then We should not be able to read the output zip without decryption
    And We should be able to decrypt the file (Genesis)

    Examples:
      | CampaignId     |  InterrogationId | VariableName | Value |
      | TEST-TABLEAUX  |  AUTO11000       | TABLEAU2A11  | 4     |

  Scenario Outline: Do we encrypt data if asked in genesis service by questionnaire
    Given Step 0 : We have some survey in directory "<QuestionnaireModelId>"
    And We have a collected variable "<VariableName>" in a document with QuestionnaireModelId "<QuestionnaireModelId>", InterrogationId "<InterrogationId>" with value "<Value>"
    And We have a external variable "<VariableName>" in a document with QuestionnaireModelId "<QuestionnaireModelId>", InterrogationId "<InterrogationId>" with value "<Value>"
    And We want to encrypt output data at the end of genesis process
    When We use the Genesis service with questionnaireModelId "<QuestionnaireModelId>"
    And We archive and encrypt the outputs
    Then We should not be able to read the output zip without decryption
    And We should be able to decrypt the file (Genesis)

    Examples:
      | QuestionnaireModelId     |  InterrogationId | VariableName | Value |
      | TEST-TABLEAUX            |  AUTO11000       | TABLEAU2A11  | 4     |