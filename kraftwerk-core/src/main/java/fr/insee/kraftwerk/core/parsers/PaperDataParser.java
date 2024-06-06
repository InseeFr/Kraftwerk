package fr.insee.kraftwerk.core.parsers;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.PaperUcq;
import fr.insee.kraftwerk.core.metadata.UcqVariable;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.rawdata.GroupInstance;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of DataParser to read data collected in paper format.
 * Variables which are not in the DDI are ignored.
 * Parser identify QCU variables
 */

@Log4j2
public class PaperDataParser extends DataParser {
	/**
	 * Parser constructor.
	 * 
	 * @param data The SurveyRawData to be filled by the parseSurveyData method. The
	 *             variables must have been previously set.
	 */
	public PaperDataParser(SurveyRawData data) {
		super(data);
	}

	@Override
	void parseDataFile(Path filePath) {
		try (Statement database = SqlUtils.openConnection().createStatement()){
			if(database == null){
				log.error("Failed connection to duckdb");
				return;
			}
			SqlUtils.readCsvFile(database, filePath);
			/*
			 * We first map the variables in the header (first line) of the CSV file to the
			 * variables of the data object. (Column 0 is the identifier).
			 */

			// Variables
			List<String> header = SqlUtils.getColumnNames(database, String.valueOf(filePath.getFileName().toString().split("\\.")[0]));
			VariablesMap variables = data.getMetadataModel().getVariables();
			Map<Integer, String> csvVariablesMap = new HashMap<>();
			for (int j = 1; j < header.size(); j++) {
				String variableName = header.get(j);
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
			ResultSet resultSet = SqlUtils.getAllData(database, String.valueOf(filePath.getFileName()).split("\\.")[0]);
			while (resultSet.next()) {
				QuestionnaireData questionnaireData = new QuestionnaireData();
				GroupInstance answers = questionnaireData.getAnswers();

				// Identifiers
				String rowIdentifier = resultSet.getString(0);
				String[] rowIdentifiers = rowIdentifier.split(Constants.PAPER_IDENTIFIER_SEPARATOR);
				questionnaireData.setIdentifier(rowIdentifiers[0]);
				data.getIdSurveyUnits().add(rowIdentifiers[0]);

				if (rowIdentifiers.length >= 1) {

					// Read variables values
					for ( Map.Entry<Integer, String> entry : csvVariablesMap.entrySet()) {
						// Get the value
						String value = resultSet.getString(entry.getKey());

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
				data.addQuestionnaire(questionnaireData);
			}
		} catch (SQLException e) {
			log.error(e.toString());
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
