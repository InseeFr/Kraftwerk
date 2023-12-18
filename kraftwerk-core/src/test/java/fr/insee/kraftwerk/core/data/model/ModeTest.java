package fr.insee.kraftwerk.core.data.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModeTest {

	@ParameterizedTest
	@MethodSource("inputsAndResults")
	void getEnumFromModeNameTest(String input, String expectedResult){
		Mode mode = Mode.getEnumFromModeName(input);
		assertEquals(mode.getModeName(),input);
		assertEquals(mode.getFolder(),expectedResult);
	}

	private static Stream<Arguments> inputsAndResults() {
		return Stream.of(
				Arguments.of("WEB", "WEB"),
				Arguments.of("TEL", "ENQ"),
				Arguments.of("F2F", "ENQ"),
				Arguments.of("OTHER", ""),
				Arguments.of("PAPER", "")
		);
	}
}
