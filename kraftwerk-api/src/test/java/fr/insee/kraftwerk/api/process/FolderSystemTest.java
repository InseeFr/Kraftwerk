package fr.insee.kraftwerk.api.process;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FolderSystemTest {

    private static Stream<Arguments> folderSystemParameterizedTests() {
        return Stream.of(
                Arguments.of(0, FolderSystem.MAIN),
                Arguments.of(1, FolderSystem.GENESIS)
        );
    }


    @ParameterizedTest
    @MethodSource("folderSystemParameterizedTests")
    void folderSystem_ParameterizedTests(int index, FolderSystem expectedResult) {
        FolderSystem[] values = FolderSystem.values();
        assertEquals(expectedResult, values[index]);
    }

}
