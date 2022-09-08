package fr.insee.kraftwerk.core.outputs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.inputs.UnknownDataFormatException;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to generate import scripts for the csv output tables.
 */

@Slf4j
public class ImportScripts {

	private final List<TableScriptInfo> tableScriptInfoList = new ArrayList<>();

	public ImportScripts() {
	};

	/** @see TableScriptInfo */
	public void registerTable(TableScriptInfo tableScriptInfo) {
		tableScriptInfoList.add(tableScriptInfo);
	}

	public String scriptR_base() {
		return "Not implemented yet.";
	}

	public String scriptR_dataTable() {

		StringBuilder script = new StringBuilder();

		for (TableScriptInfo tableScriptInfo : tableScriptInfoList) {
			String tableName = tableScriptInfo.getTableName();
			// function call
			script.append(String.format("%s <- data.table::fread(\n", tableName));

			// file, sep and header parameter
			script.append(String.format("file=\"%s\", \n", tableScriptInfo.getCsvFileName()));
			script.append(String.format("sep=\"%s\", \n", Constants.CSV_OUTPUTS_SEPARATOR));
			script.append("encoding = \"UTF-8\", \n");

			script.append("header=TRUE, \n");
			// quote parameter
			if (Constants.CSV_OUTPUTS_QUOTE_CHAR == '"') { // TODO: condition always true, but not later if we let the
															// user choose
				script.append(String.format("quote='%s')", Constants.CSV_OUTPUTS_QUOTE_CHAR));
			} else {
				script.append(String.format("quote=\"%s\")", Constants.CSV_OUTPUTS_QUOTE_CHAR));
			}

			script.append("\n\n");

			// specify format variables
			Map<String, VariablesMap> metadataVariables = tableScriptInfo.getMetadataVariables();
			Map<String, Variable> listVariables = tableScriptInfo.getAllLength(tableScriptInfo.getDataStructure(),
					metadataVariables);
			for (String variableName : listVariables.keySet()) {
				VariableType variableType = listVariables.get(variableName).getType();

				script.append(String.format("%s$%s <- as.", tableName, variableName));
				switch (variableType) {
				case STRING:
					script.append("character");
					break;
				case NUMBER:
					script.append("numeric");
					break;
				case INTEGER:
					script.append("integer");
					break;
				case BOOLEAN:
					script.append("logical");
					break;
				case DATE:
					script.append("Date");
					break;
				default:
					throw new UnknownDataFormatException("Unknown data format: " + variableType);
				}
				script.append(String.format("(%s$%s)\n", tableName, variableName));

			}
			script.append("\n\n");
		}

		return script.toString();

	}

	public String scriptPython_pandas() {
		return "Not implemented yet.";
	}

	public String scriptSAS() {

		StringBuilder script = new StringBuilder();
		script.append(String.format("/****************************************************/ \n"));
		script.append(String.format("/*****  Automated import for Kraftwerk outputs  *****/ \n"));
		script.append(String.format("/****************************************************/ \n"));
		script.append(String.format("%%let path = local_folder; \n\n"));

		for (TableScriptInfo tableScriptInfo : tableScriptInfoList) {
			String tableName = tableScriptInfo.getTableName();
			// filename reference to the file
			script.append(String.format("filename %s \"&path\\%s\" ENCODING=\"UTF-8\" ;\n",
					tableName.substring(0, Math.min(tableName.length(), 6)), tableScriptInfo.getCsvFileName()));

			// PROC IMPORT
			// careful about special characters, which can't be imported in SAS
			script.append(String.format("data %s; \n", tableName));
			script.append("%let _EFIERR_ = 0; /* set the ERROR detection macro variable */ \n");
			script.append(String.format("infile %s delimiter=\"%s\" MISSOVER DSD lrecl=13106 firstobs=2;\n",
					tableName.substring(0, Math.min(tableName.length(), 6)), Constants.CSV_OUTPUTS_SEPARATOR));

			// Special treatment to display the length of the variables
			Map<String, VariablesMap> metadataVariables = tableScriptInfo.getMetadataVariables();
			Map<String, Variable> listVariables = tableScriptInfo.getAllLength(tableScriptInfo.getDataStructure(),
					metadataVariables);
			script.append(scriptSASPart1(listVariables));

			/*
			 * format MAA2AT $1. ; format ANNEENQ best32. ;
			 */

			script.append(scriptSASPart2(listVariables));

			/*
			 * input IdUE $ ADRESSE $ NHAB MAA2AT $;
			 */

			script.append(scriptSASPart3(listVariables));

			script.append("if _ERROR_ then call symputx('_EFIERR_',1);\n");
			script.append("run;\n");

			script.append("\n\n");
		}

		return script.toString();
	}

