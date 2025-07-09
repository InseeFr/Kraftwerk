package fr.insee.kraftwerk.core.outputs.csv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CsvRegexHelperTest {

    private static final String REGEX_1_COLUMN = CsvRegexHelper.REGULAR_EXPRESSION;  //!!!WARNING!!! potential bug when there is ONLY ONE NOT-EMPTY COLUMN!
    private static final String REGEX_2_COLUMNS = CsvRegexHelper.REGULAR_EXPRESSION + ";" + CsvRegexHelper.REGULAR_EXPRESSION;;
    private static final String REGEX_REPLACEMENT_2_COLUMNS = "\"$1\";\"$2\"";
    private static final String REGEX_3_COLUMNS = CsvRegexHelper.REGULAR_EXPRESSION + ";" +
                                              CsvRegexHelper.REGULAR_EXPRESSION + ";" +
                                              CsvRegexHelper.REGULAR_EXPRESSION;
    private static final String REGEX_REPLACEMENT_3_COLUMNS = "\"$1\";\"$2\";\"$3\"";
    private static final String REGEX_BOOL_3_COLUMNS = CsvRegexHelper.REGULAR_EXPRESSION + ";" +
                                                    CsvRegexHelper.REGULAR_EXPRESSION + ";" +
                                                    CsvRegexHelper.REGULAR_EXPRESSION;
    private static final String REGEX_BOOL_REPLACEMENT_3_COLUMNS = "\"$1\";\"¤¤¤$2¤¤¤\";\"$3\"";


    private static Stream<Arguments> applyRegExParameterizedTests() {
        return Stream.of(
                Arguments.of(REGEX_1_COLUMN, "\"$1\"", false, "", "\"\""),
                //Arguments.of(REGEX_1_COLUMN, "\"$1\"", false, "\"aaa\"", "\"aaa\""), //!!!WARNING!!! potential bug when there is ONLY ONE NOT-EMPTY COLUMN!
                // //Arguments.of(REGEX_1_COLUMN, "\"$1\"", false, "aaa", "\"aaa\""), //!!!WARNING!!! potential bug when there is ONLY ONE NOT-EMPTY COLUMN!
                Arguments.of(REGEX_2_COLUMNS, REGEX_REPLACEMENT_2_COLUMNS, false, "aaa;bbb", "\"aaa\";\"bbb\""),
                Arguments.of(REGEX_2_COLUMNS, REGEX_REPLACEMENT_2_COLUMNS, false, "\"aaa\";\"bbb\"", "\"aaa\";\"bbb\""),
                Arguments.of(REGEX_3_COLUMNS, REGEX_REPLACEMENT_3_COLUMNS, false, "aaa;bbb;ccc", "\"aaa\";\"bbb\";\"ccc\""),
                Arguments.of(REGEX_3_COLUMNS, REGEX_REPLACEMENT_3_COLUMNS, false, "\"aaa\";\"bbb\";\"ccc\"", "\"aaa\";\"bbb\";\"ccc\""),
                Arguments.of(REGEX_BOOL_3_COLUMNS, REGEX_BOOL_REPLACEMENT_3_COLUMNS, true, "aaa;;bbb", "\"aaa\";\"\";\"bbb\""),
                Arguments.of(REGEX_BOOL_3_COLUMNS, REGEX_BOOL_REPLACEMENT_3_COLUMNS, true, "\"aaa\";\"\";\"bbb\"", "\"aaa\";\"\";\"bbb\""),
                Arguments.of(REGEX_BOOL_3_COLUMNS, REGEX_BOOL_REPLACEMENT_3_COLUMNS, true, "aaa;true;bbb", "\"aaa\";\"1\";\"bbb\""),
                Arguments.of(REGEX_BOOL_3_COLUMNS, REGEX_BOOL_REPLACEMENT_3_COLUMNS, true, "\"aaa\";\"true\";\"bbb\"", "\"aaa\";\"1\";\"bbb\""),
                Arguments.of(REGEX_BOOL_3_COLUMNS, REGEX_BOOL_REPLACEMENT_3_COLUMNS, true, "aaa;false;bbb", "\"aaa\";\"0\";\"bbb\""),
                Arguments.of(REGEX_BOOL_3_COLUMNS, REGEX_BOOL_REPLACEMENT_3_COLUMNS, true, "\"aaa\";\"false\";\"bbb\"", "\"aaa\";\"0\";\"bbb\"")
        );
    }

    @ParameterizedTest
    @MethodSource("applyRegExParameterizedTests")
    void applyRegExOnBlockFile_test(String regExToFind, String regExReplacement, boolean boolColumns, String input, String expectedResult) {
        StringBuilder sbInput = new StringBuilder();
        sbInput.append(input);
        List<String> boolColumnNames = new ArrayList<>();
        if(boolColumns) {
            boolColumnNames.add("col_B");
        }
        String result = CsvRegexHelper.applyRegExOnBlockFile_unitTests(sbInput, regExToFind, regExReplacement, boolColumnNames);
        assertEquals(expectedResult, result);
    }


    private static Stream<Arguments> regExPatternsParameterizedTests() {
        return Stream.of(
                Arguments.of(2, 0, REGEX_2_COLUMNS, REGEX_REPLACEMENT_2_COLUMNS),
                Arguments.of(3, 0, REGEX_3_COLUMNS, REGEX_REPLACEMENT_3_COLUMNS),
                Arguments.of(3, 1, REGEX_BOOL_3_COLUMNS, REGEX_BOOL_REPLACEMENT_3_COLUMNS)
        );
    }

    @ParameterizedTest
    @MethodSource("regExPatternsParameterizedTests")
    void regExPatternsOnBlockFile_test(int nbColumns, int nbBoolColumns, String regExToFind, String regExReplacement) {
        List<String> columnNames = new ArrayList<>();
        for (int i = 0; i < nbColumns; i++) {
            columnNames.add("col_" + i);
        }
        List<String> boolColumnNames = new ArrayList<>();
        for (int i = 0; i < nbBoolColumns; i++) {
            boolColumnNames.add("col_" + i);
        }
        List<Integer> boolColumnIndexes = new ArrayList<>();
        if(nbBoolColumns > 0) {
            boolColumnIndexes.add(1);
        }

        String regExPatternToFind = CsvRegexHelper.buildRegExPatternToFind_unitTests(columnNames);
        String regExPatternReplacement = CsvRegexHelper.buildRegExPatternReplacement_unitTests(columnNames, boolColumnNames, boolColumnIndexes);
        assertEquals(regExToFind, regExPatternToFind);
        assertEquals(regExReplacement, regExPatternReplacement);
    }

}
