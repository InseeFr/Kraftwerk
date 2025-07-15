package fr.insee.kraftwerk.api.batch;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KraftwerkServiceTypeTest {

    private static Stream<Arguments> kraftwerkServiceTypeParameterizedTests() {
        return Stream.of(
                Arguments.of(0, KraftwerkServiceType.MAIN),
                Arguments.of(1, KraftwerkServiceType.LUNATIC_ONLY),
                Arguments.of(2, KraftwerkServiceType.GENESIS),
                Arguments.of(3, KraftwerkServiceType.GENESISV2),
                Arguments.of(4, KraftwerkServiceType.FILE_BY_FILE)
        );
    }


    @ParameterizedTest
    @MethodSource("kraftwerkServiceTypeParameterizedTests")
    void kraftwerkServiceType_ParameterizedTests(int index, KraftwerkServiceType expectedResult) {
        KraftwerkServiceType[] values = KraftwerkServiceType.values();
        assertEquals(expectedResult, values[index]);
    }

}
