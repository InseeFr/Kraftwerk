Feature: Do we secure correctly the endpoints ?

  Scenario Outline: Do the role USER_KRAFTWERK have correct authorization?
    Given A user with the role "<userRole>"
    When He sends a PUT request with main service with "<folder>" input folder
    Then Response should be <statusCode>

    Examples:
    # - nameColemanDataset, namePaperDataset : Names for the datasets to aggregate
      | userRole                      | folder                      | statusCode            |
      | USER_KRAFTWERK                | SAMPLETEST-SIMPLE-RESPONSE  | 200                   |