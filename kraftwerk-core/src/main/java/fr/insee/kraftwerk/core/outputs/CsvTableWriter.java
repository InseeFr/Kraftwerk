package fr.insee.kraftwerk.core.outputs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.opencsv.CSVWriter;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Structured.DataPoint;
import lombok.extern.slf4j.Slf4j;

/**
 * To write in memory data into CSV files.
 *
 */
@Slf4j
public class CsvTableWriter {

	static CSVWriter writer;

	static void setCSVWriter(String filePath) {
		try {
			File file = new File(filePath);
			FileWriter outputFile = new FileWriter(file, StandardCharsets.UTF_8, true);
			writer = new CSVWriter(outputFile, Constants.CSV_OUTPUTS_SEPARATOR, Constants.CSV_OUTPUTS_QUOTE_CHAR,
					CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
		} catch (Exception e) {
			log.error("Unable to write csv file: " + filePath, e);
		}
	}

	static void closeWriter(String filePath) {
		try {
			writer.close();
		} catch (IOException e) {
			log.warn("IOException when trying to close: " + filePath, e);
		}
	}

	/**
	 * Update a CSV file from a Trevas dataset.
	 * 
	 * @param dataset  A Trevas dataset.
	 * @param filePath Path to the file to be written.
	 */
	public static void updateCsvTable(Dataset dataset, String filePath, String datasetName, Path outputFolder) {
		moveFile(dataset, filePath, datasetName, outputFolder);
		File file = new File(filePath);
		setCSVWriter(filePath);
		try {
			Scanner scanner = new Scanner(file);

			String[] headers = null;
			if (scanner.hasNextLine())
				headers = scanner.nextLine().split(Character.toString(Constants.CSV_OUTPUTS_SEPARATOR));

			scanner.close();

			// Map column number with variables
			List<String> variablesList = new ArrayList<>(dataset.getDataStructure().keySet());
			Map<String, Integer> columnsMap = new HashMap<>();
			int rowSize = variablesList.size();
			for (int j = 0; j < rowSize; j++) {
				columnsMap.put(variablesList.get(j), j);
			}

			// We check if the header has the same variables as the dataset
			if (Arrays.equals(headers, convertWithStream(variablesList))) {
				// Write rows
				for (int i = 0; i < dataset.getDataPoints().size(); i++) {
					DataPoint dataPoint = dataset.getDataPoints().get(i);
					String[] csvRow = new String[rowSize];
					for (String variableName : variablesList) {
						int csvColumn = columnsMap.get(variableName);
						String value = getDataPointValue(dataPoint, variableName);
						csvRow[csvColumn] = value;
					}
					writer.writeNext(csvRow);
				}

			} else {
				// In this case we have different variables between CSV header and dataset,
				// so we first get every variable from the CSV file and supply the values,
				for (int i = 0; i < dataset.getDataPoints().size(); i++) {
					DataPoint dataPoint = dataset.getDataPoints().get(i);
					String[] csvRow = new String[rowSize];
					List<String> variablesListToUse = variablesList;
					for (String variableName : headers) {
						// Verifying that the dataset contains the variable from existing CSV File
						if (columnsMap.containsKey(variableName)) {
							int csvColumn = columnsMap.get(variableName);
							String value = getDataPointValue(dataPoint, variableName);
							csvRow[csvColumn] = value;
						}
						variablesListToUse.remove(variableName);
					}
					// then we add the remaining variables
					for (String variableName : variablesListToUse) {

						int csvColumn = columnsMap.get(variableName);
						String value = getDataPointValue(dataPoint, variableName);
						csvRow[csvColumn] = value;
					}
					
					writer.writeNext(csvRow);
				}

			}
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void moveFile(Dataset dataset, String filePath, String datasetName, Path outputFolder) {
		// First we create an archive directory in case it doesn't exist
				if (!Files.exists(outputFolder.resolve("Archive"))){
					new File(outputFolder.resolve("Archive").toString()).mkdir();
				}
				// We then put the old file in the archive file
				File file = new File(filePath);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			    // get the last modified date and format it to the defined format
			    Path copied = Paths.get(outputFolder.resolve("Archive").toString() + "/" + datasetName + "-" + sdf.format(file.lastModified()) + ".csv");
			    try {
					Files.copy(Paths.get(filePath), copied, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			 
	}

	/**
	 * Write a CSV file from a Trevas dataset.
	 * 
	 * @param dataset  A Trevas dataset.
	 * @param filePath Path to the file to be written.
	 */
	public static void writeCsvTable(Dataset dataset, String filePath) {
		// File connection
		setCSVWriter(filePath);

		// Safety check
		if (dataset.getDataStructure().size() == 0) {
			log.warn("The data object has no variables.");
		}

		// Map column number with variables
		List<String> variablesList = new ArrayList<>(dataset.getDataStructure().keySet());
		Map<String, Integer> columnsMap = new HashMap<>();
		int rowSize = variablesList.size();
		for (int j = 0; j < rowSize; j++) {
			columnsMap.put(variablesList.get(j), j);
		}

		// Write header
		String[] csvHeader = convertWithStream(variablesList);
		writer.writeNext(csvHeader);

		// Write rows
		for (int i = 0; i < dataset.getDataPoints().size(); i++) {
			DataPoint dataPoint = dataset.getDataPoints().get(i);
			String[] csvRow = new String[rowSize];
			for (String variableName : variablesList) {
				int csvColumn = columnsMap.get(variableName);
				String value = getDataPointValue(dataPoint, variableName);
				csvRow[csvColumn] = value;
			}
			writer.writeNext(csvRow);
		}

		log.info(String.format("Output CSV file: %s successfully written.", filePath));

		// Connection closure
		closeWriter(filePath);
	}

	/**
	 * Return the datapoint properly formatted value for the variable given. Line
	 * breaks are replaced by spaces. NOTE: may be improved/enriched later on.
	 */
	private static String getDataPointValue(DataPoint dataPoint, String variableName) {
		Object content = dataPoint.get(variableName);
		if (content == null) {
			return "";
		} else {
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