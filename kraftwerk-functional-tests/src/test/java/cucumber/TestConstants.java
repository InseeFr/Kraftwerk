package cucumber;

/**
 * Class to create static variables giving path for test resources.
 */
public class TestConstants {

	public static final String TEST_RESOURCES_DIRECTORY = "src/test/resources";

	//Functional tests
	public static final String FUNCTIONAL_TESTS_INPUT_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/functional_tests/in";
	public static final String FUNCTIONAL_TESTS_TEMP_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/functional_tests/temp";
	public static final String FUNCTIONAL_TESTS_OUTPUT_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/functional_tests/out";

		// ----- Sample test files (Complete)
		//TODO add complete test files constants

		// ----- Sample test files (Paradata)
		//TODO add paradata test files constants

		// ----- Sample test files (Reporting data)
		public static final String SAMPLETEST_REPORTINGDATA_CAMPAIGN_NAME = "SAMPLETEST-REPORTINGDATA-v1";
		public static final String SAMPLETEST_REPORTINGDATA_FOLDER = FUNCTIONAL_TESTS_INPUT_DIRECTORY + "/" + SAMPLETEST_REPORTINGDATA_CAMPAIGN_NAME;
		public static final String SAMPLETEST_REPORTINGDATA_DDI = SAMPLETEST_REPORTINGDATA_FOLDER + "/ddi-sampletest-v1-tel.xml";
		public static final String SAMPLETEST_REPORTINGDATA_DDI_URL = "TODO";
		public static final String SAMPLETEST_REPORTINGDATA_LUNATIC_JSON = SAMPLETEST_REPORTINGDATA_FOLDER + "/lunatic/sampletest_tel.json";

		// ----- Sample test files (Data only)
		//TODO add data only test files constants
}
