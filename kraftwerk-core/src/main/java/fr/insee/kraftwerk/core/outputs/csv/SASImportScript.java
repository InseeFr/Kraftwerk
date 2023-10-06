package fr.insee.kraftwerk.core.outputs.csv;

import java.util.List;
import java.util.Map;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.metadata.ErrorVariableLength;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.outputs.ImportScript;
import fr.insee.kraftwerk.core.outputs.TableScriptInfo;

public class SASImportScript extends ImportScript {

    private List<KraftwerkError> errors;

    public SASImportScript(List<TableScriptInfo> tableScriptInfoList, List<KraftwerkError> errors) {
        super(tableScriptInfoList);
        this.errors = errors;
    }

    @Override
    public String generateScript() {
        StringBuilder script = new StringBuilder();
        script.append("/****************************************************/ ").append(END_LINE);
        script.append("/*****  Automated import for Kraftwerk outputs  *****/ ").append(END_LINE);
        script.append("/****************************************************/ ").append(END_LINE);
        script.append("%%let path = local_folder; ").append(END_LINE).append(END_LINE);

        for (TableScriptInfo tableScriptInfo : tableScriptInfoList) {
            String tableName = tableScriptInfo.getTableName();
            // Shorten table name due to SAS restrictions
            String shortenTableName = tableName.substring(0, Math.min(tableName.length(), 6));

            // filename reference to the file
            script.append(String.format("filename %s \"&path\\%s\" ENCODING=\"UTF-8\" ;",
                    shortenTableName, tableScriptInfo.getFileName())).append(END_LINE);

            // PROC IMPORT
            // careful about special characters, which can't be imported in SAS
            script.append(String.format("data %s; ", tableName)).append(END_LINE);
            script.append("%let _EFIERR_ = 0; /* set the ERROR detection macro variable */ ").append(END_LINE);
            script.append(String.format("infile %s delimiter=\"%s\" MISSOVER DSD lrecl=13106 firstobs=2;",
                    shortenTableName, Constants.CSV_OUTPUTS_SEPARATOR)).append(END_LINE).append(END_LINE);

            // Special treatment to display the length of the variables
            Map<String, VariablesMap> metadataVariables = tableScriptInfo.getMetadataVariables();
            Map<String, Variable> listVariables = getAllLength(tableScriptInfo.getDataStructure(), metadataVariables);

            // Warning about possible problem with format of variables from suggester
            script.append("    /* Warning : the actual length of these variables may be superior than the format specified in this script").append(END_LINE);
            for (KraftwerkError error : errors){
                if (error instanceof ErrorVariableLength errorVarLength){
                    script.append("        ").append(errorVarLength.getVariable().getName()).append(END_LINE);
                }
            }
            script.append("       These variables may be truncated at import in SAS*/").append(END_LINE).append(END_LINE);

            // SAS restriction: variables cannot be more than 32 bytes long
            longNameWarnings(listVariables, script);

            script.append(scriptSASPart1(listVariables));

            script.append(scriptSASPart2(listVariables));

            script.append(scriptSASPart3(listVariables));

            script.append("if _ERROR_ then call symputx('_EFIERR_',1);\n");
            script.append("run;\n");

            script.append("\n\n");
        }

        return script.toString();
    }

    /**
     * Add warning comments in script for variable names that are longer than 32 bytes (assuming UTF-8 encoding).
     * This method could have been implemented to do automatic truncation, but seems to be a bad idea:
     * cases where truncation would be needed would frequently be on variables with same prefix or suffix.
     * @param listVariable Map to be modified.
     */
    private void longNameWarnings(Map<String, Variable> listVariable, StringBuilder script) {
        List<String> longNames = listVariable.entrySet().stream().
                map(entry -> {
                    // getAllLength method is too complex
                    // Asserts here to make sure it work as expected
                    String name = entry.getKey();
                    Variable variable = entry.getValue();
                    assert name != null;
                    assert name.equals(variable.getName());
                    //
                    return name;
                })
                .filter(name -> name.length() > 32)
                .toList();
        if (! longNames.isEmpty()) {
            script.append(END_LINE);
            script.append("    /* WARNING: Following variable names are more than 32 characters long:");
            script.append(END_LINE);
            longNames.forEach(name -> script.append("        ").append(name).append(END_LINE));
            script.append("       These variables have to be shorten, otherwise SAS import will fail. */");
            script.append(END_LINE).append(END_LINE);
        }
    }

