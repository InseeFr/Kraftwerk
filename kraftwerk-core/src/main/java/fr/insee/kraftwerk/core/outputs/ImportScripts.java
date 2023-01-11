package fr.insee.kraftwerk.core.outputs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;

/**
 * Class to generate import scripts for the csv output tables.
 * 
 * Some methods should be implemented later : 
 * - scriptR_base
 * - scriptPython_pandas
 */
public class ImportScripts {

	private final List<TableScriptInfo> tableScriptInfoList;
	private static final String END_LINE = "\n";

	public ImportScripts() {
		tableScriptInfoList = new ArrayList<>();
	}

	/** @see TableScriptInfo */
	public void registerTable(TableScriptInfo tableScriptInfo) {
		tableScriptInfoList.add(tableScriptInfo);
	}

	public String scriptRdataTable() {

		StringBuilder script = new StringBuilder();

		for (TableScriptInfo tableScriptInfo : tableScriptInfoList) {
			String tableName = tableScriptInfo.getTableName();
			// function call
			script.append(String.format("%s <- data.table::fread(", tableName)).append(END_LINE);

			// file, sep and header parameter
			script.append(String.format("file=\"%s\", ", tableScriptInfo.getCsvFileName())).append(END_LINE);
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
			Map<String, Variable> listVariables = tableScriptInfo.getAllLength(tableScriptInfo.getDataStructure(),
					metadataVariables);
			for (Entry<String, Variable> varEntry : listVariables.entrySet()) {
				VariableType variableType = varEntry.getValue().getType();
				script.append(String.format("%s$%s <- as.", tableName, varEntry.getKey()));
				script.append(variableType.getFormatR());
				script.append(String.format("(%s$%s)", tableName, varEntry.getKey())).append(END_LINE);

			}
			script.append(END_LINE).append(END_LINE);
		}

		return script.toString();

	}

	public String scriptSAS() {

		StringBuilder script = new StringBuilder();
		script.append("/****************************************************/ ").append(END_LINE);
		script.append("/*****  Automated import for Kraftwerk outputs  *****/ ").append(END_LINE);
		script.append("/****************************************************/ ").append(END_LINE);
		script.append("%%let path = local_folder; ").append(END_LINE).append(END_LINE);

		for (TableScriptInfo tableScriptInfo : tableScriptInfoList) {
			String tableName = tableScriptInfo.getTableName();
			// filename reference to the file
			script.append(String.format("filename %s \"&path\\%s\" ENCODING=\"UTF-8\" ;",
					tableName.substring(0, Math.min(tableName.length(), 6)), tableScriptInfo.getCsvFileName())).append(END_LINE);

			// PROC IMPORT
			// careful about special characters, which can't be imported in SAS
			script.append(String.format("data %s; ", tableName)).append(END_LINE);
			script.append("%let _EFIERR_ = 0; /* set the ERROR detection macro variable */ ").append(END_LINE);
			script.append(String.format("infile %s delimiter=\"%s\" MISSOVER DSD lrecl=13106 firstobs=2;",
					tableName.substring(0, Math.min(tableName.length(), 6)), Constants.CSV_OUTPUTS_SEPARATOR)).append(END_LINE);

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
		for (Entry<String, Variable> varEntry : listVariables.entrySet()) {
			Variable variable = varEntry.getValue();
			String length = variable.getLength();
			if (!length.contentEquals("0")) {
				// We write the format instructions if we have inrofmations on the length of the
				// variables

				if (variable.getType().equals(VariableType.BOOLEAN)) {
					script.append(String.format("informat %s $1. ;", varEntry.getKey())).append(END_LINE);
				} else if (variable.getType().equals(VariableType.STRING)
						|| variable.getType().equals(VariableType.DATE)) {
					script.append(String.format("informat %s $%s. ;", varEntry.getKey(), length)).append(END_LINE);
				} else if (variable.getType().equals(VariableType.INTEGER)
						|| variable.getType().equals(VariableType.NUMBER)) {
					script.append(String.format("informat %s %s. ;", varEntry.getKey(), getSASNumericLength(length))).append(END_LINE);
				}
			} else {
				script.append(String.format("informat %s $32. ;", varEntry.getKey())).append(END_LINE);
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
		for (Entry<String, Variable> varEntry : listVariables.entrySet()) {
			Variable variable = varEntry.getValue();
			String length = variable.getLength();
			if (!length.contentEquals("0")) {
				// We write the format instructions if we have informations on the length of the
				// variables

				if (variable.getType().equals(VariableType.BOOLEAN) || variable.getType().equals(VariableType.STRING)
						|| variable.getType().equals(VariableType.DATE)) {
					script.append(String.format("format %s $%s. ;", varEntry.getKey(), length)).append(END_LINE);
				} else if (variable.getType().equals(VariableType.INTEGER)
						|| variable.getType().equals(VariableType.NUMBER)) {
					script.append(String.format("format %s %s. ;", varEntry.getKey(), getSASNumericLength(length))).append(END_LINE);
				}
			} else {
				script.append(String.format("format %s $32. ;", varEntry.getKey())).append(END_LINE);
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

		script.append("input ").append(END_LINE);
		for (Entry<String,Variable> variableEntry : listVariables.entrySet()) {
			Variable variable = variableEntry.getValue();

			// We write the format instructions if we have informations on the length of the
			// variables
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
