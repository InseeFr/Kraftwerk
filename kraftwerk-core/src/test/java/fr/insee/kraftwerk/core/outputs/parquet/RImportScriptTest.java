package fr.insee.kraftwerk.core.outputs.parquet;

import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.outputs.TableScriptInfo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RImportScriptTest {

    @Test
    void generateScriptTest() {
        List<TableScriptInfo> tableScriptInfoList = new ArrayList<TableScriptInfo>();

        String tableName1 = "myTableName1";
        String fileName1 = "myFileName1";
        TableScriptInfo tsi1 = new TableScriptInfo(tableName1, fileName1, null, null);
        String tableName2 = "myTableName2";
        String fileName2 = "myFileName2";
        TableScriptInfo tsi2 = new TableScriptInfo(tableName2, fileName2, null, null);

        tableScriptInfoList.add(tsi1);
        tableScriptInfoList.add(tsi2);
        RImportScript ris = new RImportScript(tableScriptInfoList);
        String result = ris.generateScript();

        StringBuilder expectedResult = new StringBuilder();
        expectedResult.append("install.packages(\"arrow\",type = \"binary\")\n");
        expectedResult.append("library(dplyr)\n");
        expectedResult.append("library(arrow)\n");
        expectedResult.append("\n");
        expectedResult.append("\n");
        expectedResult.append(fileName1 + " <- as.data.frame(arrow::read_parquet(\"" + fileName1 + "\"))\n");
        expectedResult.append("\n");
        expectedResult.append("\n");
        expectedResult.append(fileName2 + " <- as.data.frame(arrow::read_parquet(\"" + fileName2 + "\"))\n");
        expectedResult.append("\n");
        expectedResult.append("\n");
        expectedResult.append(tableName2 + " <- rbind(" + fileName2 + ")\n");
        expectedResult.append("\n");
        expectedResult.append(tableName1 + " <- rbind(" + fileName1 + ")\n");
        expectedResult.append("\n");

        assertEquals(expectedResult.toString(), result);

    }

}

