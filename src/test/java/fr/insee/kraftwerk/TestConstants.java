package fr.insee.kraftwerk;

/**
 * Class to create static variables giving path for test resources.
 */
public class TestConstants {

	public static final String TEST_RESOURCES_DIRECTORY = "src/test/resources";
	public static final String TEST_FUNCTIONAL_INPUT_DIRECTORY = TestConstants.TEST_RESOURCES_DIRECTORY + "/functional_tests/in";
	public static final String TEST_FUNCTIONAL_OUTPUT_DIRECTORY = TestConstants.TEST_RESOURCES_DIRECTORY + "/functional_tests/out";
	public static final String TEST_UNIT_INPUT_DIRECTORY = TestConstants.TEST_RESOURCES_DIRECTORY + "/unit_tests";
	public static final String TEST_UNIT_OUTPUT_DIRECTORY = TestConstants.TEST_RESOURCES_DIRECTORY + "/unit_tests/out";

    // ----- Simpsons (V1) test files
    public static final String SIMPSONS_V1_CAMPAIGN = "SIMPSONS-v1";
    public static final String SIMPSONS_V1_FOLDER = TEST_FUNCTIONAL_INPUT_DIRECTORY + "/" + SIMPSONS_V1_CAMPAIGN;
    public static final String SIMPSONS_V1_USER_INPUT = TEST_FUNCTIONAL_INPUT_DIRECTORY + "/kraftwerk_inputs_simpsons1.json";
    public static final String SIMPSONS_V1_DDI = SIMPSONS_V1_FOLDER + "/ddi-simpsons-v1.xml";
    public static final String SIMPSONS_V1_DDI_URL = "https://gitlab.insee.fr/enquetes-menages/integration-metallica/-/raw/master/Simpsons/V1/ddi-simpsons-v1.xml";
    public static final String SIMPSONS_V1_XFORMS_DATA = SIMPSONS_V1_FOLDER + "/xforms/simpsons-v1-xforms-20201217-160729.xml";
    public static final String SIMPSONS_V1_PAPER_DATA = SIMPSONS_V1_FOLDER + "/paper/simpsons-v1-paper-data.csv";

    // ----- Simpsons V2 test files
    public static final String SIMPSONS_V2_CAMPAIGN = "SIMPSONS-v2";
    public static final String SIMPSONS_V2_FOLDER = TEST_FUNCTIONAL_INPUT_DIRECTORY + "/" + SIMPSONS_V2_CAMPAIGN;
    public static final String SIMPSONS_V2_USER_INPUT = TEST_FUNCTIONAL_INPUT_DIRECTORY + "/kraftwerk_inputs_simpsons2.json";
    public static final String SIMPSONS_V2_DDI = SIMPSONS_V2_FOLDER + "/ddi-simpsons-v2.xml";
    public static final String SIMPSONS_V2_DDI_URL = "TODO";
    public static final String SIMPSONS_V2_LUNATIC_JSON = SIMPSONS_V2_FOLDER + "/simpsons-v2-sample.json";

    // ----- VQS 2021 test files (anonymized data)
    public static final String VQS_CAMPAIGN_NAME = "VQS-2021-x00";
    public static final String VQS_FOLDER = TEST_FUNCTIONAL_INPUT_DIRECTORY + "/" + VQS_CAMPAIGN_NAME;
    public static final String VQS_WEB_DDI = VQS_FOLDER + "/web/vqs-2021-x00-xforms-ddi.xml";
    public static final String VQS_PAP_DDI = VQS_FOLDER + "/papier/vqs-2021-x00-fo-ddi.xml";
    public static final String VQS_WEB_DATA = VQS_FOLDER + "/web/vqs-2021-x00_anonymized_sample.xml";
    public static final String VQS_PAP_DATA = VQS_FOLDER + "/papier/VQS_paper_anonymized_sample.txt";

    // ----- TIC 2021 test files (anonymized data)
    /* data is not anonymized yet */

    // LOG 2021 T01
    public static final String LOGT01_CAMPAIGN_NAME = "LOG-2021-t01";
    public static final String LOGT01_FOLDER = TEST_FUNCTIONAL_INPUT_DIRECTORY + "/" + LOGT01_CAMPAIGN_NAME;
    public static final String LOGT01_DDI = LOGT01_FOLDER + "/S1logement13juil_ddi.xml";
    public static final String LOGT01_DATA = LOGT01_FOLDER + "/data.complete.LOG2021T01.20210728162727.xml";
}
