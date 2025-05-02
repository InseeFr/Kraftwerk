package fr.insee.kraftwerk.core.outputs.csv;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.kraftwerk.core.outputs.OutputFiles;
import fr.insee.kraftwerk.core.outputs.TableScriptInfo;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to manage the writing of CSV output tables.
 */
@Slf4j
public class CsvOutputFiles extends OutputFiles {
	private final KraftwerkExecutionContext kraftwerkExecutionContext;

	/**
	 * When an instance is created, the output folder is created.
	 * 
	 * @param outDirectory Out directory defined in application properties.
	 * @param vtlBindings  Vtl bindings where datasets are stored.
	 */
	public CsvOutputFiles(Path outDirectory, VtlBindings vtlBindings, List<String> modes, Statement database, FileUtilsInterface fileUtilsInterface) {
		super(outDirectory, vtlBindings, modes, database, fileUtilsInterface);
		this.kraftwerkExecutionContext = null;
	}
	public CsvOutputFiles(Path outDirectory, VtlBindings vtlBindings, KraftwerkExecutionContext kraftwerkExecutionContext, List<String> modes, Statement database, FileUtilsInterface fileUtilsInterface) {
		super(outDirectory, vtlBindings, modes, database, fileUtilsInterface);
		this.kraftwerkExecutionContext = kraftwerkExecutionContext;
	}


	/**
	 * Method to write CSV output tables from datasets that are in the bindings.
	 */
	@Override
	public void writeOutputTables() throws KraftwerkException {
		for (String datasetName : getDatasetToCreate()) {
			try {
				//Temporary file
				Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir")));
				Path tmpOutputFile = Files.createTempFile(Path.of(System.getProperty("java.io.tmpdir")),outputFileName(datasetName), null);

				//Get column names
				List<String> columnNames = SqlUtils.getColumnNames(getDatabase(), datasetName);

				if(columnNames.isEmpty()){
					log.warn("dataset {} is empty !", datasetName);
					return;
				}

				//Get boolean columns names
				List<String> boolColumnNames = SqlUtils.getColumnNames(getDatabase(), datasetName, VariableType.BOOLEAN);
				//Get indexes of boolean columns
				List<Integer> boolColumnIndexes = new ArrayList<>();

				//Create file with double quotes header
				Files.write(tmpOutputFile, buildHeader(columnNames, boolColumnNames, boolColumnIndexes).getBytes());

				//Data export into temp file
				StringBuilder exportCsvQuery = getExportCsvQuery(datasetName, tmpOutputFile.toFile(), columnNames);
				this.getDatabase().execute(exportCsvQuery.toString());

				//Apply csv format transformations

				//Merge data file with header file
				//Read line by line to avoid memory waste
				try(BufferedReader bufferedReader = Files.newBufferedReader(Path.of(tmpOutputFile.toAbsolutePath() + "data"))){
					String line = bufferedReader.readLine();
					while(line != null){
						//Apply transformations to elements
						line = applyNullTransformation(line);
						line = applyBooleanTransformations(line, boolColumnIndexes);

						Files.write(tmpOutputFile,(line + "\n").getBytes(),StandardOpenOption.APPEND);
						line = bufferedReader.readLine();
					}
				}
				Files.deleteIfExists(Path.of(tmpOutputFile + "data"));

				String outputFile = getOutputFolder().resolve(outputFileName(datasetName)).toString();
				//Move to output folder
				getFileUtilsInterface().moveFile(tmpOutputFile, outputFile);
				log.info(String.format("File: %s successfully written", outputFile));
				//Count rows for functional log
				if (kraftwerkExecutionContext != null) {
					try(ResultSet countResult =
								this.getDatabase().executeQuery("SELECT COUNT(*) FROM '%s'".formatted(datasetName))){
						countResult.next();
                        kraftwerkExecutionContext.getLineCountByTableMap().put(datasetName, countResult.getInt(1));
					}
				}
			} catch (SQLException | IOException e) {
				throw new KraftwerkException(500, e.toString());
			}
        }
	}


