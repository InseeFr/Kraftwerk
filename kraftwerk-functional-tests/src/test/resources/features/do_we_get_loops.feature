Feature: Do we save loop data correctly ?

  Scenario Outline: Do we save collected variables loop data in csv file correctly ?
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service file by file
    Then In csv loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration 1 we should have value "<ExpectedCollectedValue1>" for field "<CollectedFieldName>"
    And In csv loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration 2 we should have value "<ExpectedCollectedValue2>" for field "<CollectedFieldName>"
    And In parquet loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration 1 we should have value "<ExpectedCollectedValue1>" for field "<CollectedFieldName>"
    And In parquet loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration 2 we should have value "<ExpectedCollectedValue2>" for field "<CollectedFieldName>"
    Then In csv loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration 1 we should have value "<ExpectedExternalValue1>" for field "<ExternalFieldName>"
    And In csv loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration 2 we should have value "<ExpectedExternalValue2>" for field "<ExternalFieldName>"
    And In parquet loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration 1 we should have value "<ExpectedExternalValue1>" for field "<ExternalFieldName>"
    And In parquet loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration 2 we should have value "<ExpectedExternalValue2>" for field "<ExternalFieldName>"

    Examples:
      | Directory               |LoopName       | InterrogationId | CollectedFieldName | ExternalFieldName | ExpectedCollectedValue1    | ExpectedCollectedValue2   | ExpectedExternalValue1 | ExpectedExternalValue2 |
      | SAMPLETEST-PARADATA-v1  |B_PRENOMREP    | 0000007         |ENF                 | RPPRENOM          | 1                          | 2                         | TESTRPRENOM7_2         | TESTRPRENOM7PAR1       |