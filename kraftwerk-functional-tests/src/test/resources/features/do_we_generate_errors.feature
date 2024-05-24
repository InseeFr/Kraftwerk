Feature: Do we save correctly the VTL errors of a execution
  Everybody wants to know if we save them correctly

  Scenario Outline: Do we create the error file
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We initialize with input file "<InputFile>"
    When Step 1 : We launch main service
    Then We should have a error file in directory "<Directory>"

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - OutputFileName : Name of reporting data file (with .csv extension)
      |Directory                        |InputFile      |
      |SAMPLETEST-ERROR                 |kraftwerk.json |

  Scenario Outline: Do we create the log file in each directory
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We initialize with input file "<InputFile>"
    When We launch main service 2 times
    Then We should have error files for each execution in directory "<Directory>"

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - OutputFileName : Name of reporting data file (with .csv extension)
      |Directory                        |InputFile      |
      |SAMPLETEST-ERROR                 |kraftwerk.json |