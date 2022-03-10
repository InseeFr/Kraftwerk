package fr.insee.kraftwerk.outputs;

import fr.insee.vtl.model.Structured.DataStructure;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** POJO class to store information needed to write a script for a CSV table. */
@AllArgsConstructor
public class TableScriptInfo {

    /** The name of the dataset in the destination language. */
    @Getter String tableName;
    /** The CSV file name of the table. */
    @Getter String csvFileName;
    /** The data structure (containing variable names and types) of the table. */
    @Getter DataStructure dataStructure;

}
