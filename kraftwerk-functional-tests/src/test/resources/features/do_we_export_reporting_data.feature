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
      |suivi/SAMPLEREPORTINGDATA_TEL.xml  |SAMPLETEST-REPORTINGDATA-MOOG-v1 |SAMPLETEST-REPORTINGDATA-MOOG-v1_REPORTINGDATA.csv |yyyy-MM-dd-hh-mm-ss |Report_DATE_COLLECTE  |


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

  Scenario Outline: Does the new variables are computed correctly
    Given Step 0 : We have some survey in directory "<Directory>"
    And We have reporting data file in "<ReportingDataFile>"
    When We launch reporting data service
    Then For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedIdentification>" in the "identification" field

    Examples:
    # Parameters :
    # - Directory : Directory of test campaigns
    # - InterrogationId : Interrogation identifier
    # - OutputFileName : Name of reporting data file (with .csv extension)
    # - ExpectedOutcomeSpottingStatus : Expected outcome spotting status in outputfile

      |ReportingDataFile                  |Directory                        |OutputFileName                                    |InterrogationId   |ExpectedIdentification          |
      |suivi/SAMPLEREPORTINGDATA_TEL.xml  |SAMPLETEST-REPORTINGDATA-v2      |SAMPLETEST-REPORTINGDATA-v2_REPORTINGDATA.csv     |0000001           |DESTROY                         |

  Scenario Outline: Does the OUTCOME_SPOTTING is computed correctly using a standard VTL file
    Given Step 0 : We have some survey in directory "<Directory>"
    And We have reporting data file in "<ReportingDataFile>"
    When We launch reporting data service
    Then For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedOutcomeSpottingStatus>" in the "outcome_spotting" field

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
    And For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedIdentification>" in the "identification" field
    And For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedOutcomeSpottingStatus>" in the "outcome_spotting" field
    And The output file "<RootOutputFileName>" should not exist
    Examples:
      |ReportingDataFile                  | Directory                    | OutputFileName                                     |InterrogationId   |ExpectedIdentification |ExpectedStatus  | ExpectedSpecificStatusCount | ExpectedOutcomeSpottingStatus |RootOutputFileName                     |
      |suivi/SAMPLEREPORTINGDATA_TEL.xml  | SAMPLETEST-REPORTINGDATA-v2  | SAMPLETEST-REPORTINGDATA-v2_REPORTINGDATA.csv      |0000002           |IDENTIFIED             |WFT             | 5                           | TEST                          |SAMPLETEST-REPORTINGDATA-v2_RACINE.csv |

  Scenario Outline: Reporting data only export (genesis paths)
    Given Step 0 : We have some survey in directory "<Directory>"
    And We have reporting data file in "<ReportingDataFile>"
    When We launch reporting data service with genesis input path with mode "TEL"
    Then For SurveyUnit "<InterrogationId>" we should have <ExpectedSpecificStatusCount> contact states with status "<ExpectedStatus>" in a file named "<OutputFileName>" in directory "<Directory>"
    And For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedIdentification>" in the "identification" field
    And For SurveyUnit "<InterrogationId>" in a file named "<OutputFileName>" in directory "<Directory>" we should have "<ExpectedOutcomeSpottingStatus>" in the "outcome_spotting" field
    And The output file "<RootOutputFileName>" should not exist
    Examples:
      |ReportingDataFile                  | Directory                    | OutputFileName                                |InterrogationId   |ExpectedIdentification |ExpectedStatus  | ExpectedSpecificStatusCount | ExpectedOutcomeSpottingStatus |RootOutputFileName                     |
      |suivi/SAMPLEREPORTINGDATA_TEL.xml  | SAMPLETEST-REPORTINGDATA-v2  | SAMPLETEST-REPORTINGDATA-v2_REPORTINGDATA.csv |0000002           |IDENTIFIED             |WFT             | 5                           | TEST                          |SAMPLETEST-REPORTINGDATA-v2_RACINE.csv |