	//========= OPTIMISATIONS PERFS (START) ==========
	/**
	 * @author Adrien Marchal
	 * Method to write CSV output tables from datasets that are in the bindings.
	 */
	@Override
	public void writeOutputTablesV2() throws KraftwerkException {
		for (String datasetName : getDatasetToCreate()) {
			try {
				//Temporary file
				Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir")));
				Path tmpOutputFile = Files.createTempFile(Path.of(System.getProperty("java.io.tmpdir")),outputFileName(datasetName), null);

				//Get column names
				List<String> columnNames = SqlUtils.getColumnNames(getDatabase(), datasetName);
				if(columnNames.isEmpty()){
					log.warn("dataset {} is empty !", datasetName);
					return;
				}

				//Get boolean columns names
				List<String> boolColumnNames = SqlUtils.getColumnNames(getDatabase(), datasetName, VariableType.BOOLEAN);
				//Get indexes of boolean columns
				List<Integer> boolColumnIndexes = new ArrayList<>();

				//Create file with double quotes header
				Files.write(tmpOutputFile, buildHeader(columnNames, boolColumnNames, boolColumnIndexes).getBytes());

				//Data export into temp file
				StringBuilder exportCsvQuery = getExportCsvQuery(datasetName, tmpOutputFile.toFile(), columnNames);
				this.getDatabase().execute(exportCsvQuery.toString());

				if(!boolColumnNames.isEmpty()) {
					log.warn("boolColumns NOT EMPTY !");
				}

				//!!!WARNING!!! : !!!REGEX!!! TRANSFORMATION FOR PERFORMANCES OPTIMISATIONS
				String[] regExPatternsTab = regExPatterns(columnNames, boolColumnNames, boolColumnIndexes);
				log.info(String.format("sbRegExPatternToFind : %s", regExPatternsTab[0]));
				log.info(String.format("sbRegExPatternReplacement : %s", regExPatternsTab[1]));


				//In order to be aware of the process progress, we count how much lines the file contains & how many blocks must be processed
				int totalLinesNumber = 0;
				int INPUT_FILE_LINE_NUMBER_BLOCK = 50;
				try(BufferedReader bufferedReader = Files.newBufferedReader(Path.of(tmpOutputFile.toAbsolutePath() + "data"))) {
					while (bufferedReader.readLine() != null) {
						totalLinesNumber++;
					}
					bufferedReader.close();
				}
				int totalBlocksNumber = totalLinesNumber / INPUT_FILE_LINE_NUMBER_BLOCK == 0 ? 1 : totalLinesNumber / INPUT_FILE_LINE_NUMBER_BLOCK;
				log.info(String.format("%s lines (%s blocks)", totalLinesNumber, totalBlocksNumber));

				// => READING DATA FROM ".tmpdata" file BY BLOCK OF 50 LINES AND WRITING FORMATTED DATA INTO ".tmp" file
				int currentBlockNumber = 1;
				StringBuilder sbInput = new StringBuilder();
				try(BufferedReader bufferedReader = Files.newBufferedReader(Path.of(tmpOutputFile.toAbsolutePath() + "data"))){
					String line = bufferedReader.readLine();
					int nbReadLinesInBlock = 1;
					int currentReadLine = 1;
					while(line != null || currentReadLine <= (totalLinesNumber + 1) ){

						//fill in "sbInput" before processing it
						if( (nbReadLinesInBlock - 1) < INPUT_FILE_LINE_NUMBER_BLOCK) {
							// => READ CASE FROM INPUT FILE (".tmpdata" file)
							sbInput.append(line);
							sbInput.append("\n");
							//read new line for next loop
							line = bufferedReader.readLine();
							nbReadLinesInBlock++;
							currentReadLine++;
						}
						//process "sbInput" when block is full
						else {
							// => WRITE CASE INTO OUTPUT FILE (".tmp" file)
							log.info(String.format("Processing %s / %s (line %s read)", currentBlockNumber, totalBlocksNumber, currentReadLine));
							String result = null;

							Pattern p0 = Pattern.compile(regExPatternsTab[0], Pattern.CASE_INSENSITIVE);
							Matcher m0 = p0.matcher(sbInput.toString());
							//System.out.println("Original String0: " + sbInput.toString()); //For debug purposes
							result = m0.replaceAll(regExPatternsTab[1]);
							//System.out.println("Modified String0: " + result); //For debug purposes

							//free RAM as soon as possible -> empty "sbInput"
							sbInput.delete(0, sbInput.length());

							//If there are boolColumns, subsequent regEx patterns must be applied :
							//NOTE : change "true" or "false" by "1" or "0"
							if(!boolColumnNames.isEmpty()) {
								//REMINDER : in previous process (outside current loop), we surrounded all bool columns
								//			 by "\"###" and "###\"".
								//1) Process empty entries in boolean columns
								Pattern p1 = Pattern.compile("\"######\"", Pattern.CASE_INSENSITIVE);
								Matcher m1 = p1.matcher(result);
								//System.out.println("Original String1: " + result); //For debug purposes
								result = m1.replaceAll("\"\"");
								//System.out.println("Modified String1: " + result); //For debug purposes

								//2) process "true" values
								Pattern p2 = Pattern.compile("\"###true###\"", Pattern.CASE_INSENSITIVE);
								Matcher m2 = p2.matcher(result);
								//System.out.println("Original String2: " + result); //For debug purposes
								result = m2.replaceAll("1");
								//System.out.println("Modified String2: " + result); //For debug purposes

								//3) process "false" values
								Pattern p3 = Pattern.compile("\"###false###\"", Pattern.CASE_INSENSITIVE);
								Matcher m3 = p3.matcher(result);
								//System.out.println("Original String3: " + result); //For debug purposes
								result = m3.replaceAll("0");
								//System.out.println("Modified String3: " + result); //For debug purposes
							}

							Files.write(tmpOutputFile,(result + "\n").getBytes(),StandardOpenOption.APPEND);

							//reset index AT THE END
							nbReadLinesInBlock = 1;
							//increment block number for next loop
							currentBlockNumber++;
						}
					}
				}

				Files.deleteIfExists(Path.of(tmpOutputFile + "data"));

				String outputFile = getOutputFolder().resolve(outputFileName(datasetName)).toString();
				//Move to output folder
				getFileUtilsInterface().moveFile(tmpOutputFile, outputFile);
				log.info(String.format("File: %s successfully written", outputFile));
				//Count rows for functional log
				if (kraftwerkExecutionContext != null) {
					try(ResultSet countResult =
								this.getDatabase().executeQuery("SELECT COUNT(*) FROM '%s'".formatted(datasetName))){
						countResult.next();
						kraftwerkExecutionContext.getLineCountByTableMap().put(datasetName, countResult.getInt(1));
					}
				}
			} catch (SQLException | IOException e) {
				throw new KraftwerkException(500, e.toString());
			}
		}
	}


