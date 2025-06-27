Feature: Do we save correctly loop data from Genesis?

  Scenario Outline: Genesis loop collected data export
    Given Step 0 : We have some survey in directory "<CampaignId>"
    And We have a collected variable "<VariableName>" in a loop named "<LoopName>" iteration <Iteration> in a document with CampaignId "<CampaignId>", InterrogationId "<InterrogationId>" with value "<Value>"
    When We use the Genesis service with campaignId "<CampaignId>"
    Then We check if the CSV format is correct
    And In csv loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration <Iteration> we should have value "<Value>" for field "<VariableName>"
    And In parquet loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration <Iteration> we should have value "<Value>" for field "<VariableName>"
    And Step 2 : We check "<LoopName>" output file has <ExpectedFileLinesCount> lines and <ExpectedVariablesCount> variables
    Examples:
      | CampaignId    | InterrogationId | VariableName | Value | LoopName | Iteration | ExpectedFileLinesCount | ExpectedVariablesCount |
      | TEST-TABLEAUX | AUTO11000       | TABESA2      | 66    | TABESA   | 6         | 2                      | 5                      |
      | TEST-GLOB     | AUTO203         | TABOFATS4    | 54    | TABOFATS | 1         | 2                      | 14                     |
      | TEST-GLOB     | AUTO204         | TABOFATS2    | act2  | TABOFATS | 1         | 2                      | 14                     |
      | TEST-GLOB     | AUTO204         | TABOFATS2    | act3  | TABOFATS | 2         | 2                      | 14                     |

  Scenario Outline: Genesis loop external data export
    Given Step 0 : We have some survey in directory "<CampaignId>"
    And We have a external variable "<VariableName>" in a loop named "<LoopName>" iteration <Iteration> in a document with CampaignId "<CampaignId>", InterrogationId "<InterrogationId>" with value "<Value>"
    When We use the Genesis service with campaignId "<CampaignId>"
    Then We check if the CSV format is correct
    And In csv loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration <Iteration> we should have value "<Value>" for field "<VariableName>"
    And In parquet loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration <Iteration> we should have value "<Value>" for field "<VariableName>"
    And Step 2 : We check "<LoopName>" output file has <ExpectedFileLinesCount> lines and <ExpectedVariablesCount> variables
    Examples:
      | CampaignId     |  InterrogationId | VariableName | Value | LoopName | Iteration | ExpectedFileLinesCount | ExpectedVariablesCount |
      | TEST-TABLEAUX  |  AUTO11000       | TABESA2      | 66    | TABESA   | 6         | 2                      | 5                      |
      | TEST-GLOB      |  AUTO203         | TABOFATS4    | 54    | TABOFATS | 1         | 2                      | 14                     |
      | TEST-GLOB      |  AUTO204         | TABOFATS2    | act2  | TABOFATS | 1         | 2                      | 14                     |
      | TEST-GLOB      |  AUTO204         | TABOFATS2    | act3  | TABOFATS | 2         | 2                      | 14                     |

  Scenario Outline: GenesisV2 loop collected data export
    Given Step 0 : We have some survey in directory "<CampaignId>"
    And We have a collected variable "<VariableName>" in a loop named "<LoopName>" iteration <Iteration> in a document with CampaignId "<CampaignId>", InterrogationId "<InterrogationId>" with value "<Value>"
    When We use the GenesisV2 service with campaignId "<CampaignId>"
    Then We check if the CSV format is correct
    And In csv loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration <Iteration> we should have value "<Value>" for field "<VariableName>"
    And In parquet loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration <Iteration> we should have value "<Value>" for field "<VariableName>"
    And Step 2 : We check "<LoopName>" output file has <ExpectedFileLinesCount> lines and <ExpectedVariablesCount> variables
    Examples:
      | CampaignId    | InterrogationId | VariableName | Value | LoopName | Iteration | ExpectedFileLinesCount | ExpectedVariablesCount |
      | TEST-TABLEAUX | AUTO11000       | TABESA2      | 66    | TABESA   | 6         | 2                      | 5                      |
      | TEST-GLOB     | AUTO203         | TABOFATS4    | 54    | TABOFATS | 1         | 2                      | 14                     |
      | TEST-GLOB     | AUTO204         | TABOFATS2    | act2  | TABOFATS | 1         | 2                      | 14                     |
      | TEST-GLOB     | AUTO204         | TABOFATS2    | act3  | TABOFATS | 2         | 2                      | 14                     |

  Scenario Outline: GenesisV2 loop external data export
    Given Step 0 : We have some survey in directory "<CampaignId>"
    And We have a external variable "<VariableName>" in a loop named "<LoopName>" iteration <Iteration> in a document with CampaignId "<CampaignId>", InterrogationId "<InterrogationId>" with value "<Value>"
    When We use the GenesisV2 service with campaignId "<CampaignId>"
    Then We check if the CSV format is correct
    And In csv loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration <Iteration> we should have value "<Value>" for field "<VariableName>"
    And In parquet loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration <Iteration> we should have value "<Value>" for field "<VariableName>"
    And Step 2 : We check "<LoopName>" output file has <ExpectedFileLinesCount> lines and <ExpectedVariablesCount> variables
    Examples:
      | CampaignId     |  InterrogationId | VariableName | Value | LoopName | Iteration | ExpectedFileLinesCount | ExpectedVariablesCount |
      | TEST-TABLEAUX  |  AUTO11000       | TABESA2      | 66    | TABESA   | 6         | 2                      | 5                      |
      | TEST-GLOB      |  AUTO203         | TABOFATS4    | 54    | TABOFATS | 1         | 2                      | 14                     |
      | TEST-GLOB      |  AUTO204         | TABOFATS2    | act2  | TABOFATS | 1         | 2                      | 14                     |
      | TEST-GLOB      |  AUTO204         | TABOFATS2    | act3  | TABOFATS | 2         | 2                      | 14                     |
