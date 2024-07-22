Feature: Do we apply correctly vtl script ?
  Everybody wants to know if we apply them correctly

  Scenario Outline: Do we create variable correctly ?
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service
    Then In a file named "<FileName>" there should be a "<VariableName>" field

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - ExpectedLineCount : Expected row quantity (header included)
    # - ExpectedDataFieldCount : Expected field quantity

      |Directory                        |FileName                           |VariableName              |
      |SAMPLETEST-VTL                   |SAMPLETEST-VTL_RACINE.csv          |FAF_VAR                   |
      |SAMPLETEST-VTL                   |SAMPLETEST-VTL_RACINE.csv          |RECONCILIATION_VAR        |
      |SAMPLETEST-VTL                   |SAMPLETEST-VTL_RACINE.csv          |TRANSFORMATION_VAR        |
      |SAMPLETEST-VTL                   |SAMPLETEST-VTL_RACINE.csv          |INFOLEVEL_VAR             |

  Scenario Outline: Do we remove root variable correctly ?
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service
    Then In a file named "<FileName>" there shouldn't be a "<UnexpectedVariableName>" field

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - ExpectedLineCount : Expected row quantity (header included)
    # - ExpectedDataFieldCount : Expected field quantity

      |Directory                        |FileName                           |UnexpectedVariableName |
      |SAMPLETEST-VTL                   |SAMPLETEST-VTL_RACINE.csv          |xAxis                  |


