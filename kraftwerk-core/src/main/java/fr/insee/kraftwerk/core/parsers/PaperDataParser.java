package fr.insee.kraftwerk.core.parsers;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.PaperUcq;
import fr.insee.kraftwerk.core.metadata.UcqVariable;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.rawdata.GroupInstance;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import lombok.extern.log4j.Log4j2;

/**
 * Implementation of DataParser to read data collected in paper format.
 * Variables which are not in the DDI are ignored.
 * Parser identify QCU variables
 */

@Log4j2
public class PaperDataParser extends DataParser {

	/** File reader */
	private FileReader filereader;
	/** Csv reader */
	private CSVReader csvReader;

	/**
	 * Parser constructor.
	 * 
	 * @param data The SurveyRawData to be filled by the parseSurveyData method. The
	 *             variables must have been previously set.
	 */
	public PaperDataParser(SurveyRawData data) {
		super(data);
	}

	/**
	 * Instantiate a CSVReader.
	 * 
	 * @param filePath Path to the CSV file.
	 */
	private void readCsvFile(Path filePath) {
		try {
			// Create an object of file reader
			// class with CSV file as a parameter.
			filereader = new FileReader(filePath.toString());
			// create csvReader object passing
			// file reader as a parameter
			csvReader = new CSVReader(filereader);

			// create csvParser object with
			// custom separator semicolon
			CSVParser parser = new CSVParserBuilder().withSeparator(Constants.CSV_PAPER_DATA_SEPARATOR).build();

			// create csvReader object with parameter
			// file reader and parser
			csvReader = new CSVReaderBuilder(filereader)
					// .withSkipLines(1) // (uncomment to ignore header)
					.withCSVParser(parser).build();

		} catch (FileNotFoundException e) {
			log.error(String.format("Unable to find the file %s", filePath), e);
		}
	}

	@Override
	void parseDataFile(Path filePath) {

		readCsvFile(filePath);

		try {

			/*
			 * We first map the variables in the header (first line) of the CSV file to the
			 * variables of the data object. (Column 0 is the identifier).
			 */

			// Variables
			String[] header = csvReader.readNext();
			VariablesMap variables = data.getVariablesMap();
			Map<Integer, String> csvVariablesMap = new HashMap<>();
			for (int j = 1; j < header.length; j++) {
				String variableName = header[j];
				// If the variable name is in the DDI we map it directly
				if (variables.hasVariable(variableName)) {
					csvVariablesMap.put(j, variableName);
				}
				// Else the variable might be from a unique choice question that has been split
				else {
					String variableStem = getVariableStem(variableName);
					if (variables.hasUcq(variableStem)) {
						String ucqValue = getUcqValue(variableName);
						// Reminder: at this point we have value and text registered from the DDI.
						// We want to register the corresponding variable name in the ucq modality
						// object,
						// and have the indicator variable in the variables map associated with the data
						// object.
						UcqVariable ucqVariable = (UcqVariable) variables.getVariable(variableStem);
						PaperUcq indicatorVariable = new PaperUcq(variableName, ucqVariable, ucqValue);
						variables.putVariable(indicatorVariable);
						csvVariablesMap.put(j, variableName);
					} else {
						log.warn(String.format("Unable to find a variable corresponding to CSV column \"%s\"",
								variableName));
						log.warn("Values of this column will be ignored.");
					}

				}
			}

			/*
			 * Then we read each data line and carefully put values at the right place.
			 */

			// Survey answers
			String[] nextRecord;
			while ((nextRecord = csvReader.readNext()) != null) {

				QuestionnaireData questionnaireData = new QuestionnaireData();
				GroupInstance answers = questionnaireData.getAnswers();

				// Identifiers
				String rowIdentifier = nextRecord[0];
				String[] rowIdentifiers = rowIdentifier.split(Constants.PAPER_IDENTIFIER_SEPARATOR);
				questionnaireData.setIdentifier(rowIdentifiers[0]);
				data.getIdSurveyUnits().add(rowIdentifiers[0]);

				if (rowIdentifiers.length >= 1) {

					// Read variables values
					for (int j : csvVariablesMap.keySet()) {
						// Get the value
						String value = nextRecord[j];

						// Get the variable
						String variableName = csvVariablesMap.get(j);
						Variable variable = variables.getVariable(variableName);
						// Put the value
						if (variable.getGroup().isRoot()) {
							answers.putValue(variableName, value);
						} else if (rowIdentifiers.length > 1) {
							answers.putValue(variableName, value);
							String subGroupId = rowIdentifiers[1];
							String groupName = variable.getGroupName();
							answers.getSubGroup(groupName).putValue(value, variableName,
									createGroupId(groupName, subGroupId));

						}
					}
				}
				data.addQuestionnaire(questionnaireData);
			}
			filereader.close();
			csvReader.close();
		} catch (CsvValidationException e) {
			log.error(String.format("Following CSV file is malformed: %s", filePath), e);
		} catch (IOException e) {
			log.error(String.format("Could not connect to data file %s", filePath), e);
		}
	}

	// TODO: do something more robust here (-> needs of standardisation for paper
	// data files)
	private String getVariableStem(String variableName) {
		String[] decomposition = variableName.split("_");
		if (decomposition.length == 2) { // (no "_" in the variable name)
			return decomposition[0];
		} else { // (otherwise, reconstitute the variable name)
			return String.join("_", Arrays.copyOf(decomposition, decomposition.length - 1));
		}
	}

	private String getUcqValue(String variableName) {
		String[] decomposition = variableName.split("_");
		return decomposition[decomposition.length - 1];
	}

	/**
	 * In paper datafiles, the identifier is like "[IdUE]_[row identifier]"
	 *
	 * Example: //TODO: write explanations for this
	 *
	 * @param subGroupId The group level identifier for all variables of a given
	 *                   questionnaire.
	 * @param groupName  A group name.
	 *
	 * @return A concatenation of these which is a group instance identifier.
	 */
	private String createGroupId(String groupName, String subGroupId) {
		return groupName + "-" + subGroupId;
	}

}
