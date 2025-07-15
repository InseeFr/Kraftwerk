package fr.insee.kraftwerk.core.dataprocessing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StepEnumTest {

    private static Stream<Arguments> fileSystemTypeParameterizedTests() {
        return Stream.of(
                Arguments.of(0, 1, "BUILD_BINDINGS"),
                Arguments.of(1, 2, "UNIMODAL_PROCESSING"),
                Arguments.of(2, 3, "MULTIMODAL_PROCESSING")
        );
    }


    @ParameterizedTest
    @MethodSource("fileSystemTypeParameterizedTests")
    void fileSystemType_ParameterizedTests(int index, int expectedIndex, String expectedLabel) {
        StepEnum[] values = StepEnum.values();
        StepEnum stepEnum = values[index];
        assertEquals(expectedIndex, stepEnum.getStepNumber());
        assertEquals(expectedLabel, stepEnum.getStepLabel());
    }

}
