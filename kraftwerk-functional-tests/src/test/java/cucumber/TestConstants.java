package cucumber;

import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import org.jetbrains.annotations.NotNull;

/**
 * Class to create static variables giving path for test resources.
 */
public class TestConstants {

	public static final String TEST_RESOURCES_DIRECTORY = "src/test/resources";

	//Functional tests
    public static final String FUNCTIONAL_TESTS_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/functional_tests";
	public static final String FUNCTIONAL_TESTS_INPUT_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/functional_tests/in";
	public static final String FUNCTIONAL_TESTS_TEMP_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/functional_tests/temp";
	public static final String FUNCTIONAL_TESTS_OUTPUT_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/functional_tests/out";

  // ----- Sample test files (Reporting data)
  public static final String SAMPLETEST_REPORTINGDATA_CAMPAIGN_NAME = "SAMPLETEST-REPORTINGDATA-v1";
  public static final String SAMPLETEST_REPORTINGDATA_FOLDER = FUNCTIONAL_TESTS_INPUT_DIRECTORY + "/" + SAMPLETEST_REPORTINGDATA_CAMPAIGN_NAME;
  public static final String SAMPLETEST_REPORTINGDATA_DDI = SAMPLETEST_REPORTINGDATA_FOLDER + "/ddi-sampletest-v1-tel.xml";
  public static final String SAMPLETEST_REPORTINGDATA_DDI_URL = "TODO";
  public static final String SAMPLETEST_REPORTINGDATA_LUNATIC_JSON = SAMPLETEST_REPORTINGDATA_FOLDER + "/lunatic/sampletest_tel.json";


    @NotNull
    public static KraftwerkExecutionContext getKraftwerkExecutionContext() {
        return new KraftwerkExecutionContext(
                null,
                false,
                true,
                true,
                false,
                419430400L
        );
    }

    @NotNull
    public static KraftwerkExecutionContext getKraftwerkExecutionContext(String inDirectory, boolean withEncryption) {
        return new KraftwerkExecutionContext(
                inDirectory,
                false,
                true,
                true,
                withEncryption,
                419430400L
        );
    }

}