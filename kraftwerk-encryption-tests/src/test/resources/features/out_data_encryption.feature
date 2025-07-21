Feature: Output Data Encryption

  Scenario Outline: Do we encrypt data if asked in main service
    Given Step 0 : We have some survey in directory "<Directory>"
    And We want to encrypt output data at the end of process
    When Step 1 : We launch main service
    Then We should not be able to read the csv output file
    And We should not be able to read the parquet output file
    And We should be able to decrypt the file

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
    Then We should not be able to read the csv output file
    And We should not be able to read the parquet output file
    And We should be able to decrypt the file

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
    Then We should not be able to read the csv output file
    And We should not be able to read the parquet output file
    And We should be able to decrypt the file (Genesis)

    Examples:
      | CampaignId     |  InterrogationId | VariableName | Value |
      | TEST-TABLEAUX  |  AUTO11000       | TABLEAU2A11  | 4     |
