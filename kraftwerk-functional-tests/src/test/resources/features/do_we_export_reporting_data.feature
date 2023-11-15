Feature: Do we save correctly all reporting data ?
  Everybody wants to know if we save them correctly

  Scenario Outline: Do we create reporting data file with the right structure
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service
    Then We should have a file named "<OutputFileName>" in directory "<Directory>" with <ExpectedReportingDataFieldCount> reporting data fields

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - OutputFileName : Name of reporting data file (with .csv extension)
    # - ExpectedReportingDataFieldCount : Expected field quantity excluding IdUE

    |Directory                        |OutputFileName                                    |ExpectedReportingDataFieldCount   |
    |SAMPLETEST-REPORTINGDATA-v1      |SAMPLETEST-REPORTINGDATA-v1_REPORTINGDATA.csv     |70                                |



  Scenario Outline: Do we have the good amount of lines
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service
    Then We should have <ExpectedTotalCount> lines different than header in a file named "<OutputFileName>" in directory "<Directory>"

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - OutputFileName : Name of reporting data file (with .csv extension)
    # - ExpectedTotalCount : Expected line count (other than header)

      |Directory                        |OutputFileName                                    |ExpectedTotalCount |
      |SAMPLETEST-REPORTINGDATA-v1      |SAMPLETEST-REPORTINGDATA-v1_REPORTINGDATA.csv     |3                  |

  Scenario Outline: Does the file have a correct date format
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service
    Then In file named "<OutputFileName>" in directory "<Directory>" we should have the following date format : "<ExpectedDateFormat>"

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - OutputFileName : Name of reporting data file (with .csv extension)
    # - ExpectedDateformat : Expected date format in file

      |Directory                        |OutputFileName                                    |ExpectedDateFormat  |
      |SAMPLETEST-REPORTINGDATA-v1      |SAMPLETEST-REPORTINGDATA-v1_REPORTINGDATA.csv     |yyyy-MM-dd-hh-mm-ss |


  Scenario Outline: The file has all the contact attempts of a certain type
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service
    Then For SurveyUnit "<SurveyUnitId>" we should have <ExpectedSpecificStatusCount> contact attempts with status "<ExpectedStatus>" in a file named "<OutputFileName>" in directory "<Directory>"

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - OutputFileName : Name of reporting data file (with .csv extension)
    # - SurveyUnitId : Survey unit identifier
    # - ExpectedSpecificStatusCount : Expected count for said status
    # - ExpectedStatus : Expected status (in input file)

      |Directory                        |OutputFileName                                    |SurveyUnitId |ExpectedSpecificStatusCount  |ExpectedStatus   |
      |SAMPLETEST-REPORTINGDATA-v1      |SAMPLETEST-REPORTINGDATA-v1_REPORTINGDATA.csv     |0000003      |2                            |REF              |

  Scenario Outline: The file has all the contact states of a specific type
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service
    Then For SurveyUnit "<SurveyUnitId>" we should have <ExpectedSpecificStatusCount> contact states with status "<ExpectedStatus>" in a file named "<OutputFileName>" in directory "<Directory>"

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - OutputFileName : Name of reporting data file (with .csv extension)
    # - SurveyUnitId : Survey unit identifier
    # - ExpectedSpecificStatusCount : Expected count for said status
    # - ExpectedStatus : Expected status (in input file)

      |Directory                        |OutputFileName                                    |SurveyUnitId |ExpectedSpecificStatusCount  |ExpectedStatus   |
      |SAMPLETEST-REPORTINGDATA-v1      |SAMPLETEST-REPORTINGDATA-v1_REPORTINGDATA.csv     |0000002      |5                            |WFT              |


  Scenario Outline: The root file doesn't have any reporting data
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service
    Then We shouldn't have any reporting data in "<RootFileName>" in directory "<Directory>"

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - RootFileName : Name of root file (with .csv extension)

      |Directory                        |RootFileName                               |
      |SAMPLETEST-REPORTINGDATA-v1      |SAMPLETEST-REPORTINGDATA-v1_RACINE.csv     |
      |SAMPLETEST-DATAONLY-v1           |SAMPLETEST-DATAONLY-v1_RACINE.csv          |

  Scenario Outline: If there is no reporting data, there is no reporting data file
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service
    Then We shouldn't have any reporting data file in directory "<Directory>"

  Examples:
  # Parameters :
  # - Directory : Directory of test campaigns

    |Directory                        |
    |SAMPLETEST-DATAONLY-v1           |

  Scenario Outline: Does the OUTCOME_SPOTTING is computed correctly
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service
    Then For SurveyUnit "<SurveyUnitId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedOutcomeSpottingStatus>" in the OUTCOME_SPOTTING field

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - SurveyUnitId : Survey unit identifier
    # - OutputFileName : Name of reporting data file (with .csv extension)
    # - ExpectedOutcomeSpottingStatus : Expected outcome spotting status in outputfile

      |Directory                        |OutputFileName                                    |SurveyUnitId   |ExpectedOutcomeSpottingStatus   |
      |SAMPLETEST-REPORTINGDATA-v1      |SAMPLETEST-REPORTINGDATA-v1_REPORTINGDATA.csv     |0000001        |DESTROY                         |