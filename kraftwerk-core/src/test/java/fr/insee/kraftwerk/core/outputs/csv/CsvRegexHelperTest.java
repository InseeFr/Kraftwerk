package fr.insee.kraftwerk.core.outputs.csv;

import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CsvRegexHelperTest {

    private static final String REGEX_1_COLUMN = CsvRegexHelper.REGULAR_EXPRESSION;  //!!!WARNING!!! potential bug when there is ONLY ONE NOT-EMPTY COLUMN!
    private static final String REGEX_2_COLUMNS = CsvRegexHelper.REGULAR_EXPRESSION + ";" + CsvRegexHelper.REGULAR_EXPRESSION;
    private static final String REGEX_REPLACEMENT_2_COLUMNS = "\"$1\";\"$2\"";
    private static final String REGEX_3_COLUMNS = CsvRegexHelper.REGULAR_EXPRESSION + ";" +
                                              CsvRegexHelper.REGULAR_EXPRESSION + ";" +
                                              CsvRegexHelper.REGULAR_EXPRESSION;
    private static final String REGEX_REPLACEMENT_3_COLUMNS = "\"$1\";\"$2\";\"$3\"";
    private static final String REGEX_BOOL_3_COLUMNS = CsvRegexHelper.REGULAR_EXPRESSION + ";" +
                                                    CsvRegexHelper.REGULAR_EXPRESSION + ";" +
                                                    CsvRegexHelper.REGULAR_EXPRESSION;
    private static final String REGEX_BOOL_REPLACEMENT_3_COLUMNS = "\"$1\";\"¤¤¤$2¤¤¤\";\"$3\"";


    @Test
    void writeIntoTmpFile_Performance_Test(@TempDir Path tempDir) throws IOException, KraftwerkException {
        Path file = tempDir.resolve("temp_file.csv");
        Path fileData = tempDir.resolve("temp_file.csvdata");
        //Prepare content for Performance Tests

        String header = "\"interrogationId\";\"BOOL1\";\"BOOL2\";\"BOOL3\";\"BOOL4\";\"CALCUL_VOYAGES\";\"test_mois\";\"HABLOGHK\";\"T_EMPLOI\";\"T_ACTIVANTE2\";\"PROFESSIONCLAIR_NORM\";\"PROFESSIONCLAIR_FLOUS\";\"PMV\";\"PMAR\";\"IL_ELLE\";\"HF_MIN\";\"HF_MAJ\";\"E\";\"MOIS1S\";\"MOIS2S\";\"MOIS3S\";\"MOISREI\";\"MOIS1L\";\"MOIS2L\";\"MOIS3L\";\"MOIS1SL\";\"MOIS2SL\";\"MOIS3SL\";\"MOISREIL\";\"MOIS2\";\"MOIS3\";\"MOISMIN\";\"MOISMAX\";\"JMAXR\";\"JMAXD\";\"ANNEEPREC\";\"ANNEESUIV\";\"DATEVDMIN\";\"DATEVDMAX\";\"DATEVRMAX\";\"ANNEEMIN\";\"ANNEEINT\";\"ILS_ELLES\";\"F_FFE\";\"F_VE\";\"NE\";\"IER_IERE\";\"PREN_IND\";\"ADRESSE_FIDELI\";\"PREN_F\";\"MOIS1\";\"ANNEE\";\"NUMERO_ADRESSE_FIDELI\";\"REPETITION_FIDELI\";\"LIBELLE_VOIE_FIDELI\";\"MS_ADRESSE\";\"COMPLEMENT_ADRESSE_FIDELI\";\"CODE_POSTAL_FIDELI\";\"LIBELLE_COMMUNE_FIDELI\";\"PREN_S\";\"KISH\";\"PRENOM_REPONDANT\";\"QUI_REPOND\";\"CONFIRM_ADRESSE\";\"ADR_FRANCE\";\"NUMERO_ADRESSE_I1\";\"REPETITION_ADRESSE_I1\";\"LIBELLE_VOIE_I1\";\"COMPLEMENT_ADRESSE_I1\";\"CODE_POSTAL_I1\";\"LIBELLE_COMMUNE_I1\";\"SEXE\";\"DATENAIS\";\"ANNNAIS_IND\";\"LNAIS\";\"PAYSNAIS\";\"ANNEE_INSTAL_FR\";\"NATIONALITE\";\"COUPLE\";\"ETATMATRI\";\"DIPLOME\";\"T_SITUAEU\";\"T_TRAVAIL\";\"T_ACTIVANTE\";\"T_PROFESSIONF\";\"T_PROFESSIONH\";\"T_PROFESSIONCLAIR\";\"T_PROFESSIONCLAIRP\";\"T_STCPUB\";\"POSITION_PUBLIC\";\"POSITION_PRIVE\";\"NBSAL\";\"T_ACTIV\";\"T_ACTIVCLAIR\";\"HAB_LOG\";\"OCCUPANT_LOGT1\";\"OCCUPANT_LOGT2\";\"OCCUPANT_LOGT3\";\"OCCUPANT_LOGT4\";\"OCCUPANT_LOGT5\";\"OCCUPANT_LOGT6\";\"OCCUPANT_LOGT7\";\"OCCUPANT_LOGT8\";\"OCCUPANT_LOGT9\";\"SEJOUR_ETRANGER\";\"RETOUR_FRANCE\";\"RES_SECOND\";\"FREQUENCE_DEPL\";\"NB_VOY_R2\";\"NB_VOY_PROS\";\"NB_VOY_PERSOS\";\"NB_AR_PRO\";\"NB_AR_PRO_ETR\";\"NB_AR_PERSO\";\"NB_AR_PERSO_ETR\";\"TEMPSITW\";\"COMMENT_QE\";\"PRENOM_SAISI_MISSING\";\"KISH_MISSING\";\"PRENOM_REPONDANT_MISSING\";\"QUI_REPOND_MISSING\";\"CONFIRM_ADRESSE_MISSING\";\"ADR_FRANCE_MISSING\";\"NUMERO_ADRESSE_I1_MISSING\";\"REPETITION_ADRESSE_I1_MISSING\";\"LIBELLE_VOIE_I1_MISSING\";\"COMPLEMENT_ADRESSE_I1_MISSING\";\"CODE_POSTAL_I1_MISSING\";\"LIBELLE_COMMUNE_I1_MISSING\";\"SEXE_MISSING\";\"DATENAIS_MISSING\";\"ANNNAIS_IND_MISSING\";\"LNAIS_MISSING\";\"PAYSNAIS_MISSING\";\"ANNEE_INSTAL_FR_MISSING\";\"NATIONALITE_MISSING\";\"COUPLE_MISSING\";\"ETATMATRI_MISSING\";\"DIPLOME_MISSING\";\"SITUA_PRINC_MISSING\";\"TRAVAIL_MISSING\";\"ACTIVANTE_MISSING\";\"PROFESSIONF_MISSING\";\"PROFESSIONH_MISSING\";\"PROFESSIONCLAIR_MISSING\";\"PROFESSIONCLAIRP_MISSING\";\"STCPUB_MISSING\";\"POS_PUBLIC_MISSING\";\"POS_PRIVE_MISSING\";\"TAILL_INDEP_MISSING\";\"T_ACTIV_MISSING\";\"T_ACTIVCLAIR_MISSING\";\"HAB_LOG_MISSING\";\"OCCUPANT_LOGT_MISSING\";\"PRENOM_HL_MISSING\";\"SEJOUR_ETRANGER_MISSING\";\"RETOUR_FRANCE_MISSING\";\"RES_SECOND_MISSING\";\"FREQUENCE_DEPL_MISSING\";\"VR2_MISSING\";\"NB_VOY_PROS_MISSING\";\"NB_VOY_PERSOS_MISSING\";\"NB_AR_PRO_MISSING\";\"NB_AR_PRO_ETR_MISSING\";\"NB_AR_PERSO_MISSING\";\"NB_AR_PERSO_ETR_MISSING\";\"TEMPSITW_MISSING\";\"COMMENT_QE_MISSING\"";
        Files.write(fileData, header.getBytes());
        Files.write(fileData, "\n".getBytes(), StandardOpenOption.APPEND);
        Files.write(file, header.getBytes());
        Files.write(file, "\n".getBytes(), StandardOpenOption.APPEND);

        List<String> colNames = Arrays.asList(header.split(";"));
        List<String> boolColNames = Arrays.asList("BOOL1", "BOOL2", "BOOL3", "BOOL4");
        List<Integer> boolColIndexes = List.of(new Integer[]{1, 2, 3, 4});

        int nbLines = 50000;
        int blockSize = 50;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= nbLines; i++) {
            sb.append("\"UE00000000" + i + "\";true;false;;\"\";\"1\";\"04\";\"0\";\"\";\"T_ACTIVANTE" + i + "_C\";\"\";\"\";\"de \";\"de \";\"elle\";\"femme\";\"Femme\";\"e\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"ils / elles\";\"fe\";\"Veuve\";\"ne\";\"ière\";\"\";\"      \";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"KISH" + i + "_C\";\"\";\"\";\"CONFIRM_ADRESSE" + i + "_2C\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"SEXE" + i + "_C\";\"DATENAIS" + i + "_2C\";\"\";\"LNAIS" + i + "_2C\";\"\";\"\";\"NATIONALITE" + i + "_C\";\"COUPLE" + i + "_C\";\"ETATMATRI" + i + "_C\";\"DIPLOME" + i + "_2C\";\"T_SITUAEU" + i + "_2C\";\"T_TRAVAIL" + i + "_2C\";\"T_ACTIVANTE" + i + "_C\";\"T_PROFESSIONF" + i + "_C\";\"\";\"\";\"\";\"T_STCPUB" + i + "_2C\";\"\";\"POSITION_PRIVE" + i + "_2C\";\"\";\"T_ACTIV" + i + "_C\";\"\";\"1\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"SEJOUR_ETRANGER" + i + "_2C\";\"\";\"RES_SECOND" + i + "_2C\";\"FREQUENCE_DEPL" + i + "_C\";\"1\";\"\";\"0\";\"\";\"\";\"0\";\"\";\"1\";\"COMMENT_QE" + i + "_C\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\"");
            sb.append("\n");
            if(i % blockSize == 0){
                Files.write(fileData, sb.toString().getBytes(), StandardOpenOption.APPEND);
                sb.delete(0, sb.length());
            }
        }

        long startTime = System.currentTimeMillis();
        CsvRegexHelper.writeIntoTmpFile(file, colNames, boolColNames, boolColIndexes);
        long endTime = System.currentTimeMillis();
        long delta = endTime - startTime;
        System.out.println("delta : " + delta);

        //Assert that the process on 50k lines takes less than 20s!
        assertTrue(delta < 20000);
    }

    private static Stream<Arguments> applyRegExParameterizedTests() {
        return Stream.of(
                Arguments.of(REGEX_1_COLUMN, "\"$1\"", false, "", "\"\""),
                //Arguments.of(REGEX_1_COLUMN, "\"$1\"", false, "\"aaa\"", "\"aaa\""), //!!!WARNING!!! potential bug when there is ONLY ONE NOT-EMPTY COLUMN!
                // //Arguments.of(REGEX_1_COLUMN, "\"$1\"", false, "aaa", "\"aaa\""), //!!!WARNING!!! potential bug when there is ONLY ONE NOT-EMPTY COLUMN!
                Arguments.of(REGEX_2_COLUMNS, REGEX_REPLACEMENT_2_COLUMNS, false, "aaa;bbb", "\"aaa\";\"bbb\""),
                Arguments.of(REGEX_2_COLUMNS, REGEX_REPLACEMENT_2_COLUMNS, false, "\"aaa\";\"bbb\"", "\"aaa\";\"bbb\""),
                Arguments.of(REGEX_3_COLUMNS, REGEX_REPLACEMENT_3_COLUMNS, false, "aaa;bbb;ccc", "\"aaa\";\"bbb\";\"ccc\""),
                Arguments.of(REGEX_3_COLUMNS, REGEX_REPLACEMENT_3_COLUMNS, false, "\"aaa\";\"bbb\";\"ccc\"", "\"aaa\";\"bbb\";\"ccc\""),
                Arguments.of(REGEX_BOOL_3_COLUMNS, REGEX_BOOL_REPLACEMENT_3_COLUMNS, true, "aaa;;bbb", "\"aaa\";\"\";\"bbb\""),
                Arguments.of(REGEX_BOOL_3_COLUMNS, REGEX_BOOL_REPLACEMENT_3_COLUMNS, true, "\"aaa\";\"\";\"bbb\"", "\"aaa\";\"\";\"bbb\""),
                Arguments.of(REGEX_BOOL_3_COLUMNS, REGEX_BOOL_REPLACEMENT_3_COLUMNS, true, "aaa;true;bbb", "\"aaa\";\"1\";\"bbb\""),
                Arguments.of(REGEX_BOOL_3_COLUMNS, REGEX_BOOL_REPLACEMENT_3_COLUMNS, true, "\"aaa\";\"true\";\"bbb\"", "\"aaa\";\"1\";\"bbb\""),
                Arguments.of(REGEX_BOOL_3_COLUMNS, REGEX_BOOL_REPLACEMENT_3_COLUMNS, true, "aaa;false;bbb", "\"aaa\";\"0\";\"bbb\""),
                Arguments.of(REGEX_BOOL_3_COLUMNS, REGEX_BOOL_REPLACEMENT_3_COLUMNS, true, "\"aaa\";\"false\";\"bbb\"", "\"aaa\";\"0\";\"bbb\"")
        );
    }

    @ParameterizedTest
    @MethodSource("applyRegExParameterizedTests")
    void applyRegExOnBlockFile_test(String regExToFind, String regExReplacement, boolean boolColumns, String input, String expectedResult) {
        StringBuilder sbInput = new StringBuilder();
        sbInput.append(input);
        List<String> boolColumnNames = new ArrayList<>();
        if(boolColumns) {
            boolColumnNames.add("col_B");
        }
        String result = CsvRegexHelper.applyRegExOnBlockFile(sbInput, regExToFind, regExReplacement, boolColumnNames);
        assertEquals(expectedResult, result);
    }


    private static Stream<Arguments> regExPatternsParameterizedTests() {
        return Stream.of(
                Arguments.of(2, 0, REGEX_2_COLUMNS, REGEX_REPLACEMENT_2_COLUMNS),
                Arguments.of(3, 0, REGEX_3_COLUMNS, REGEX_REPLACEMENT_3_COLUMNS),
                Arguments.of(3, 1, REGEX_BOOL_3_COLUMNS, REGEX_BOOL_REPLACEMENT_3_COLUMNS)
        );
    }

    @ParameterizedTest
    @MethodSource("regExPatternsParameterizedTests")
    void regExPatternsOnBlockFile_test(int nbColumns, int nbBoolColumns, String regExToFind, String regExReplacement) {
        List<String> columnNames = new ArrayList<>();
        for (int i = 0; i < nbColumns; i++) {
            columnNames.add("col_" + i);
        }
        List<Integer> boolColumnIndexes = new ArrayList<>();
        if(nbBoolColumns > 0) {
            boolColumnIndexes.add(1);
        }

        String regExPatternToFind = CsvRegexHelper.buildRegExPatternToFind(columnNames);
        String regExPatternReplacement = CsvRegexHelper.buildRegExPatternReplacement(columnNames, boolColumnIndexes);
        assertEquals(regExToFind, regExPatternToFind);
        assertEquals(regExReplacement, regExPatternReplacement);
    }

}
