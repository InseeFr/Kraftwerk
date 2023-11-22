Feature: Do we apply all standard vtl scripts ?
  Everybody wants to know if we execute them correctly


  Scenario Outline: Do we apply standard script
    Given Step 0 : We have some survey in directory "<Directory>"
    And We have test standard vtl scripts
    When Step 1 : We launch main service
    Then We should have a field named "<ColumnName>" in the root output file of "<Directory>" filled with "<ExpectedContent>"
    Examples:
      | Directory               | ColumnName            | ExpectedContent     |
      | SAMPLETEST-DATAONLY-v1  | TESTVTLFAF            | TESTFAF             |
      | SAMPLETEST-DATAONLY-v1  | TESTVTLMULTIMODE      | TESTMULTIMODE       |
      | SAMPLETEST-DATAONLY-v1  | TESTVTLINFOLEVEL      | TESTINFOLEVEL       |

  Scenario Outline: Do we apply standard script without creating _keep tables
    Given Step 0 : We have some survey in directory "<Directory>"
    And We have test standard vtl scripts
    When Step 1 : We launch main service
    Then we shouldn't have any _keep file in "<Directory>" output directory
    Examples:
      | Directory |
      | SAMPLETEST-DATAONLY-v1  |