    /**
     * Put informats for the first step.
     * Example: Put informats informat MAA2AT $1. ; informat ANNEENQ best32. ;
     *
     * @param listVariables a list of the variables with their format and length
     * @return a script with the informat instructions
     */
    private String scriptSASPart1(Map<String, Variable> listVariables) {
        StringBuilder script = new StringBuilder();
        for (Map.Entry<String, Variable> varEntry : listVariables.entrySet()) {
            Variable variable = varEntry.getValue();
            String sasFormat = variable.getSasFormat();
            if (!sasFormat.contentEquals("0")) {
                // We write the format instructions if we have information on variables length
                if (variable.getType().equals(VariableType.BOOLEAN)) {
                    script.append(String.format("informat %s $1. ;", varEntry.getKey())).append(END_LINE);
                } else if (variable.getType().equals(VariableType.STRING)
                        || variable.getType().equals(VariableType.DATE)) {
                    script.append(String.format("informat %s $%s. ;", varEntry.getKey(), sasFormat)).append(END_LINE);
                } else if (variable.getType().equals(VariableType.INTEGER)
                        || variable.getType().equals(VariableType.NUMBER)) {
                    script.append(String.format("informat %s %s ;", varEntry.getKey(), sasFormat)).append(END_LINE);
                }
            } else {
                script.append(String.format("informat %s $32. ;", varEntry.getKey())).append(END_LINE);
            }
        }
        script.append("\n");
        return script.toString();
    }

    /**
     * Put formats for the second step.
     * Example: format MAA2AT $1. ; format ANNEENQ best32. ;
     *
     * @param listVariables a list of the variables with their format and length
     * @return a script with the format instructions
     */
    private String scriptSASPart2(Map<String, Variable> listVariables) {
        StringBuilder script = new StringBuilder();
        for (Map.Entry<String, Variable> varEntry : listVariables.entrySet()) {
            Variable variable = varEntry.getValue();
            String sasFormat = variable.getSasFormat();
            if (!sasFormat.contentEquals("0")) {
                // We write the format instructions if we have information on variables length
                if (variable.getType().equals(VariableType.BOOLEAN) || variable.getType().equals(VariableType.STRING)
                        || variable.getType().equals(VariableType.DATE)) {
                    script.append(String.format("format %s $%s. ;", varEntry.getKey(), sasFormat)).append(END_LINE);
                } else if (variable.getType().equals(VariableType.INTEGER)
                        || variable.getType().equals(VariableType.NUMBER)) {
                    script.append(String.format("format %s %s ;", varEntry.getKey(), sasFormat)).append(END_LINE);
                }
            } else {
                script.append(String.format("format %s $32. ;", varEntry.getKey())).append(END_LINE);
            }
        }
        script.append("\n");
        return script.toString();
    }

    /**
     * Put the input formats for the third step.
     * Example: input IdUE $ ADRESSE $ NHAB MAA2AT $;
     *
     * @param listVariables a list of the variables with their format and length
     * @return a script with the input instructions
     */
    private String scriptSASPart3(Map<String, Variable> listVariables) {
        StringBuilder script = new StringBuilder();
        script.append("input ").append(END_LINE);
        for (Map.Entry<String,Variable> variableEntry : listVariables.entrySet()) {
            Variable variable = variableEntry.getValue();
            // We write the format instructions if we have information on variables length
            if (variable.getType().equals(VariableType.BOOLEAN) || variable.getType().equals(VariableType.STRING)
                    || variable.getType().equals(VariableType.DATE)) {
                script.append(String.format("%s $ ", variableEntry.getKey())).append(END_LINE);
            } else if (variable.getType().equals(VariableType.INTEGER)
                    || variable.getType().equals(VariableType.NUMBER)) {

                script.append(String.format("%s ", variableEntry.getKey())).append(END_LINE);
            }
        }
        script.append(";").append(END_LINE);
        return script.toString();
    }

}
