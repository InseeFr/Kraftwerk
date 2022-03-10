Feature: Do we get the variables from the DDI ?
  We try to get the list of all variables of a DDI, including the structure of groups of variables, with the link to the DDI as a parameter

  Scenario Outline: Do we get the variables
    Given DDI is here at "<linkDDI>"
    When I try to collect the variables's infos
    Then The variables I try to count should answer <expectedNumberVariables> and have "<expectedGroup>" in it

    Examples:
    # We try to get the DDIs, and count the 
    # Parameters : 
    # - expectedNumberVariables : expected number of variables, independant of any group
    # - expectedGroup : name of one group that's in the DDI and we expect it to appears in the datastructure after collecting the DDI
    # - linkDDI : link to the DDI in the Metallica's gitlab
    |expectedNumberVariables |expectedGroup        |linkDDI                                                                                                                     |
    |91                      |FAVOURITE_CHARACTERS |https://gitlab.insee.fr/enquetes-menages/integration-metallica/-/raw/master/Simpsons/V1/ddi-simpsons-v1.xml                 |
    |120                     |FAVOURITE_CHAR       |https://gitlab.insee.fr/enquetes-menages/integration-metallica/-/raw/master/Simpsons/V2/ddi-simpsons-v2.xml                 |
    |48                      |BOUCLEINDIV          |https://gitlab.insee.fr/enquetes-menages/integration-metallica/-/raw/master/VQS/vqs-2021-x00/web/vqs-2021-x00-xforms-ddi.xml|
    |486                     |BOUCLE_PRENOMS       |https://gitlab.insee.fr/enquetes-menages/integration-metallica/-/raw/master/Logement/LOG2021T01/S1logement13juil_ddi.xml    |
    