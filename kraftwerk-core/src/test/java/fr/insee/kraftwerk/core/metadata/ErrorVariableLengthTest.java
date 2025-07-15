package fr.insee.kraftwerk.core.metadata;

import fr.insee.bpm.metadata.model.Variable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorVariableLengthTest {

    @Test
    void errorVariableLength_toString_null_Test() {
        ErrorVariableLength errorVariableLength = new ErrorVariableLength(null, null);
        String expectedResult = String.format("Warning : The maximum length read for variable %s (DataMode: %s) exceed expected length", "null", "null") + "\n" +
                String.format("Expected: %s but received: %d", "null", -1) + "\n";
        assertEquals(expectedResult, errorVariableLength.toString());
    }

    @Test
    void errorVariableLength_toString1_Test() {
        String varName = "myVarName";
        Variable var = new Variable(varName, null, null); //instantiation with minimum of parameters
        String dataMode = "dataMode1234";
        ErrorVariableLength errorVariableLength = new ErrorVariableLength(var, dataMode);
        String expectedResult = String.format("Warning : The maximum length read for variable %s (DataMode: %s) exceed expected length", varName, dataMode) + "\n" +
                String.format("Expected: %s but received: %d", 1, 0) + "\n";
        assertEquals(expectedResult, errorVariableLength.toString());
    }

    @Test
    void errorVariableLength_toString2_Test() {
        String varName = "myVarName";
        Variable var = new Variable(varName, null, null, "567");
        var.setMaxLengthData(234);
        String dataMode = "dataMode1234";
        ErrorVariableLength errorVariableLength = new ErrorVariableLength(var, dataMode);
        String expectedResult = String.format("Warning : The maximum length read for variable %s (DataMode: %s) exceed expected length", varName, dataMode) + "\n" +
                String.format("Expected: %s but received: %d", 567, 234) + "\n";
        assertEquals(expectedResult, errorVariableLength.toString());
    }

    @Test
    void errorVariableLength_self_Tests() {
        ErrorVariableLength errorVariableLength = new ErrorVariableLength(null, null);
        assertEquals(errorVariableLength, errorVariableLength);
    }


    private static Stream<Arguments> errorVariableLengthEqualsParameterizedTests() {
        return Stream.of(
                Arguments.of(null, Boolean.FALSE),
                Arguments.of("aaa", Boolean.FALSE),
                Arguments.of(new ErrorVariableLength(new Variable("myVarName900", null, null), "myDatamode555"), Boolean.TRUE),
                Arguments.of(new ErrorVariableLength(new Variable("myVarName900", null, null), "myDatamode444"), Boolean.FALSE),
                Arguments.of(new ErrorVariableLength(new Variable("myVarName800", null, null), "myDatamode555"), Boolean.FALSE),
                Arguments.of(new ErrorVariableLength(new Variable("myVarName800", null, null), "myDatamode444"), Boolean.FALSE)
        );
    }


    @ParameterizedTest
    @MethodSource("errorVariableLengthEqualsParameterizedTests")
    void errorVariableLength_ParameterizedTests(Object comparedObject, boolean expectedResult) {
        ErrorVariableLength errorVariableLength = new ErrorVariableLength(new Variable("myVarName900", null, null), "myDatamode555");
        assertEquals(expectedResult, errorVariableLength.equals(comparedObject));
    }

    @Test
    void errorVariableLength_hashCode_Tests() {
        ErrorVariableLength errorVariableLength = new ErrorVariableLength(null, null );
        assertEquals(961, errorVariableLength.hashCode());
    }

}
