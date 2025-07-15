package fr.insee.kraftwerk.core.utils.files;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileSystemTypeTest {

    private static Stream<Arguments> fileSystemTypeParameterizedTests() {
        return Stream.of(
                Arguments.of(0, FileSystemType.OS_FILESYSTEM),
                Arguments.of(1, FileSystemType.MINIO)
        );
    }


    @ParameterizedTest
    @MethodSource("fileSystemTypeParameterizedTests")
    void fileSystemType_ParameterizedTests(int index, FileSystemType expectedResult) {
        FileSystemType[] values = FileSystemType.values();
        assertEquals(expectedResult, values[index]);
    }

}
