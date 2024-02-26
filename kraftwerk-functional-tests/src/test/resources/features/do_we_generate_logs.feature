Feature: Do we save correctly the logs of a execution
  Everybody wants to know if we save them correctly

  Scenario Outline: Do we create the log file
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service
    Then We should have a log file named in directory "<Directory>"

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - OutputFileName : Name of reporting data file (with .csv extension)
    |Directory                        |
    |SAMPLETEST-REPORTINGDATA-v1      |