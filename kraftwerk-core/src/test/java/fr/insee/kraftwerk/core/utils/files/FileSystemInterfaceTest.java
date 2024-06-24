package fr.insee.kraftwerk.core.utils.files;

import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileSystemInterfaceTest {
    @Test
    void testTransformToOut() {
        assertEquals(Paths.get("C://Users/in/Kraftwerk/src/test/resources/functional_tests/out/VQS"),
				FileUtilsInterface.transformToOut(Paths.get("C://Users/in/Kraftwerk/src/test/resources/functional_tests/in/VQS")));
    }

	@Test
	void testTransformToOut2() {
		Path path = Paths.get("C://Users/in/Kraftwerk/src/test/resources/functional_tests/out/VQS");
		assertEquals(path, FileUtilsInterface.transformToOut(Paths.get("C://Users/in/Kraftwerk/src/test/resources/functional_tests/in/VQS"), LocalDateTime.now()).getParent());
	}

	@Test
	void convertToPathTest_nullUserField() throws KraftwerkException {
		Assertions.assertThat(FileUtilsInterface.convertToPath(null,null)).isNull();
	}

	@Test
	void convertToPathTest_directoryNotExists(){
		Assert.assertThrows(KraftwerkException.class, () -> FileUtilsInterface.convertToPath("test", Path.of("NOT SUPPOSED TO EXIST")));
	}

	@Test
	void convertToPathTest() throws KraftwerkException {
		//GIVEN
		String campaignName = "convert_path";
		Path inputDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName);

		//WHEN+THEN
		Assertions.assertThat(FileUtilsInterface.convertToPath("test.txt", inputDirectory)).exists();
	}

	@Test
	void convertToURLTest_nullUserField(){
		Assertions.assertThat(FileUtilsInterface.convertToUrl(null,null)).isNull();
	}

	@Test
	void convertToURLTest(){
		//GIVEN
		String campaignName = "convert_path";
		Path inputDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName);

		//WHEN+THEN
		Assertions.assertThat(FileUtilsInterface.convertToUrl("test.txt", inputDirectory).getFile()).endsWith("test.txt");
	}

}

