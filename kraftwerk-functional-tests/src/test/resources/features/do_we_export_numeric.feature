Feature: Do we save number format correctly ?
  Everybody wants to know if we save them correctly

  Scenario Outline: Do we export large numbers in right format (without E)
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service
    Then Step 7 : We check that id "<SurveyUnitId>" has value "<ExpectedValue>" for variable "<VariableName>" in table "<TableName>"
    Examples:
      |Directory                        |SurveyUnitId |ExpectedValue  |VariableName |TableName  |
      |SAMPLETEST-DATAONLY-v1           |0000005      |19800340       |T_NPIECES    |RACINE     |

