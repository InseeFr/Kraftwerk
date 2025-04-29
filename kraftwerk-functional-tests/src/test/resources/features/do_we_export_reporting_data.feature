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
    # - ExpectedReportingDataFieldCount : Expected field quantity excluding interrogationId

    |Directory                        |OutputFileName                                         |ExpectedReportingDataFieldCount   |
    |SAMPLETEST-REPORTINGDATA-v1      |SAMPLETEST-REPORTINGDATA-v1_REPORTINGDATA.csv          |75                                |
    |SAMPLETEST-REPORTINGDATA-v2      |SAMPLETEST-REPORTINGDATA-v2_REPORTINGDATA.csv          |75                                |
    |SAMPLETEST-REPORTINGDATA-MOOG-v1 |SAMPLETEST-REPORTINGDATA-MOOG-v1_REPORTINGDATA.csv     |31                                |



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
      |SAMPLETEST-REPORTINGDATA-v1      |SAMPLETEST-REPORTINGDATA-v1_REPORTINGDATA.csv     |4                  |


  Scenario Outline: Do we have the good amount of lines when we exclude non-respondents
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service with an export of reporting data only for survey respondents
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
    Then In file named "<OutputFileName>" in directory "<Directory>" we should have the date format "yyyy-MM-dd-hh-mm-ss" for field "<FieldName>"

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - OutputFileName : Name of reporting data file (with .csv extension)
    # - ExpectedDateformat : Expected date format in file

      |Directory                        |OutputFileName                                      |FieldName             |
      |SAMPLETEST-REPORTINGDATA-v1      |SAMPLETEST-REPORTINGDATA-v1_REPORTINGDATA.csv       |STATE_1_DATE          |
      |SAMPLETEST-REPORTINGDATA-MOOG-v1 |SAMPLETEST-REPORTINGDATA-MOOG-v1_REPORTINGDATA.csv  |Report_DATE_COLLECTE  |


  Scenario Outline: The file has all the contact attempts of a certain type
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service
    Then For SurveyUnit "<InterrogationId>" we should have <ExpectedSpecificStatusCount> contact attempts with status "<ExpectedStatus>" in a file named "<OutputFileName>" in directory "<Directory>"

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - OutputFileName : Name of reporting data file (with .csv extension)
    # - InterrogationId : Interrogation identifier
    # - ExpectedSpecificStatusCount : Expected count for said status
    # - ExpectedStatus : Expected status (in input file)

      |Directory                        |OutputFileName                                    |InterrogationId |ExpectedSpecificStatusCount  |ExpectedStatus   |
      |SAMPLETEST-REPORTINGDATA-v1      |SAMPLETEST-REPORTINGDATA-v1_REPORTINGDATA.csv     |0000003         |2                            |REF              |

  Scenario Outline: The file has all the contact states of a specific type
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service
    Then For SurveyUnit "<InterrogationId>" we should have <ExpectedSpecificStatusCount> contact states with status "<ExpectedStatus>" in a file named "<OutputFileName>" in directory "<Directory>"

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - OutputFileName : Name of reporting data file (with .csv extension)
    # - InterrogationId : Interrogation identifier
    # - ExpectedSpecificStatusCount : Expected count for said status
    # - ExpectedStatus : Expected status (in input file)

      |Directory                        |OutputFileName                                    |InterrogationId |ExpectedSpecificStatusCount  |ExpectedStatus   |
      |SAMPLETEST-REPORTINGDATA-v1      |SAMPLETEST-REPORTINGDATA-v1_REPORTINGDATA.csv     |0000002         |5                            |WFT              |


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


  Scenario Outline: Does the identification variables are computed correctly
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service
    Then For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedIdentification>" in the "identification" field

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - InterrogationId : Interrogation identifier
    # - OutputFileName : Name of reporting data file (with .csv extension)
    # - ExpectedOutcomeSpottingStatus : Expected outcome spotting status in outputfile

      |Directory                        |OutputFileName                                    |InterrogationId   |ExpectedIdentification          |
      |SAMPLETEST-REPORTINGDATA-v2      |SAMPLETEST-REPORTINGDATA-v2_REPORTINGDATA.csv     |0000001           |DESTROY                         |


  Scenario Outline: Does the OUTCOME_SPOTTING is computed correctly
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service
    Then For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedOutcomeSpottingStatus>" in the "outcome_spotting" field

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - InterrogationId : Interrogation identifier
    # - OutputFileName : Name of reporting data file (with .csv extension)
    # - ExpectedOutcomeSpottingStatus : Expected outcome spotting status in outputfile

      |Directory                        |OutputFileName                                    |InterrogationId   |ExpectedOutcomeSpottingStatus   |
      |SAMPLETEST-REPORTINGDATA-v2      |SAMPLETEST-REPORTINGDATA-v2_REPORTINGDATA.csv     |0000001           |TEST                            |

    #TODO Adapt this test when we have the correct TEL .vtl script


  Scenario Outline: Does the InseeSampleIdentifiers variables are exported correctly
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service
    Then For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedNoGrap>" in the "NoGrap" field
    And For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedNoLog>" in the "NoLog" field
    And For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedNole>" in the "Nole" field
    And For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedAutre>" in the "Autre" field
    Examples:
      |Directory                        |OutputFileName                                    |InterrogationId   | ExpectedNoGrap   | ExpectedNoLog  | ExpectedNole | ExpectedAutre |
      |SAMPLETEST-REPORTINGDATA-V3      |SAMPLETEST-REPORTINGDATA-V3_REPORTINGDATA.csv     |99P10352919       | 1                | 7              | 8            | 9             |
      |SAMPLETEST-REPORTINGDATA-V3      |SAMPLETEST-REPORTINGDATA-V3_REPORTINGDATA.csv     |79P10160878       | 3                | 2              | 1            |               |
      |SAMPLETEST-REPORTINGDATA-V3      |SAMPLETEST-REPORTINGDATA-V3_REPORTINGDATA.csv     |79P10160880       | 0                | 0              | 0            |               |

  Scenario Outline: Does the ClosingCause variables are exported correctly
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service
    Then For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedClosingCause>" in the "ClosingCause" field
    And For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedClosingCauseDate>" in the "ClosingCause_Date" field
    Examples:
      |Directory                        |OutputFileName                                    |InterrogationId   | ExpectedClosingCause | ExpectedClosingCauseDate |
      |SAMPLETEST-REPORTINGDATA-V3      |SAMPLETEST-REPORTINGDATA-V3_REPORTINGDATA.csv     |79P10160878       | NPA                  | 2025-02-18-09-25-57      |
      |SAMPLETEST-REPORTINGDATA-V3      |SAMPLETEST-REPORTINGDATA-V3_REPORTINGDATA.csv     |79P10160880       | ROW                  | 2025-02-18-09-25-48      |

  Scenario Outline: Does the identification configuration, individual status and can process variables are exported correctly
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service
    Then In a file named "<OutputFileName>" in directory "<Directory>" we should only have "<ExpectedIdentificationConfiguration>" in the "IdentificationConfiguration" field
    And For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedIndividualStatus>" in the "individualStatus" field
    And For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedInterviewerCanProcess>" in the "interviewerCanProcess" field
    Examples:
      |Directory                        |OutputFileName                                    |InterrogationId   | ExpectedIdentificationConfiguration | ExpectedIndividualStatus | ExpectedInterviewerCanProcess |
      |SAMPLETEST-REPORTINGDATA-V4      |SAMPLETEST-REPORTINGDATA-V4_REPORTINGDATA.csv     |INDTEL811_tech1   | INDTEL                              | SAME_ADDRESS             |                               |
      |SAMPLETEST-REPORTINGDATA-V4      |SAMPLETEST-REPORTINGDATA-V4_REPORTINGDATA.csv     |INDTEL987_tech    | INDTEL                              | OTHER_ADDRESS            |                               |
      |SAMPLETEST-REPORTINGDATA-V5      |SAMPLETEST-REPORTINGDATA-V5_REPORTINGDATA.csv     |INDF2F02_tech     | INDF2F                              | SAME_ADDRESS             |                               |
      |SAMPLETEST-REPORTINGDATA-V5      |SAMPLETEST-REPORTINGDATA-V5_REPORTINGDATA.csv     |INDF2F05_tech     | INDF2F                              | OTHER_ADDRESS            | YES                           |
      |SAMPLETEST-REPORTINGDATA-V5      |SAMPLETEST-REPORTINGDATA-V5_REPORTINGDATA.csv     |INDF2F15_tech     | INDF2F                              | OTHER_ADDRESS            | NO                            |
