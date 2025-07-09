package fr.insee.kraftwerk.core.parsers;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import fr.insee.bpm.metadata.model.PaperUcq;
import fr.insee.bpm.metadata.model.UcqVariable;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.rawdata.GroupInstance;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of DataParser to read data collected in paper format.
 * Variables which are not in the DDI are ignored.
 * Parser identify QCU variables
 */

@Log4j2
public class PaperDataParser extends DataParser {

    /** Csv reader */
	private CSVReader csvReader;

	/**
	 * Parser constructor.
	 *
	 * @param data The SurveyRawData to be filled by the parseSurveyData method. The
	 *             variables must have been previously set.
	 */
	public PaperDataParser(SurveyRawData data, FileUtilsInterface fileUtilsInterface) {
		super(data, fileUtilsInterface);
	}

	/**
	 * Instantiate a CSVReader.
	 *
	 * @param inputStream stream to the CSV file.
	 */
	private void readCsvFileStream(InputStream inputStream) {
		// Create an object of file reader
		// class with CSV file as a parameter.
        /** Input stream reader */
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		// create csvReader object passing
		// file reader as a parameter
		csvReader = new CSVReader(inputStreamReader);

		// create csvParser object with
		// custom separator semicolon
		CSVParser parser = new CSVParserBuilder().withSeparator(Constants.CSV_PAPER_DATA_SEPARATOR).build();

		// create csvReader object with parameter
		// file reader and parser
		csvReader = new CSVReaderBuilder(inputStreamReader)
				// .withSkipLines(1) // (uncomment to ignore header)
				.withCSVParser(parser).build();
	}

	@Override
	void parseDataFile(Path filePath) {
		try(InputStream inputStream = fileUtilsInterface.readFile(filePath.toString())){
			readCsvFileStream(inputStream);

			/*
			 * We first map the variables in the header (first line) of the CSV file to the
			 * variables of the data object. (Column 0 is the identifier).
			 */

			// Variables
			String[] header = csvReader.readNext();
			VariablesMap variables = data.getMetadataModel().getVariables();
			Map<Integer, String> csvVariablesMap = new HashMap<>();
			for (int j = 0; j < header.length; j++) {
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
						// We want to register the corresponding variable name in the ucq modality object,
						// and have the indicator variable in the variables map associated with the data object.
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
					readVariablesValues(csvVariablesMap, nextRecord, variables, answers, rowIdentifiers);
				}
				data.addQuestionnaire(questionnaireData);
			}
			csvReader.close();
		} catch (CsvValidationException e) {
			log.error(String.format("Following CSV file is malformed: %s", filePath), e);
		} catch (IOException e) {
			log.error(String.format("Could not connect to data file %s", filePath), e);
		}
	}

	private void readVariablesValues(Map<Integer, String> csvVariablesMap, String[] nextRecord, VariablesMap variables, GroupInstance answers, String[] rowIdentifiers) {
		// Read variables values
		for ( Map.Entry<Integer, String> entry : csvVariablesMap.entrySet()) {
			// Get the value
			String value = nextRecord[entry.getKey()];

			// Get the variable
			String variableName = entry.getValue();
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

	// TODO: do something more robust here (-> needs of standardisation for paper
	// data files)
	private String getVariableStem(String variableName) {
		String[] decomposition = variableName.split("_");
		if (decomposition.length == 2) { // no "_" in the variable name
			return decomposition[0];
		} else { // (otherwise, reconstitute the variable name)
			return String.join("_", Arrays.copyOf(decomposition, decomposition.length - 1));
		}
	}

	private static String getUcqValue(String variableName) {
		String[] decomposition = variableName.split("_");
		return decomposition[decomposition.length - 1];
	}

	/**
	 * For unit tests coverage (package scoped)
	 */
	static String getUcqValue_UnitTest(String variableName) {
		return getUcqValue(variableName);
	}

	/**
	 * In paper datafiles, the identifier is like "[interrogationId]_[row identifier]"
	 *
	 * @param subGroupId The group level identifier for all variables of a given
	 *                   questionnaire.
	 * @param groupName  A group name.
	 *
	 * @return A concatenation of these which is a group instance identifier.
	 */
	private static String createGroupId(String groupName, String subGroupId) {
		return groupName + "-" + subGroupId;
	}

	/**
	 * For unit tests coverage (package scoped)
	 */
	static String createGroupId_UnitTest(String groupName, String subGroupId) {
		return createGroupId(groupName, subGroupId);
	}

}