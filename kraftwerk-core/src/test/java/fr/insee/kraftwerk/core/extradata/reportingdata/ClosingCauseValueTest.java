package fr.insee.kraftwerk.core.extradata.reportingdata;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClosingCauseValueTest {

    private static Stream<Arguments> closingCauseValueParameterizedTests() {
        return Stream.of(
                Arguments.of(0, ClosingCauseValue.NPA),
                Arguments.of(1, ClosingCauseValue.NPI),
                Arguments.of(2, ClosingCauseValue.NPX),
                Arguments.of(3, ClosingCauseValue.ROW)
        );
    }


    @ParameterizedTest
    @MethodSource("closingCauseValueParameterizedTests")
    void closingCauseValue_ParameterizedTests(int index, ClosingCauseValue expectedResult) {
        ClosingCauseValue[] values = ClosingCauseValue.values();
        assertEquals(expectedResult, values[index]);
    }

}
