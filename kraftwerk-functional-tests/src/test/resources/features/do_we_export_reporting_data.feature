Feature: Do we save correctly all reporting data ?
  Everybody wants to know if we save them correctly

  Scenario Outline: Do we create reporting data file with the right structure
    Given Step 0 : We have some survey in directory "<Directory>"
    And We have reporting data file in "<ReportingDataFile>"
    When We launch reporting data service
    Then We should have a file named "<OutputFileName>" in directory "<Directory>" with <ExpectedReportingDataFieldCount> reporting data fields

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - OutputFileName : Name of reporting data file (with .csv extension)
    # - ExpectedReportingDataFieldCount : Expected field quantity excluding interrogationId
      |ReportingDataFile                  |Directory                        |OutputFileName                                         |ExpectedReportingDataFieldCount   |
      |suivi/SAMPLEREPORTINGDATA_TEL.xml  |SAMPLETEST-REPORTINGDATA-v1      |SAMPLETEST-REPORTINGDATA-v1_REPORTINGDATA.csv          |84                                |
      |suivi/SAMPLEREPORTINGDATA_TEL.xml  |SAMPLETEST-REPORTINGDATA-v2      |SAMPLETEST-REPORTINGDATA-v2_REPORTINGDATA.csv          |84                                |
      |suivi/SAMPLEREPORTINGDATA_TEL.xml  |SAMPLETEST-REPORTINGDATA-MOOG-v1 |SAMPLETEST-REPORTINGDATA-MOOG-v1_REPORTINGDATA.csv     |40                                |



  Scenario Outline: Do we have the good amount of lines
    Given Step 0 : We have some survey in directory "<Directory>"
    And We have reporting data file in "<ReportingDataFile>"
    When We launch reporting data service
    Then We should have <ExpectedTotalCount> lines different than header in a file named "<OutputFileName>" in directory "<Directory>"

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - OutputFileName : Name of reporting data file (with .csv extension)
    # - ExpectedTotalCount : Expected line count (other than header)

      |ReportingDataFile                  |Directory                        |OutputFileName                                    |ExpectedTotalCount |
      |suivi/SAMPLEREPORTINGDATA_TEL.xml  |SAMPLETEST-REPORTINGDATA-v1      |SAMPLETEST-REPORTINGDATA-v1_REPORTINGDATA.csv     |4                  |


  Scenario Outline: Does the file have a correct date format
    Given Step 0 : We have some survey in directory "<Directory>"
    And We have reporting data file in "<ReportingDataFile>"
    When We launch reporting data service
    Then In file named "<OutputFileName>" in directory "<Directory>" we should have the date format "<ExpectedDateFormat>" for field "<FieldName>"

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - OutputFileName : Name of reporting data file (with .csv extension)
    # - ExpectedDateformat : Expected date format in file

      |ReportingDataFile                  |Directory                        |OutputFileName                                     |ExpectedDateFormat  |FieldName             |
      |suivi/SAMPLEREPORTINGDATA_TEL.xml  |SAMPLETEST-REPORTINGDATA-v1      |SAMPLETEST-REPORTINGDATA-v1_REPORTINGDATA.csv      |yyyy-MM-dd-hh-mm-ss |STATE_1_DATE          |
      |suivi/SAMPLEREPORTINGDATA_TEL.xml  |SAMPLETEST-REPORTINGDATA-MOOG-v1 |SAMPLETEST-REPORTINGDATA-MOOG-v1_REPORTINGDATA.csv |yyyy-MM-dd-hh-mm-ss |REPORT_DATE_COLLECTE  |


  Scenario Outline: The file has all the contact attempts of a certain type
    Given Step 0 : We have some survey in directory "<Directory>"
    And We have reporting data file in "<ReportingDataFile>"
    When We launch reporting data service
    Then For SurveyUnit "<InterrogationId>" we should have <ExpectedSpecificStatusCount> contact attempts with status "<ExpectedStatus>" in a file named "<OutputFileName>" in directory "<Directory>"

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - OutputFileName : Name of reporting data file (with .csv extension)
    # - InterrogationId : Interrogation identifier
    # - ExpectedSpecificStatusCount : Expected count for said status
    # - ExpectedStatus : Expected status (in input file)

      |ReportingDataFile                  |Directory                        |OutputFileName                                    |InterrogationId |ExpectedSpecificStatusCount  |ExpectedStatus   |
      |suivi/SAMPLEREPORTINGDATA_TEL.xml  |SAMPLETEST-REPORTINGDATA-v1      |SAMPLETEST-REPORTINGDATA-v1_REPORTINGDATA.csv     |0000003         |2                            |REF              |

  Scenario Outline: The file has all the contact states of a specific type
    Given Step 0 : We have some survey in directory "<Directory>"
    And We have reporting data file in "<ReportingDataFile>"
    When We launch reporting data service
    Then For SurveyUnit "<InterrogationId>" we should have <ExpectedSpecificStatusCount> contact states with status "<ExpectedStatus>" in a file named "<OutputFileName>" in directory "<Directory>"

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - OutputFileName : Name of reporting data file (with .csv extension)
    # - InterrogationId : Interrogation identifier
    # - ExpectedSpecificStatusCount : Expected count for said status
    # - ExpectedStatus : Expected status (in input file)

      |ReportingDataFile                  |Directory                        |OutputFileName                                    |InterrogationId |ExpectedSpecificStatusCount  |ExpectedStatus   |
      |suivi/SAMPLEREPORTINGDATA_TEL.xml  |SAMPLETEST-REPORTINGDATA-v1      |SAMPLETEST-REPORTINGDATA-v1_REPORTINGDATA.csv     |0000002         |5                            |WFT              |

  Scenario Outline: Does the identification variables are computed correctly
    Given Step 0 : We have some survey in directory "<Directory>"
    And We have reporting data file in "<ReportingDataFile>"
    When We launch reporting data service
    Then For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedIdentification>" in the "IDENTIFICATION" field
    And In a file named "<OutputFileName>" in directory "<Directory>" we should only have "<ExpectedTypeSpotting>" in the "TYPE_SPOTTING" field
    And For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedIndividualStatus>" in the "INDIVIDUALSTATUS" field
    And For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedInterviewerCanProcess>" in the "INTERVIEWERCANPROCESS" field
    And For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedOccupant>" in the "OCCUPANT" field


    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - InterrogationId : Interrogation identifier
    # - OutputFileName : Name of reporting data file (with .csv extension)
    # - ExpectedOutcomeSpottingStatus : Expected outcome spotting status in outputfile

      |ReportingDataFile                  |Directory                         |OutputFileName                                    |InterrogationId   |ExpectedIdentification | ExpectedTypeSpotting   | ExpectedIndividualStatus | ExpectedInterviewerCanProcess | ExpectedOccupant |
      |suivi/SAMPLEREPORTINGDATA_TEL.xml  |SAMPLETEST-REPORTINGDATA-v2       |SAMPLETEST-REPORTINGDATA-v2_REPORTINGDATA.csv     |0000001           |DESTROY                |                        |                          |                               |                  |
      |reporting_us.xml                   |SAMPLETEST-REPORTINGDATA-V3       |SAMPLETEST-REPORTINGDATA-V3_REPORTINGDATA.csv     |99P10352919       |IDENTIFIED             |                        |                          |                               | IDENTIFIED       |
      |campaign.extract_indtel_us9894.xml |SAMPLETEST-REPORTINGDATA-V4       |SAMPLETEST-REPORTINGDATA-V4_REPORTINGDATA.csv     |INDTEL987_tech    |                       | INDTEL                 | OTHER_ADDRESS            |                               |                  |
      |campaign.extract_indtel_us9894.xml |SAMPLETEST-REPORTINGDATA-V4       |SAMPLETEST-REPORTINGDATA-V4_REPORTINGDATA.csv     |INDTEL811_tech1   |                       | INDTEL                 | SAME_ADDRESS             |                               |                  |
      |campaign.extract_indf2f_us9894.xml |SAMPLETEST-REPORTINGDATA-V5       |SAMPLETEST-REPORTINGDATA-V5_REPORTINGDATA.csv     |INDF2F02_tech     |                       | INDF2F                 | SAME_ADDRESS             |                               |                  |
      |campaign.extract_indf2f_us9894.xml |SAMPLETEST-REPORTINGDATA-V5       |SAMPLETEST-REPORTINGDATA-V5_REPORTINGDATA.csv     |INDF2F05_tech     |                       | INDF2F                 | OTHER_ADDRESS            | YES                           |                  |
      |campaign.extract_indf2f_us9894.xml |SAMPLETEST-REPORTINGDATA-V5       |SAMPLETEST-REPORTINGDATA-V5_REPORTINGDATA.csv     |INDF2F15_tech     |                       | INDF2F                 | OTHER_ADDRESS            | NO                            |                  |



  Scenario Outline: Does the OUTCOME_SPOTTING is computed correctly using a standard VTL file
    Given Step 0 : We have some survey in directory "<Directory>"
    And We have reporting data file in "<ReportingDataFile>"
    When We launch reporting data service
    Then For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedOutcomeSpottingStatus>" in the "OUTCOME_SPOTTING" field

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - InterrogationId : Interrogation identifier
    # - OutputFileName : Name of reporting data file (with .csv extension)
    # - ExpectedOutcomeSpottingStatus : Expected outcome spotting status in outputfile

      |ReportingDataFile                  |Directory                        |OutputFileName                                    |InterrogationId   |ExpectedOutcomeSpottingStatus   |
      |suivi/SAMPLEREPORTINGDATA_TEL.xml  |SAMPLETEST-REPORTINGDATA-v2      |SAMPLETEST-REPORTINGDATA-v2_REPORTINGDATA.csv     |0000001           |TEST                            |

    #TODO Adapt this test when we have the correct TEL .vtl script

  Scenario Outline: Reporting data only export (main paths)
    Given Step 0 : We have some survey in directory "<Directory>"
    And We have reporting data file in "<ReportingDataFile>"
    When We launch reporting data service
    Then For SurveyUnit "<InterrogationId>" we should have <ExpectedSpecificStatusCount> contact states with status "<ExpectedStatus>" in a file named "<OutputFileName>" in directory "<Directory>"
    And For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedIdentification>" in the "IDENTIFICATION" field
    And In parquet reporting data file in directory "<Directory>" for interrogationId "<InterrogationId>" we should have value "<ExpectedIdentification>" for field "IDENTIFICATION"
    And For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedOutcomeSpottingStatus>" in the "OUTCOME_SPOTTING" field
    And In parquet reporting data file in directory "<Directory>" for interrogationId "<InterrogationId>" we should have value "<ExpectedOutcomeSpottingStatus>" for field "OUTCOME_SPOTTING"
    And The output file "<RootOutputFileName>" should not exist
    Examples:
      |ReportingDataFile                  | Directory                    | OutputFileName                                     |InterrogationId   |ExpectedIdentification |ExpectedStatus  | ExpectedSpecificStatusCount | ExpectedOutcomeSpottingStatus |RootOutputFileName                     |
      |suivi/SAMPLEREPORTINGDATA_TEL.xml  | SAMPLETEST-REPORTINGDATA-v2  | SAMPLETEST-REPORTINGDATA-v2_REPORTINGDATA.csv      |0000002           |IDENTIFIED             |WFT             | 5                           | TEST                          |SAMPLETEST-REPORTINGDATA-v2_RACINE.csv |

  Scenario Outline: Reporting data new variables export
    Given Step 0 : We have some survey in directory "<Directory>"
    And We have reporting data file in "<ReportingDataFile>"
    When We launch reporting data service
    Then For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedNoGrap>" in the "NOGRAP" field
    And For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedNoLog>" in the "NOLOG" field
    And For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedNole>" in the "NOLE" field
    And For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedAutre>" in the "AUTRE" field
    Then For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedClosingCause>" in the "CLOSINGCAUSE" field
    And For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedClosingCauseDate>" in the "CLOSINGCAUSE_DATE" field
    Examples:
      |ReportingDataFile |Directory                        |OutputFileName                                    |InterrogationId   | ExpectedNoGrap   | ExpectedNoLog  | ExpectedNole | ExpectedAutre | ExpectedClosingCause | ExpectedClosingCauseDate |
      |reporting_us.xml  |SAMPLETEST-REPORTINGDATA-V3      |SAMPLETEST-REPORTINGDATA-V3_REPORTINGDATA.csv     |99P10352919       | 1                | 7              | 8            | 9             |                      |                          |
      |reporting_us.xml  |SAMPLETEST-REPORTINGDATA-V3      |SAMPLETEST-REPORTINGDATA-V3_REPORTINGDATA.csv     |79P10160878       | 3                | 2              | 1            |               | NPA                  | 2025-02-18-09-25-57      |
      |reporting_us.xml  |SAMPLETEST-REPORTINGDATA-V3      |SAMPLETEST-REPORTINGDATA-V3_REPORTINGDATA.csv     |79P10160880       | 0                | 0              | 0            |               | ROW                  | 2025-02-18-09-25-48      |

  Scenario Outline: Reporting data only export (genesis paths)
    Given Step 0 : We have some survey in directory "<Directory>"
    And We have reporting data file in "<ReportingDataFile>"
    When We launch reporting data service with genesis input path with mode "TEL"
    Then For SurveyUnit "<InterrogationId>" we should have <ExpectedSpecificStatusCount> contact states with status "<ExpectedStatus>" in a file named "<OutputFileName>" in directory "<Directory>"
    And For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedIdentification>" in the "IDENTIFICATION" field
    And In parquet reporting data file in directory "<Directory>" for interrogationId "<InterrogationId>" we should have value "<ExpectedIdentification>" for field "IDENTIFICATION"
    And For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedOutcomeSpottingStatus>" in the "OUTCOME_SPOTTING" field
    And In parquet reporting data file in directory "<Directory>" for interrogationId "<InterrogationId>" we should have value "<ExpectedOutcomeSpottingStatus>" for field "OUTCOME_SPOTTING"
    And The output file "<RootOutputFileName>" should not exist
    Examples:
      |ReportingDataFile                  | Directory                    | OutputFileName                                |InterrogationId   |ExpectedIdentification |ExpectedStatus  | ExpectedSpecificStatusCount | ExpectedOutcomeSpottingStatus |RootOutputFileName                     |
      |suivi/SAMPLEREPORTINGDATA_TEL.xml  | SAMPLETEST-REPORTINGDATA-v2  | SAMPLETEST-REPORTINGDATA-v2_REPORTINGDATA.csv |0000002           |IDENTIFIED             |WFT             | 5                           | TEST                          |SAMPLETEST-REPORTINGDATA-v2_RACINE.csv |
