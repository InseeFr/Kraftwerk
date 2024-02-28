#Feature: Do we execute TCM standard script ?
#  We try to get the duration of the orchestrators in the paradata files
#
#Scenario Outline: Does the standard TCM script has been executed
#Given Step 0 : We have some survey in directory "<Directory>"
#  And We have a test VTL script named "<TCMScriptName>" creating a variable "<FieldName>" in dataset "<DatasetName>"
#When Step 1 : We launch main service
#  And We clean the test VTL script named "<TCMScriptName>"
#Then In a file named "<OutputFileName>" there should be a "<FieldName>" field
#
#Examples:
#    # Parameters :
#    # - Directory : Directory of test campaigns
#    # - OutputFileName : Name of reporting data file (with .csv extension)
#    # - SurveyUnitId : Survey unit identifier
#    # - FieldName : Expected field name
#
#|Directory                        |OutputFileName                    |TCMScriptName  |FieldName       |DatasetName |
#|SAMPLETEST-DATAONLY-v1           |SAMPLETEST-DATAONLY-v1_RACINE.csv |TCM_TEST       |TEST_TCM_1      |FAF         |
#
# TODO Enable this when we have the DDI sequence extraction XSLT script