Feature: Do we aggregate correctly the datasets we got ?

  Scenario Outline: Do we aggregate correctly
    Given We have some VTLBindings named "<nameColemanDataset>" and "<namePaperDataset>"
    When I try to aggregate the bindings
    Then The datasets I try to aggregate should return an aggregated dataset
    
    Examples:
    # - nameColemanDataset, namePaperDataset : Names for the datasets to aggregate
    | nameColemanDataset | namePaperDataset |
    | CAWI               | PAPI             |
    