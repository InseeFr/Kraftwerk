Feature: Do we save correctly all data file by file?
  Everybody wants to know if we save them correctly

  Scenario Outline: Do we create data files with the right structure with file by file
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service file by file
    Then Step 2 : We check root output file has <ExpectedLineCount> lines and <ExpectedDataFieldCount> variables
    And We check if the CSV format is correct
    And Step 2 : We check root parquet output file has <ExpectedLineCount> lines and <ExpectedDataFieldCount> variables
    Then Step 2 : We check "<LoopName>" output file has <ExpectedLoopLineCount> lines and <ExpectedLoopDataFieldCount> variables
    And We check if the CSV format is correct
    And We check "<LoopName>" parquet output file has <ExpectedLoopLineCount> lines and <ExpectedLoopDataFieldCount> variables

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - ExpectedLineCount : Expected row quantity
    # - ExpectedDataFieldCount : Expected field quantity

  |Directory                        |ExpectedLineCount     |ExpectedDataFieldCount |LoopName         |ExpectedLoopLineCount     |ExpectedLoopDataFieldCount       |
  |SAMPLETEST-MULTIPLEDATA-v1       |7                     |136                    |BOUCLE_PRENOMS   |11                        |222                              |
  |SAMPLETEST-MULTIPLEDATA-v2       |4                     |136                    |BOUCLE_PRENOMS   |6                         |222                              |
  |SAMPLETEST-PARADATA-v1           |2                     |13                     |B_PRENOMREP      |3                         |1088                             |
