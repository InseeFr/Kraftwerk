package fr.insee.kraftwerk.core.outputs.parquet;

import fr.insee.kraftwerk.core.outputs.ImportScript;
import fr.insee.kraftwerk.core.outputs.TableScriptInfo;
import org.apache.commons.io.FilenameUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        script.append("install.packages(\"arrow\",type = \"binary\")").append(END_LINE);
        script.append("library(dplyr)").append(END_LINE);
        script.append("library(arrow)").append(END_LINE);
        script.append(END_LINE).append(END_LINE);

        Map<String,String> tablesToBind = new HashMap<>();

        for (TableScriptInfo tableScriptInfo : tableScriptInfoList) {
            String tableName = tableScriptInfo.getTableName();
            String tempTable = FilenameUtils.getName(tableScriptInfo.getFileName());

            script.append(String.format("%s <- as.data.frame(arrow::read_parquet(\"%s\"))", tempTable, tableScriptInfo.getFileName())).append(END_LINE);
            tablesToBind.merge(tableName,tempTable,(t1,t2) -> t1.concat(",").concat(t2));

            script.append(END_LINE).append(END_LINE);
        }

        tablesToBind.forEach((tableName,tempTablesList)-> {
            script.append(String.format("%s <- rbind(%s)",tableName,tempTablesList));
            script.append(END_LINE).append(END_LINE);
        });

        return script.toString();
    }

}