	private String[] regExPatterns(List<String> columnNames, List<String> boolColumnNames, List<Integer> boolColumnIndexes) {
		String[] result = new String[2];

		//MAIN PATTERN : ALL NON-BOOLEAN FIELDS ARE SURROUNDED BY QUOTES
		//1) dynamically set regEx Pattern
		StringBuilder sbRegExPatternToFind = new StringBuilder();
		//sbRegExPatternToFind.append("^"); //DO NOT ADD THIS AS IT WILL ONLY PROCESS THE 1ST LINE!
		StringBuilder sbRegExPatternReplacement = new StringBuilder();
		int colIndex = 0;

		//If no boolean column at all, we simply add double quotes to all fields
		if(boolColumnNames.isEmpty()) {
			for(String colName : columnNames) {
				sbRegExPatternToFind.append("\"?([\\w\\-\\s\\/éèê]*)\"?");
				sbRegExPatternReplacement.append("\"$").append(colIndex + 1).append("\"");
				if( (colIndex + 1) < columnNames.size()) {
					sbRegExPatternToFind.append(";");
					sbRegExPatternReplacement.append(";");
				}
				colIndex++;
			}
		} else {
			//if there are boolColumns
			log.warn("boolColumns NOT EMPTY !");
			//for each column, we check if it is a boolean column or not
			for(String colName : columnNames) {
				if(boolColumnIndexes.contains(colIndex)) {
					sbRegExPatternToFind.append("\"?([\\w\\-\\s\\/éèê]*)\"?");
					//=> we FIRST surround boolean columns by "\"###" and "###\"" to be sure
					// not to further update "true" or "false" strings in fields which would NOT BE TAGGED as booleans.
					//NOTE : a subsequent process will be needed if there are boolColumns
					sbRegExPatternReplacement.append("\"###$").append(colIndex + 1).append("###\"");
				} else {
					sbRegExPatternToFind.append("\"?([\\w\\-\\s\\/éèê]*)\"?");
					//we add double quotes in case of boolean column
					sbRegExPatternReplacement.append("\"$").append(colIndex + 1).append("\"");
				}
				if( (colIndex + 1) < columnNames.size()) {
					sbRegExPatternToFind.append(";");
					sbRegExPatternReplacement.append(";");
				}
				colIndex++;
			}
		}

		//fill-in result object
		result[0] = sbRegExPatternToFind.toString();
		result[1] = sbRegExPatternReplacement.toString();

		return result;
	}
	//========= OPTIMISATIONS PERFS (END) ==========



