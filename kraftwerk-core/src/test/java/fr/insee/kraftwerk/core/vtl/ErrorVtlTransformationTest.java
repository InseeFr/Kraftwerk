package fr.insee.kraftwerk.core.vtl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorVtlTransformationTest {

    @Test
    void errorVtlTransformation_Test() {
        String vtlScript = "myVtlStript1234.vtl";
        String message = "myMessage";
        ErrorVtlTransformation errorVtlTransformation = new ErrorVtlTransformation(vtlScript, message);
        String expectedResult = "VTL Transformation error detected on :" + "\n" +
                                "Script='" + vtlScript + '\'' + "\n" +
                                "Message='" + message + '\'' + "\n";
        assertEquals(expectedResult, errorVtlTransformation.toString());
    }

    /*
    //NOTE : This test is commented because equality test on self object generates sonar issue!
    @Test
    void errorVtlTransformation_self_Tests() {
        ErrorVtlTransformation errorVtlTransformation = new ErrorVtlTransformation("a", "c");
        assertEquals(errorVtlTransformation, errorVtlTransformation);
    }
    */


    private static Stream<Arguments> errorVtlTransformationEqualsParameterizedTests() {
        return Stream.of(
                Arguments.of(new ErrorVtlTransformation("a", "c"), new ErrorVtlTransformation("a", "c"), Boolean.TRUE),
                Arguments.of(new ErrorVtlTransformation("a", "c"), new ErrorVtlTransformation("a", "b"), Boolean.FALSE),
                Arguments.of(new ErrorVtlTransformation("a", "c"), new ErrorVtlTransformation("b", "c"), Boolean.FALSE),
                Arguments.of(new ErrorVtlTransformation("a", "c"), null, Boolean.FALSE),
                Arguments.of(new ErrorVtlTransformation("a", "c"), "xxx", Boolean.FALSE),
                Arguments.of(new ErrorVtlTransformation("a", "c"), new ErrorVtlTransformation("b", "c"), Boolean.FALSE)
        );
    }


    @ParameterizedTest
    @MethodSource("errorVtlTransformationEqualsParameterizedTests")
    void errorVtlTransformation_ParameterizedTests(Object o1, Object o2, boolean expectedResult) {
        assertEquals(expectedResult, o1.equals(o2));
    }

    @Test
    void errorVtlTransformation_hashCode_Tests() {
        ErrorVtlTransformation errorVtlTransformation = new ErrorVtlTransformation("a", "c");
        assertEquals(4067, errorVtlTransformation.hashCode());
    }


}
