package fr.insee.kraftwerk.api;

import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import org.jetbrains.annotations.NotNull;

/**
 * Class to create static variables giving path for test resources.
 */
public class TestConstants {

	public static final String TEST_RESOURCES_DIRECTORY = "src/test/resources";

	//Unit tests
	public static final String UNIT_TESTS_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/unit_tests";
	public static final String UNIT_TESTS_DUMP = TEST_RESOURCES_DIRECTORY + "/unit_tests/out";

	@NotNull
	public static KraftwerkExecutionContext getKraftwerkExecutionContext() {
		return new KraftwerkExecutionContext(
				null,
				false,
				true,
				false,
				419430400L
		);
	}

}
