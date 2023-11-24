Feature: Do we get the paradata from the collected surveys ?
  We try to get the duration of the orchestrators in the paradata files

  Scenario Outline: Do we get the paradata
    Given We read data from input named "<Directory>"
    When I try to collect paradata's useful infos
    Then We check we have <nbLines> paradata lines in total, and that UE "<identifier>" has <nbOrchestrators> orchestrators during "<expectedDuration>" and <nbSessions> sessions during "<sessionTime>"
    And For UE "<identifier>" the collection date should be "<expectedCollectionDate>"

    Examples:
    # We try to get the paradata, and count the values
    # Parameters : 
    # - inputLoaderFile : name of the modeInputs, with the extension
    # - nbLines : to write 
    |Directory                   | nbLines | identifier | expectedDuration   | nbOrchestrators | nbSessions   |   sessionTime      |expectedCollectionDate  |
    |SAMPLETEST-PARADATA-v1      | 1       | 0000007    | 0 jours, 01:04:20  | 6               |  6           | 0 jours, 01:04:22  |2023-11-22T14:57:02.932 |
