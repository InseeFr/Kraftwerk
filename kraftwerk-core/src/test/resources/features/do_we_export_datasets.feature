Feature: Do we save correctly the datasets we got ?
  Everybody wants to know if we save them correctly

  Scenario Outline: Do we export correctly
    Given We have some SurveyRawData named "<nameDataset>"
    When I try to export the dataset named "<nameDataset>"
    When I try to import the dataset named "<nameDataset>"
    Then I should get some dataset values from "<nameDataset>"


    Examples:
    # Parameters : 
    # - nameDataset : Default names that launch some examples datasets
    |nameDataset |
    |COLEMAN     |
    |PAPER       |