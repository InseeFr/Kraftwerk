package fr.insee.kraftwerk.core.vtl;

import fr.insee.kraftwerk.core.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class VtlChecker {

    private VtlChecker(){}

    public static String fixVtlExpression(String vtlExpression, String bindingName, VtlBindings vtlBindings) {
        String identifiers = StringUtils.join(vtlBindings.getDatasetIdentifierNames(bindingName), ", ");

        vtlExpression = fixCurrentDate(vtlExpression);
        vtlExpression = fixFirstValue(vtlExpression, identifiers);
        vtlExpression = fixSum(vtlExpression, identifiers);

        return vtlExpression;
    }

    private static @NotNull String fixFirstValue(String vtlExpression, String identifiers) {
        String identifiersWithoutIdUE = identifiers.replaceFirst("IdUE, ", "");
        vtlExpression = vtlExpression.replace("over()", String.format("over(PARTITION BY IdUE order by (%s))", identifiersWithoutIdUE));
        return vtlExpression;
    }

    private static @NotNull String fixSum(String input, String groupByParam) {
        if (!input.contains("sum(") && !input.contains("SUM(")){return input;}
        StringBuilder result = new StringBuilder();
        int index = 0;

        while (index < input.length()) {
            int sumIndex = findNextSumIndex(input, index);
            int closingParenthesisIndex = findClosingParenthesisIndex(input, sumIndex + 3);

            if (sumIndex == -1) { //no more sum
                result.append(input.substring(index));
            }

            if (sumIndex != -1 && closingParenthesisIndex == -1) {
                log.warn("Missing closing parenthesis in VTL expression : {}", input);
                result.append(input.substring(sumIndex));  // Incomplete sum function, just append the rest
            }

            if (sumIndex == -1 ||  closingParenthesisIndex == -1){
                break;
            }

            result.append(input, index, sumIndex);
            result.append(input, sumIndex, closingParenthesisIndex );
            result.append(" group by ").append(groupByParam).append(" )");
            index = closingParenthesisIndex + 1;
        }

        return result.toString();
    }

    private static int findNextSumIndex(String input, int fromIndex) {
        String lowerInput = input.toLowerCase();
        int sumIndex = lowerInput.indexOf("sum(", fromIndex);
        while (sumIndex != -1 && !isSumFunction(input, sumIndex)) {
            sumIndex = lowerInput.indexOf("sum(", sumIndex + 4); //4 is char number of "sum("
        }
        return sumIndex;
    }

    private static boolean isSumFunction(String input, int index) {
        return index == 0 || !Character.isLetterOrDigit(input.charAt(index - 1));
    }

    private static int findClosingParenthesisIndex(String input, int openIndex) {
        int depth = 1;
        for (int i = openIndex + 1; i < input.length(); i++) {
            if (input.charAt(i) == '(') {
                depth++;
            } else if (input.charAt(i) == ')') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;  // No closing parenthesis found
    }

    private static @NotNull String fixCurrentDate(String vtlExpression) {
        vtlExpression = vtlExpression.replace("CURRENT_DATE()", Constants.OUTCOME_DATE);
        vtlExpression = vtlExpression.replace("current_date()", Constants.OUTCOME_DATE);
        vtlExpression = vtlExpression.replace("CURRENT_DATE", Constants.OUTCOME_DATE);
        vtlExpression = vtlExpression.replace("current_date", Constants.OUTCOME_DATE);

        return vtlExpression;
    }


}
