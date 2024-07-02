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

}

