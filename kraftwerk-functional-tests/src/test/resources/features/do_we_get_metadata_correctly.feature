Feature: Do we retrieve correctly all metadata from specifications ?

  Scenario Outline: Does the metadata model is correct with Lunatic?
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We initialize metadata model with lunatic specification only
    Then We should have a metadata model with <NumberOfVariables> variables
    And We should have <NumberOfStringVariables> of type STRING

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - NumberOfVariables : Expected number of variables identified in Lunatic Json File
    # - NumberOfStringVariables : Expected number of variables of String Type

      |Directory                        |NumberOfVariables              | NumberOfStringVariables    |
      |SAMPLETEST-METADATA              |370                            | 174                        |

  Scenario Outline: Does the metadata model is correct with DDI?
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We initialize metadata model with DDI specification only
    Then We should have a metadata model with <NumberOfVariables> variables
    And We should have <NumberOfStringVariables> of type STRING

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - NumberOfVariables : Expected number of variables identified in Lunatic Json File
    # - NumberOfStringVariables : Expected number of variables of String Type

      |Directory                        |NumberOfVariables              | NumberOfStringVariables    |
    #  |SAMPLETEST-METADATA              |370                            | 163                        |
    #  |SAMPLETEST-SIMPLE-RESPONSE       |10                             | 1                          |
    #  |SAMPLETEST-UCQ                   |8                              | 4                          |
    #  |SAMPLETEST-MCQ                   |16                             | 9                          |
      |SAMPLETEST-TABLES                |18                             | 11                         |