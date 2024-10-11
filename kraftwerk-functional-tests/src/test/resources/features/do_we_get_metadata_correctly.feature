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

      |Directory                        |NumberOfVariables   | NumberOfStringVariables    |
      |SAMPLETEST-METADATA              |370                 | 174                        |
      |SAMPLETEST-SIMPLE-RESPONSE       |10                  | 1                          |
      |SAMPLETEST-UCQ                   |8                   | 4                          |
      |SAMPLETEST-MCQ                   |16                  | 9                          |
      |SAMPLETEST-TABLES                |26                  | 11                         |
      |SAMPLETEST-ROUNDABOUT            |6                   | 1                          |

  Scenario Outline: Does the affectations of variables are correct with Lunatic?
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We initialize metadata model with lunatic specification only
    Then The variables "<Variables>" should be respectively in groups "<ExpectedGroups>"

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - NumberOfVariables : Expected number of variables identified in Lunatic Json File
    # - NumberOfStringVariables : Expected number of variables of String Type

      |Directory                        |Variables                                      |ExpectedGroups                               |
      |SAMPLETEST-METADATA              |RESIDENCE,SEXE_TEL,SEXE_TEL_MISSING            |RACINE,BOUCLE_PRENOM_TEL,BOUCLE_PRENOM_TEL   |
      |SAMPLETEST-ROUNDABOUT            |NBIND,PRENOM,FILTER_RESULT_PRENOM              |RACINE,BOUCLE_PRENOM,BOUCLE_PRENOM           |
      |SAMPLETEST-TABLES                |AGE,TABLISTE2F1,FILTER_RESULT_TABCODELIST1D21  |BOUCLE_AGE,BOUCLE_TABLISTE2F1,BOUCLE_PRENOM  |

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
      |SAMPLETEST-METADATA              |370                            | 163                        |
      |SAMPLETEST-SIMPLE-RESPONSE       |10                             | 1                          |
      |SAMPLETEST-UCQ                   |8                              | 4                          |
      |SAMPLETEST-MCQ                   |16                             | 9                          |
      |SAMPLETEST-TABLES                |26                             | 11                         |
      |SAMPLETEST-ROUNDABOUT            |6                              | 1                          |