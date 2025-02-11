Feature: Do we save correctly all data from Genesis?

  Scenario Outline: Genesis Collected Data export (csv)
    Given Step 0 : We have some survey in directory "<CampaignId>"
    Given We have a collected variable "<VariableName>" in a document with CampaignId "<CampaignId>", InterrogationId "<InterrogationId>" with value "<Value>"
    When We use the Genesis service with campaignId "<CampaignId>"
    Then We check if the CSV format is correct
    Then In root csv output file we should have "<Value>" for survey unit "<InterrogationId>", column "<VariableName>"
    Examples:
      | CampaignId     |  InterrogationId | VariableName | Value |
      | TEST-TABLEAUX  |  AUTO11000       | TABLEAU2A11  | 4     |


  Scenario Outline: Genesis Collected Data export (parquet)
    Given Step 0 : We have some survey in directory "<CampaignId>"
    Given We have a collected variable "<VariableName>" in a document with CampaignId "<CampaignId>", InterrogationId "<InterrogationId>" with value "<Value>"
    When We use the Genesis service with campaignId "<CampaignId>"
    Then In root parquet output file we should have "<Value>" for survey unit "<InterrogationId>", column "<VariableName>"
    Examples:
      | CampaignId      |  InterrogationId | VariableName | Value |
      | TEST-TABLEAUX   |  AUTO11000       | TABLEAU2A11  | 4     |

  Scenario Outline: Genesis external Data export (csv)
    Given Step 0 : We have some survey in directory "<CampaignId>"
    Given We have a external variable "<VariableName>" in a document with CampaignId "<CampaignId>", InterrogationId "<InterrogationId>" with value "<Value>"
    When We use the Genesis service with campaignId "<CampaignId>"
    Then We check if the CSV format is correct
    Then In root csv output file we should have "<Value>" for survey unit "<InterrogationId>", column "<VariableName>"
    Examples:
      | CampaignId     |  InterrogationId | VariableName | Value |
      | TEST-TABLEAUX  |  AUTO11000       | TABLEAU2A11  | 4     |


  Scenario Outline: Genesis external Data export (parquet)
    Given Step 0 : We have some survey in directory "<CampaignId>"
    Given We have a external variable "<VariableName>" in a document with CampaignId "<CampaignId>", InterrogationId "<InterrogationId>" with value "<Value>"
    When We use the Genesis service with campaignId "<CampaignId>"
    Then In root parquet output file we should have "<Value>" for survey unit "<InterrogationId>", column "<VariableName>"
    Examples:
      | CampaignId      |  InterrogationId | VariableName | Value |
      | TEST-TABLEAUX   |  AUTO11000       | TABLEAU2A11  | 4     |