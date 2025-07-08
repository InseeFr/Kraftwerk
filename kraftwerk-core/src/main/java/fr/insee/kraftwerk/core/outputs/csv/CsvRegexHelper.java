package fr.insee.kraftwerk.core.outputs.csv;

import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Adrien Marchal
 * Class used to make transformations on CSV content with usage of RegEx (Regular Expression), for performance optimization purpose
 */
@Slf4j
public class CsvRegexHelper {

    private static final String REGULAR_EXPRESSION = "\"?([\\w\\- \\/éèê\\.àçù]*)\"?";
    private static final String BOOLEAN_FIELD_IDENTIFIER_START = "¤¤¤";
    private static final String BOOLEAN_FIELD_IDENTIFIER_END = "¤¤¤";

    /**
     *
     * @param tmpOutputFile file in which we write dada transformed by Regex
     * @param columnNames CSV colum names
     * @param boolColumnNames CSV colum names typed as boolean
     * @param boolColumnIndexes indexes of CSV boolean columns
     * @throws KraftwerkException runtime exception
     */
    static void writeIntoTmpFile(Path tmpOutputFile, List<String> columnNames, List<String> boolColumnNames, List<Integer> boolColumnIndexes) throws KraftwerkException {
        try {
            //!!!WARNING!!! : !!!REGEX!!! TRANSFORMATION FOR PERFORMANCES OPTIMISATIONS
            String[] regExPatternsTab = regExPatterns(columnNames, boolColumnNames, boolColumnIndexes);
            log.info("sbRegExPatternToFind : {}", regExPatternsTab[0]);
            log.info("sbRegExPatternReplacement : {}", regExPatternsTab[1]);


            //In order to be aware of the process progress, we count how much lines the file contains & how many blocks must be processed
            int totalLinesNumber = 0;
            int INPUT_FILE_LINE_NUMBER_BLOCK = 50;
            try(BufferedReader bufferedReader = Files.newBufferedReader(Path.of(tmpOutputFile.toAbsolutePath() + "data"))) {
                String line = bufferedReader.readLine();
                while (line != null) {
                    totalLinesNumber++;
                    line = bufferedReader.readLine();
                }
            }
            int totalBlocksNumber = totalLinesNumber / INPUT_FILE_LINE_NUMBER_BLOCK == 0 ? 1 : totalLinesNumber / INPUT_FILE_LINE_NUMBER_BLOCK;
            log.info("{} lines ({} blocks)", totalLinesNumber, totalBlocksNumber);

            // => READING DATA FROM ".tmpdata" file BY BLOCK OF 50 LINES AND WRITING FORMATTED DATA INTO ".tmp" file
            int currentBlockNumber = 1;
            StringBuilder sbInput = new StringBuilder();
            try(BufferedReader bufferedReader = Files.newBufferedReader(Path.of(tmpOutputFile.toAbsolutePath() + "data"))){
                String line = bufferedReader.readLine();
                int nbReadLinesInBlock = 1; //Nb of lines read in the current block
                int currentReadLine = 1; //Nb of lines read in the whole file
                while(line != null){
                    //fill in "sbInput" before processing it
                    // => READ CASE FROM INPUT FILE (".tmpdata" file)
                    sbInput.append(line);

                    //process "sbInput" when block is full of end of file is reached
                    if (nbReadLinesInBlock >= INPUT_FILE_LINE_NUMBER_BLOCK || currentReadLine >= totalLinesNumber) {
                        // => WRITE CASE INTO OUTPUT FILE (".tmp" file)
                        if(currentReadLine % 1000 == 0) {
                            log.info("Processing {} / {} (line {} read)", currentBlockNumber, totalBlocksNumber, currentReadLine);
                        }
                        String result = applyRegExOnBlockFile(sbInput, regExPatternsTab, boolColumnNames);

                        Files.write(tmpOutputFile,(result).getBytes(), StandardOpenOption.APPEND);

                        //free RAM as soon as possible -> empty "sbInput"
                        sbInput.delete(0, sbInput.length());
                        //reset index AT THE END
                        nbReadLinesInBlock = 0;
                        //increment block number for next loop
                        currentBlockNumber++;
                    }

                    //read new line for next loop
                    line = bufferedReader.readLine();
                    nbReadLinesInBlock++;
                    currentReadLine++;
                    //We insert a carriage return ONLY IF END OF FILE IS NOT REACHED!
                    if(line != null) {
                        sbInput.append("\n");
                    }
                }
            }
        } catch (IOException e) {
            throw new KraftwerkException(500, e.toString());
        }
    }

    private static String applyRegExOnBlockFile(StringBuilder sbInput, String[] regExPatternsTab, List<String> boolColumnNames) {
        String result;

        Pattern p0 = Pattern.compile(regExPatternsTab[0], Pattern.CASE_INSENSITIVE);
        Matcher m0 = p0.matcher(sbInput.toString());
        result = m0.replaceAll(regExPatternsTab[1]);

        //If there are boolColumns, subsequent regEx patterns must be applied :
        //NOTE : change "true" or "false" by "1" or "0"
        if(!boolColumnNames.isEmpty()) {
            //REMINDER : in previous process (outside current loop), we surrounded all bool columns
            //			 by "\"¤¤¤" and "¤¤¤\"".
            //1) Process empty entries in boolean columns
            Pattern p1 = Pattern.compile("\"" + BOOLEAN_FIELD_IDENTIFIER_START + BOOLEAN_FIELD_IDENTIFIER_END + "\"", Pattern.CASE_INSENSITIVE);
            Matcher m1 = p1.matcher(result);
            result = m1.replaceAll("\"\"");

            //2) process "true" values
            Pattern p2 = Pattern.compile("\"" + BOOLEAN_FIELD_IDENTIFIER_START + "true" + BOOLEAN_FIELD_IDENTIFIER_END + "\"", Pattern.CASE_INSENSITIVE);
            Matcher m2 = p2.matcher(result);
            result = m2.replaceAll("\"1\"");

            //3) process "false" values
            Pattern p3 = Pattern.compile("\"" + BOOLEAN_FIELD_IDENTIFIER_START + "false" + BOOLEAN_FIELD_IDENTIFIER_END + "\"", Pattern.CASE_INSENSITIVE);
            Matcher m3 = p3.matcher(result);
            result = m3.replaceAll("\"0\"");
        }

        return result;
    }


    private static String[] regExPatterns(List<String> columnNames, List<String> boolColumnNames, List<Integer> boolColumnIndexes) {
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
                sbRegExPatternToFind.append(REGULAR_EXPRESSION);
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
                    sbRegExPatternToFind.append(REGULAR_EXPRESSION);
                    //=> we FIRST surround boolean columns by "\"¤¤¤" and "¤¤¤\"" to be sure
                    // not to further update "true" or "false" strings in fields which would NOT BE TAGGED as booleans.
                    //NOTE : a subsequent process will be needed if there are boolColumns
                    sbRegExPatternReplacement.append("\"" + BOOLEAN_FIELD_IDENTIFIER_START + "$").append(colIndex + 1).append(BOOLEAN_FIELD_IDENTIFIER_END + "\"");
                } else {
                    sbRegExPatternToFind.append(REGULAR_EXPRESSION);
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

}
