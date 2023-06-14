package fr.insee.kraftwerk.core.outputs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Structured.Component;
import fr.insee.vtl.model.Structured.DataPoint;
import lombok.extern.log4j.Log4j2;

/**
 * To write in memory data into CSV files.
 */
@Log4j2
public class CsvTableWriter {
	
	private CsvTableWriter() {
		//Utility class
	}


	private static CSVWriter setCSVWriter(Path filePath) throws IOException {
		File file = filePath.toFile();
		FileWriter outputFile = new FileWriter(file, StandardCharsets.UTF_8, true);
		return new CSVWriter(outputFile, Constants.CSV_OUTPUTS_SEPARATOR, Constants.getCsvOutputQuoteChar(),
				ICSVWriter.DEFAULT_ESCAPE_CHARACTER, ICSVWriter.DEFAULT_LINE_END);
	}

	/**
	 * Update a CSV file from a Trevas dataset.
	 * 
	 * @param dataset  A Trevas dataset.
	 * @param filePath Path to the file to be written.
	 */
	public static void updateCsvTable(Dataset dataset, Path filePath, Map<String,VariablesMap> metadataVariables, String datasetName) {
		File file = filePath.toFile();
		try (CSVWriter writer = setCSVWriter(filePath)){
			String[] headers = getHeaders(file);

			List<String> variablesSpec = initializeVariablesSpec(metadataVariables, datasetName);

			//All the variables of the dataset
			List<String> variablesDataset = new ArrayList<>(dataset.getDataStructure().keySet());
			variablesDataset.add("New_var_test");
			ArrayList<String> columns = getColumns(datasetName, variablesSpec, variablesDataset);

			String[] columnsTable = convertWithStream(columns);
			List<String> variablesNotInHeaders = new ArrayList<>();
			if(!Arrays.equals(headers, columnsTable)){
				variablesNotInHeaders = Arrays.stream(columnsTable).filter(element -> !Arrays.asList(headers).contains(element)).toList();
				if (!variablesNotInHeaders.isEmpty()){
					variablesNotInHeaders.stream().forEach(varNotFound -> log.warn("Variable {} not present in headers of existing CSV output and will not be added in the output file",varNotFound));
				}
			}

			int rowSize = Arrays.asList(headers).size();
			log.info("{} rows to write in file {}", dataset.getDataPoints().size(), filePath);

			// We check if the header has the same variables as the dataset


			for (int i = 0; i < dataset.getDataPoints().size(); i++) {
				DataPoint dataPoint = dataset.getDataPoints().get(i);
				String[] csvRow = new String[rowSize];
				for (String variableName : variablesDataset) {
					if(!variablesNotInHeaders.contains(variableName)){
						int csvColumn = Arrays.asList(headers).indexOf(variableName);
						Component vtlVar = dataset.getDataStructure().get(variableName);
						String value = getDataPointValue(dataPoint, vtlVar);
						csvRow[csvColumn] = value;
					}
				}
				writer.writeNext(csvRow);

			}
		} catch (IOException e) {
			log.error(String.format("IOException occurred when trying to update CSV table: %s", filePath));
		}

	}

	private static ArrayList<String> getColumns(String datasetName, List<String> variablesSpec, List<String> variablesDataset) {
		ArrayList<String> columns = new ArrayList<>();
		//We add the identifiers prior to the other variables
		columns.add(Constants.ROOT_IDENTIFIER_NAME);
		//Columns for loop identifier
		if (!datasetName.equals(Constants.ROOT_GROUP_NAME)){
			columns.add(datasetName);
		}
		//We add all variables found in specifications
		for (String varSpec : variablesSpec) {
			columns.add(varSpec);
		}
		//We add additional variables produced in the process
		for (String varDataset : variablesDataset){
			if (!columns.contains(varDataset)) {
				columns.add(varDataset);
			}
		}
		return columns;
	}


	private static String[] getHeaders(File file) throws FileNotFoundException {
		Scanner scanner = new Scanner(file);
		String[] headers = null;
		if (scanner.hasNextLine()) headers = scanner.nextLine().split(Character.toString(Constants.CSV_OUTPUTS_SEPARATOR));
		if (headers != null) {
			for (int i=0;i<headers.length;i++){
				headers[i] = headers[i].replace("\"","");
			}
		}
		scanner.close();
		return headers;
	}

	/**
	 * Write a CSV file from a Trevas dataset.
	 * 
	 * @param dataset  A Trevas dataset.
	 * @param filePath Path to the file to be written.
	 */
	public static void writeCsvTable(Dataset dataset, Path filePath, Map<String,VariablesMap> metadataVariables, String datasetName) {
		// File connection
		try (CSVWriter writer = setCSVWriter(filePath)){
						
			// Safety check
			if (dataset.getDataStructure().size() == 0) {
				log.warn("The data object has no variables.");
			}

			List<String> variablesSpec = initializeVariablesSpec(metadataVariables, datasetName);

			//All the variables of the dataset
			List<String> variablesDataset = new ArrayList<>(dataset.getDataStructure().keySet());
			ArrayList<String> columns = getColumns(datasetName, variablesSpec, variablesDataset);
			int rowSize = columns.size();

			// Write header
			String[] csvHeader = convertWithStream(columns);
			writer.writeNext(csvHeader);

			// Write rows
			for (int i = 0; i < dataset.getDataPoints().size(); i++) {
				DataPoint dataPoint = dataset.getDataPoints().get(i);
				String[] csvRow = new String[rowSize];
				for (String variableName : variablesDataset) {
					int csvColumn = columns.indexOf(variableName);
					Component varVtl = dataset.getDataStructure().get(variableName);
					String value = getDataPointValue(dataPoint, varVtl);
					csvRow[csvColumn] = value;
				}
				writer.writeNext(csvRow);
			}
			log.debug("Nb variables in table : {}", dataset.getDataStructure().size());
			log.debug("Nb lines in table : {}", dataset.getDataPoints().size());
			log.info(String.format("Output CSV file: %s successfully written.", filePath));
		} catch (IOException e) {
			log.error(String.format("IOException occurred when trying to write CSV table: %s", filePath));
		}
	}


	private static List<String> initializeVariablesSpec(Map<String, VariablesMap> metadataVariables,
			String datasetName) {
		List<String> variablesSpec = new ArrayList<>();
		for (Entry<String, VariablesMap> entry :  metadataVariables.entrySet()){
			VariablesMap variablesMap = entry.getValue();
			for (String varName : variablesMap.getGroupVariableNamesAsList(datasetName)){
				if (!variablesSpec.contains(varName)){
					variablesSpec.add(varName);
				}
			}
		}
		return variablesSpec;
	}

	/**
	 * Return the datapoint properly formatted value for the variable given. Line
	 * breaks are replaced by spaces. NOTE: may be improved/enriched later on.
	 */
	public static String getDataPointValue(DataPoint dataPoint, Component variable) {
		Object content = dataPoint.get(variable.getName());
		if (content == null) {
			return "";
		} else {
			if (variable.getType().equals(Boolean.class)) {
				if (content.equals(true)) {
					content = "1";
				} else {
					content = "0";
				}
			}
			String value = content.toString();
			value = value.replace('\n', ' ');
			value = value.replace('\r', ' ');
			return value;
		}
	}

	/**
	 * Static method to convert a list into an array.
	 * 
	 * @param list A List containing String type objects.
	 * @return A String[] array.
	 */
	private static String[] convertWithStream(List<String> list) {
		// https://dzone.com/articles/converting-between-java-list-and-array
		return list.toArray(String[]::new);
	}

}
