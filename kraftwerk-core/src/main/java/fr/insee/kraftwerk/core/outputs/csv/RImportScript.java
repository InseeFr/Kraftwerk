package fr.insee.kraftwerk.core.outputs.csv;

import java.util.List;
import java.util.Map;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.outputs.ImportScript;
import fr.insee.kraftwerk.core.outputs.TableScriptInfo;

/**
 * Implementation of ImportScript to generate R script using the <code>data.table</code> package.
 * */
public class RImportScript extends ImportScript {

    public RImportScript(List<TableScriptInfo> tableScriptInfoList) {
        super(tableScriptInfoList);
    }

    @Override
    public String generateScript() {
        StringBuilder script = new StringBuilder();

        for (TableScriptInfo tableScriptInfo : tableScriptInfoList) {
            String tableName = tableScriptInfo.getTableName();
            // function call
            script.append(String.format("%s <- data.table::fread(", tableName)).append(END_LINE);

            // file, sep and header parameter
            script.append(String.format("file=\"%s\", ", tableScriptInfo.getFileName())).append(END_LINE);
            script.append(String.format("sep=\"%s\", ", Constants.CSV_OUTPUTS_SEPARATOR)).append(END_LINE);
            script.append("encoding = \"UTF-8\", ").append(END_LINE);

            script.append("header=TRUE, ").append(END_LINE);
            // quote parameter
            if (Constants.getCsvOutputQuoteChar() == '"') {
                script.append(String.format("quote='%s')", Constants.getCsvOutputQuoteChar()));
            } else {
                script.append(String.format("quote=\"%s\")", Constants.getCsvOutputQuoteChar()));
            }

            script.append(END_LINE).append(END_LINE);

            // specify format variables
            Map<String, VariablesMap> metadataVariables = tableScriptInfo.getMetadataVariables();
            Map<String, Variable> listVariables = getAllLength(tableScriptInfo.getDataStructure(), metadataVariables);
            for (Map.Entry<String, Variable> varEntry : listVariables.entrySet()) {
                VariableType variableType = varEntry.getValue().getType();
                script.append(String.format("%s$%s <- as.", tableName, varEntry.getKey()));
                script.append(variableType.getFormatR());
                script.append(String.format("(%s$%s)", tableName, varEntry.getKey())).append(END_LINE);

            }
            script.append(END_LINE).append(END_LINE);
        }

        return script.toString();
    }

}
