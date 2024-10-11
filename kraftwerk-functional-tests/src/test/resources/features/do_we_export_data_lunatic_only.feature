Feature: Do we save correctly all data without DDI ?
  Everybody wants to know if we save them correctly

  Scenario Outline: Do we create CSV root data file with the right structure
    Given Step 0 : We have some survey in directory "<Directory>"
    When We launch lunatic only service
    Then Step 2 : We check root output file has <ExpectedLineCount> lines and <ExpectedDataFieldCount> variables
    Then We check if the CSV format is correct

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - ExpectedLineCount : Expected row quantity
    # - ExpectedDataFieldCount : Expected field quantity

      |Directory                        |ExpectedLineCount     |ExpectedDataFieldCount            |
      #HAD TO CHANGE MANUALLY THE SPECIFICATIONS BECAUSE IT DID NOT RESPECT THE MODEL LUNATIC---> FIND ANOTHER QUESTIONNAIRE
      #|SAMPLETEST-LUNATICONLY           |6                     |222                               |