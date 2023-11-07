package fr.insee.kraftwerk.core.outputs.parquet;

import java.util.List;

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

        //import libraries
        script.append("library(dplyr)").append(END_LINE);
        script.append("library(arrow)").append(END_LINE);
        script.append(END_LINE).append(END_LINE);

        for (TableScriptInfo tableScriptInfo : tableScriptInfoList) {
            String tableName = tableScriptInfo.getTableName();

            
            
            // function call
            script.append(String.format("%s <- as.data.frame(arrow::read_parquet(\"%s\")", tableName, tableScriptInfo.getFileName())).append(END_LINE);

            script.append(END_LINE).append(END_LINE);
        }

        return script.toString();
    }

}
