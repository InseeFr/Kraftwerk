Feature: Do we save correctly loop data from Genesis?

  Scenario Outline: Genesis loop collected data export (csv)
    Given Step 0 : We have some survey in directory "<CampaignId>"
    Given We have a collected variable "<VariableName>" in a loop named "<LoopName>" iteration <Iteration> in a document with CampaignId "<CampaignId>", InterrogationId "<InterrogationId>" with value "<Value>"
    When We use the Genesis service with campaignId "<CampaignId>"
    Then We check if the CSV format is correct
    Then In csv loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration <Iteration> we should have value "<Value>" for field "<VariableName>"
    Examples:
      | CampaignId    |  InterrogationId | VariableName | Value | LoopName | Iteration |
      | TEST-TABLEAUX |  AUTO11000       | TABESA2      | 66    | TABESA   | 6         |
      | TEST-GLOB     |  AUTO203         | TABOFATS4    | 54    | TABOFATS | 1         |
      | TEST-GLOB     |  AUTO204         | TABOFATS2    | act2  | TABOFATS | 1         |
      | TEST-GLOB     |  AUTO204         | TABOFATS2    | act3  | TABOFATS | 2         |


  Scenario Outline: Genesis loop collected data export (parquet)
    Given Step 0 : We have some survey in directory "<CampaignId>"
    Given We have a collected variable "<VariableName>" in a loop named "<LoopName>" iteration <Iteration> in a document with CampaignId "<CampaignId>", InterrogationId "<InterrogationId>" with value "<Value>"
    When We use the Genesis service with campaignId "<CampaignId>"
    Then In parquet loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration <Iteration> we should have value "<Value>" for field "<VariableName>"
    Examples:
      | CampaignId     |  InterrogationId | VariableName | Value | LoopName | Iteration |
      | TEST-TABLEAUX  |  AUTO11000       | TABESA2      | 66    | TABESA   | 6         |
      | TEST-GLOB      |  AUTO203         | TABOFATS4    | 54    | TABOFATS | 1         |
      | TEST-GLOB      |  AUTO204         | TABOFATS2    | act2  | TABOFATS | 1         |
      | TEST-GLOB      |  AUTO204         | TABOFATS2    | act3  | TABOFATS | 2         |

  Scenario Outline: Genesis loop external data export (csv)
    Given Step 0 : We have some survey in directory "<CampaignId>"
    Given We have a external variable "<VariableName>" in a loop named "<LoopName>" iteration <Iteration> in a document with CampaignId "<CampaignId>", InterrogationId "<InterrogationId>" with value "<Value>"
    When We use the Genesis service with campaignId "<CampaignId>"
    Then We check if the CSV format is correct
    Then In csv loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration <Iteration> we should have value "<Value>" for field "<VariableName>"
    Examples:
      | CampaignId     |  InterrogationId | VariableName | Value | LoopName | Iteration |
      | TEST-TABLEAUX |  AUTO11000       | TABESA2       | 66    | TABESA   | 6         |
      | TEST-GLOB      |  AUTO203         | TABOFATS4    | 54    | TABOFATS | 1         |
      | TEST-GLOB      |  AUTO204         | TABOFATS2    | act2  | TABOFATS | 1         |
      | TEST-GLOB      |  AUTO204         | TABOFATS2    | act3  | TABOFATS | 2         |

  Scenario Outline: Genesis loop external data export (parquet)
    Given Step 0 : We have some survey in directory "<CampaignId>"
    Given We have a external variable "<VariableName>" in a loop named "<LoopName>" iteration <Iteration> in a document with CampaignId "<CampaignId>", InterrogationId "<InterrogationId>" with value "<Value>"
    When We use the Genesis service with campaignId "<CampaignId>"
    Then In parquet loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration <Iteration> we should have value "<Value>" for field "<VariableName>"
    Examples:
      | CampaignId     |  InterrogationId | VariableName | Value | LoopName | Iteration |
      | TEST-TABLEAUX |  AUTO11000        | TABESA2      | 66    | TABESA   | 6         |
      | TEST-GLOB      |  AUTO203         | TABOFATS4    | 54    | TABOFATS | 1         |
      | TEST-GLOB      |  AUTO204         | TABOFATS2    | act2  | TABOFATS | 1         |
      | TEST-GLOB      |  AUTO204         | TABOFATS2    | act3  | TABOFATS | 2         |