package fr.insee.kraftwerk.core.vtl;

import fr.insee.kraftwerk.core.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class VtlChecker {

    private VtlChecker(){}

    private static boolean aggr = false;

    public static String fixVtlExpression(String vtlExpression,String calculatedName, String bindingName, VtlBindings vtlBindings) {
        String identifiers = StringUtils.join(vtlBindings.getDatasetIdentifierNames(bindingName), ", ");

        aggr = false ;

        vtlExpression = fixCurrentDate(vtlExpression);
        vtlExpression = fixFirstValue(vtlExpression, identifiers);
        vtlExpression = fixSum(vtlExpression, identifiers);
        vtlExpression = fixMin(vtlExpression, identifiers);
        vtlExpression = fixMax(vtlExpression, identifiers);

        if (aggr) {
            vtlExpression = String.format("%s_aggr := %1$s [aggr %s := %s]; " +
                            "%1$s := join (%1$s, %1$s_aggr);",
                    bindingName, calculatedName, vtlExpression);
        }else {
            vtlExpression = String.format("%s := %1$s [calc %s := %s];", bindingName, calculatedName, vtlExpression);
        }

        return vtlExpression;
    }

    private static @NotNull String fixFirstValue(String vtlExpression, String identifiers) {
        String identifiersWithoutIdUE = identifiers.replaceFirst("IdUE, ", "");
        vtlExpression = vtlExpression.replace("over()", String.format("over(PARTITION BY IdUE order by (%s))", identifiersWithoutIdUE));
        return vtlExpression;
    }

    private static @NotNull String fixSum(String input, String groupByParam) {
        if (!input.toLowerCase().contains("sum(")){return input;}
        return addMissingGroupBy(input, groupByParam, "sum");
    }

    private static @NotNull String fixMin(String input, String groupByParam) {
        if (!input.toLowerCase().contains("min(")){return input;}
        return addMissingGroupBy(input, groupByParam, "min");
    }

    private static @NotNull String fixMax(String input, String groupByParam) {
        if (!input.toLowerCase().contains("max(")){return input;}
        return addMissingGroupBy(input, groupByParam, "max");
    }

    private static @NotNull String addMissingGroupBy(String input, String groupByParam, String functionToFind) {
        aggr = true;
        StringBuilder result = new StringBuilder();
        int index = 0;

        while (index < input.length()) {
            int functionIndex = findNextFunctionIndex(input, index, functionToFind);
            int closingParenthesisIndex = findClosingParenthesisIndex(input, functionIndex + 3);

            if (functionIndex == -1) { //no more sum
                result.append(input.substring(index));
            }

            if (functionIndex != -1 && closingParenthesisIndex == -1) {
                log.warn("Missing closing parenthesis in VTL expression : {}", input);
                result.append(input.substring(functionIndex));  // Incomplete function, just append the rest
            }

            if (functionIndex == -1 ||  closingParenthesisIndex == -1){
                break;
            }

            result.append(input, index, functionIndex);
            result.append(input, functionIndex, closingParenthesisIndex );
            result.append(" group by ").append(groupByParam).append(" )");
            index = closingParenthesisIndex + 1;
        }

        return result.toString();
    }

    private static int findNextFunctionIndex(String input, int fromIndex, String functionToFind) {
        String lowerInput = input.toLowerCase();
        String withParenthesis = functionToFind.toLowerCase() + "(";
        int functionIndex = lowerInput.indexOf(withParenthesis, fromIndex);
        while (functionIndex != -1 && !isFunctionToFind(input, functionIndex)) {
            functionIndex = lowerInput.indexOf(withParenthesis, functionIndex + 4); //4 is char number of "sum("
        }
        return functionIndex;
    }

    private static boolean isFunctionToFind(String input, int index) {
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
