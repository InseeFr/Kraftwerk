Feature: Do we save correctly all data file by file?
  Everybody wants to know if we save them correctly

  Scenario Outline: Do we create CSV root data file with the right structure
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service file by file
    Then Step 2 : We check root output file has <ExpectedLineCount> lines and <ExpectedDataFieldCount> variables
    Then We check if the CSV format is correct

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - ExpectedLineCount : Expected row quantity
    # - ExpectedDataFieldCount : Expected field quantity

    |Directory                        |ExpectedLineCount     |ExpectedDataFieldCount            |
    |SAMPLETEST-MULTIPLEDATA-v1       |7                     |136                               |
    |SAMPLETEST-MULTIPLEDATA-v2       |4                     |136                               |

  Scenario Outline: Do we create Parquet root data file with the right structure
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service file by file
    Then Step 2 : We check root parquet output file has <ExpectedLineCount> lines and <ExpectedDataFieldCount> variables

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - ExpectedLineCount : Expected row quantity
    # - ExpectedDataFieldCount : Expected field quantity

      |Directory                            |ExpectedLineCount     |ExpectedDataFieldCount            |
      |SAMPLETEST-MULTIPLEDATA-v1           |6                     |136                               |
      |SAMPLETEST-MULTIPLEDATA-v2           |3                     |136                               |

  Scenario Outline: Do we create CSV loop data file with the right structure
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service file by file
    Then Step 2 : We check "<LoopName>" output file has <ExpectedLineCount> lines and <ExpectedDataFieldCount> variables
    Then We check if the CSV format is correct

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - LoopName : Name of loop table
    # - ExpectedLineCount : Expected row quantity
    # - ExpectedDataFieldCount : Expected field quantity

      |Directory                        |LoopName         |ExpectedLineCount     |ExpectedDataFieldCount            |
      |SAMPLETEST-MULTIPLEDATA-v1       |BOUCLE_PRENOMS   |11                     |222                               |
      |SAMPLETEST-MULTIPLEDATA-v2       |BOUCLE_PRENOMS   |6                     |222                               |

  Scenario Outline: Do we create Parquet loop data file with the right structure
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service file by file
    Then We check "<LoopName>" parquet output file has <ExpectedLineCount> lines and <ExpectedDataFieldCount> variables

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - LoopName : Name of loop table
    # - ExpectedLineCount : Expected row quantity
    # - ExpectedDataFieldCount : Expected field quantity

      |Directory                            |LoopName         |ExpectedLineCount     |ExpectedDataFieldCount            |
      |SAMPLETEST-MULTIPLEDATA-v1           |BOUCLE_PRENOMS   |10                     |222                               |
      |SAMPLETEST-MULTIPLEDATA-v2           |BOUCLE_PRENOMS   |5                     |222                               |