	/**
	 * Convert Kraftwerk in memory type to a type argument for the data.table::fread
	 * colClasses option.
	 * 
	 * @param variableType a VariableType from the Kraftwerk enum class
	 * @return eiter: character, integer, numeric, Date, logical
	 */
	private String getDataTableType(VariableType variableType) {
		switch (variableType) {
		case STRING:
			return "character";
		case INTEGER:
			return "integer";
		case NUMBER:
			return "number";
		case DATE:
			return "Date";
		case BOOLEAN:
			return "logical";
		default:
			log.debug("Missing variable type: this should not happen!");
			return null;
		}
	}

	/**
	 * Get the length in SAS standards
	 * 
	 * @param length a value of the dataset variable
	 * @return the length corrected according to the SAS rules
	 */
	/*
	 * Put informats informat MAA2AT $1. ; informat ANNEENQ best32. ;
	 */
	public String getSASNumericLength(String length) {
		String result = length;
		// SAS doesn't allow decimal lengths
		if (length.contains(".")) {
			String[] lengths = length.split("\\.");
			result = String.valueOf(Integer.parseInt(lengths[0]) + Integer.parseInt(lengths[1]));
		}
		// SAS's minimum length for a numeric variable is 3
		if (length.contentEquals("0") || length.contentEquals("1") || length.contentEquals("2")) {
			result = "3";
		}
		return result;
	}

	/**
	 * Put informats for the first step
	 * 
	 * @param listVariables a list of the variables with their format and length
	 * @return a script with the informat instructions
	 */
	public String scriptSASPart1(Map<String, Variable> listVariables) {

		StringBuilder script = new StringBuilder();
		for (String variableName : listVariables.keySet()) {
			Variable variable = listVariables.get(variableName);
			String length = variable.getLength();
			if (!length.contentEquals("0")) {
				// We write the format instructions if we have inrofmations on the length of the
				// variables

				if (variable.getType().equals(VariableType.BOOLEAN)) {
					script.append(String.format("informat %s $1. ;\n", variableName, length));
				} else if (variable.getType().equals(VariableType.STRING)
						|| variable.getType().equals(VariableType.DATE)) {
					script.append(String.format("informat %s $%s. ;\n", variableName, length));
				} else if (variable.getType().equals(VariableType.INTEGER)
						|| variable.getType().equals(VariableType.NUMBER)) {
					if (length.contains(".")) {
						script.append(String.format("informat %s %s. ;\n", variableName, getSASNumericLength(length)));
					} else {
						script.append(String.format("informat %s %s. ;\n", variableName, getSASNumericLength(length)));
					}
				}
			} else {
				script.append(String.format("informat %s $32. ;\n", variableName));
			}
		}
		script.append("\n");
		return script.toString();
	}

	/**
	 * Put formats for the second step
	 * 
	 * @param listVariables a list of the variables with their format and length
	 * @return a script with the format instructions
	 */
	public String scriptSASPart2(Map<String, Variable> listVariables) {

		StringBuilder script = new StringBuilder();
		for (String variableName : listVariables.keySet()) {
			Variable variable = listVariables.get(variableName);
			String length = variable.getLength();
			if (!length.contentEquals("0")) {
				// We write the format instructions if we have inrofmations on the length of the
				// variables

				if (variable.getType().equals(VariableType.BOOLEAN) || variable.getType().equals(VariableType.STRING)
						|| variable.getType().equals(VariableType.DATE)) {
					script.append(String.format("format %s $%s. ;\n", variableName, length));
				} else if (variable.getType().equals(VariableType.INTEGER)
						|| variable.getType().equals(VariableType.NUMBER)) {
					if (length.contains(".")) {
						script.append(String.format("format %s %s. ;\n", variableName, getSASNumericLength(length)));
					} else {
						script.append(String.format("format %s %s. ;\n", variableName, getSASNumericLength(length)));
					}
				}
			} else {
				script.append(String.format("format %s $32. ;\n", variableName));
			}
		}
		script.append("\n");
		return script.toString();
	}

	/**
	 * Put the input formats for the third step
	 * 
	 * @param listVariables a list of the variables with their format and length
	 * @return a script with the input instructions
	 */
	public String scriptSASPart3(Map<String, Variable> listVariables) {

		StringBuilder script = new StringBuilder();

		script.append(String.format("input \n"));
		for (String variableName : listVariables.keySet()) {
			Variable variable = listVariables.get(variableName);
			String length = variable.getLength();

			// We write the format instructions if we have informations on the length of the
			// variables
			if (variable.getType().equals(VariableType.BOOLEAN) || variable.getType().equals(VariableType.STRING)
					|| variable.getType().equals(VariableType.DATE)) {
				script.append(String.format("%s $ \n", variableName, length));
			} else if (variable.getType().equals(VariableType.INTEGER)
					|| variable.getType().equals(VariableType.NUMBER)) {

				script.append(String.format("%s \n", variableName, length));

			}
		}
		script.append(";\n");
		return script.toString();
	}
}