	private static @NotNull StringBuilder getExportCsvQuery(String datasetName, File outputFile, List<String> columnNames) {
		StringBuilder exportCsvQuery = new StringBuilder(String.format("COPY %s TO '%s' (FORMAT CSV, HEADER false, DELIMITER '%s', OVERWRITE_OR_IGNORE true", datasetName, outputFile.getAbsolutePath() +"data", Constants.CSV_OUTPUTS_SEPARATOR));
		//Double quote values parameter
		exportCsvQuery.append(", FORCE_QUOTE(");
		for (String stringColumnName : columnNames) {
			exportCsvQuery.append(String.format("'%s',", stringColumnName));
		}
		//Remove last ","
		exportCsvQuery.deleteCharAt(exportCsvQuery.length() - 1);
		exportCsvQuery.append("))");
		return exportCsvQuery;
	}

	private static String buildHeader(List<String> columnNames, List<String> boolColumnNames, List<Integer> boolColumnIndexes) {
		StringBuilder headerBuilder = new StringBuilder();
		for (String columnName : columnNames) {
			headerBuilder.append(String.format("\"%s\"", columnName)).append(Constants.CSV_OUTPUTS_SEPARATOR);
			if(boolColumnNames.contains(columnName)){
				boolColumnIndexes.add(columnNames.indexOf(columnName));
			}
		}
		headerBuilder.deleteCharAt(headerBuilder.length()-1);
		headerBuilder.append("\n");
		return headerBuilder.toString();
	}

	/**
	 * replaces false/true by 0/1 in a line
	 * @param csvLine line to transform
	 * @param boolColumnIndexes indexes of booleans values to change
	 * @return the transformed line
	 */
	private String applyBooleanTransformations(String csvLine, List<Integer> boolColumnIndexes) {
		String[] lineElements = csvLine.split(String.valueOf(Constants.CSV_OUTPUTS_SEPARATOR), -1);
		//change "true" or "false" by "1" or "0"
		for (int elementIndex : boolColumnIndexes) {
			lineElements[elementIndex] = lineElements[elementIndex].replace("false", "0").replace("true", "1");
		}
		//Rebuild csv line
		return String.join(String.valueOf(Constants.CSV_OUTPUTS_SEPARATOR),lineElements);
	}

	/**
	 * Changes null values to "" in a line
	 * @param csvLine line to transform
	 * @return the transformed line
	 */
	private String applyNullTransformation(String csvLine) {
		String[] lineElements = csvLine.split(String.valueOf(Constants.CSV_OUTPUTS_SEPARATOR), -1);
		for (int i = 0; i < lineElements.length; i++) {
			if (lineElements[i].isEmpty()) {
				lineElements[i] = "\"\"";
			}
		}
		return String.join(String.valueOf(Constants.CSV_OUTPUTS_SEPARATOR),lineElements);
	}

	@Override
	public void writeImportScripts(Map<String, MetadataModel> metadataModels, KraftwerkExecutionContext kraftwerkExecutionContext) {
		// Assemble required info to write scripts
		List<TableScriptInfo> tableScriptInfoList = new ArrayList<>();
		for (String datasetName : getDatasetToCreate()) {
			TableScriptInfo tableScriptInfo = new TableScriptInfo(datasetName, outputFileName(datasetName),
					getVtlBindings().getDataset(datasetName).getDataStructure(), metadataModels);
			tableScriptInfoList.add(tableScriptInfo);
		}
		// Write scripts
		TextFileWriter.writeFile(getOutputFolder().resolve("import_with_data_table.R"),
				new RImportScript(tableScriptInfoList).generateScript(), getFileUtilsInterface());
		TextFileWriter.writeFile(getOutputFolder().resolve("import.sas"),
				new SASImportScript(tableScriptInfoList,
						kraftwerkExecutionContext == null ? new ArrayList<>() :
								kraftwerkExecutionContext.getErrors()
				).generateScript(), getFileUtilsInterface());
	}

	/**
	 * Return the name of the file to be written from the dataset name.
	 */
	@Override
	public String outputFileName(String datasetName) {
		return getOutputFolder().getParent().getFileName() + "_" + datasetName + ".csv";
	}

}
