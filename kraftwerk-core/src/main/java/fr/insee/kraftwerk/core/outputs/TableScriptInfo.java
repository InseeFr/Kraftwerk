package fr.insee.kraftwerk.core.outputs;

import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.vtl.model.Structured.DataStructure;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/** POJO class to store information needed to write a script for a table. */
@AllArgsConstructor
@Getter
public class TableScriptInfo {

	/** The name of the dataset in the destination language. */
	private String tableName;

	/** The file name of the table. */
	private String fileName;

	/** The data structure (containing variable names and types) of the table. */
	private DataStructure dataStructure;

	/** The data structure (containing variable length) of the table. */
	private Map<String, MetadataModel> metadataModels;

}
