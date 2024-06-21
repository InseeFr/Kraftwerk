Feature: Do we increment correctly the datasets file by file ?

  Scenario Outline: Do we increment correctly in csv and parquet file
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service file by file
    Then Step 5 : We check if we have <lineCount> lines
    Then Step 6 : We check if we have <variableCount> variables
    Then We check if there is only one header
    Then Step 2 : We check root parquet output file has <parquetLineCount> lines and <variableCount> variables

    # linecount-1 because there is a final empty line in csv
    Examples:
    | Directory                   | lineCount | parquetLineCount | variableCount |
    | SAMPLETEST-MULTIPLEDATA-v1  | 7         | 6                | 136           |