Feature: Do we save loop data correctly ?

  Scenario Outline: Do we save collected variables loop data correctly ?
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service file by file
    Then In csv loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration <LoopIteration> we should have value "<ExpectedValue>" for field "<FieldName>"

    Examples:
      | Directory               |LoopName       | InterrogationId | LoopIteration | FieldName        | ExpectedValue     |
      | SAMPLETEST-PARADATA-v1  |B_PRENOMREP    | 0000007         | 1             |ENF               | 1                 |
      | SAMPLETEST-PARADATA-v1  |B_PRENOMREP    | 0000007         | 2             |ENF               | 2                 |

  Scenario Outline: Do we save external variables loop data correctly ?
    Given Step 0 : We have some survey in directory "<Directory>"
    When Step 1 : We launch main service file by file
    Then In csv loop file for loop "<LoopName>" for interrogationId "<InterrogationId>" and iteration <LoopIteration> we should have value "<ExpectedValue>" for field "<FieldName>"

    Examples:
      | Directory               |LoopName       | InterrogationId | LoopIteration | FieldName      | ExpectedValue     |
      | SAMPLETEST-PARADATA-v1  |B_PRENOMREP    | 0000007         | 1             |RPPRENOM        | TESTRPRENOM7_2    |
      | SAMPLETEST-PARADATA-v1  |B_PRENOMREP    | 0000007         | 2             |RPPRENOM        | TESTRPRENOM7PAR1  |