package fr.insee.kraftwerk.core.utils;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.UserInputsFile;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SqlUtilsTest {
    private static Connection testDatabase;
    @BeforeAll
    static void init() throws SQLException {
        testDatabase = SqlUtils.openConnection();

    }


    @Test
    void convertVTLBindingTest() throws SQLException, KraftwerkException {
        try(Statement testDatabaseStatement = SqlUtils.openConnection().createStatement()) {
            //Given
            UserInputsFile testUserInputsFile = new UserInputsFile(
                    Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "user_inputs/inputs_valid_several_modes.json"),
                    Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "user_inputs"));
            VtlBindings vtlBindings = new VtlBindings();
            Dataset testDataset = new InMemoryDataset(List.of(),
                    List.of(new Structured.Component("TestString", String.class, Dataset.Role.IDENTIFIER)));
            for (String mode : testUserInputsFile.getModes()) {
                vtlBindings.put(mode, testDataset);
            }
            vtlBindings.put(testUserInputsFile.getMultimodeDatasetName(), testDataset);
            vtlBindings.put(Constants.ROOT_GROUP_NAME, testDataset);
            vtlBindings.put("LOOP", testDataset);
            vtlBindings.put("FROM_USER", testDataset);

            //Add data to root dataset
            Map<String, Object> dataRow = new HashMap<>();
            dataRow.put("TestString", "test");
            vtlBindings.getDataset(Constants.ROOT_GROUP_NAME).getDataPoints().add(new Structured.DataPoint(vtlBindings.getDataset(Constants.ROOT_GROUP_NAME).getDataStructure(), dataRow));

            //When
            SqlUtils.convertVtlBindingsIntoSqlDatabase(vtlBindings, testDatabaseStatement);

            //Then
            //1 table / dataset
            ResultSet resultSet = testDatabaseStatement.executeQuery("SHOW TABLES");
            List<String> tableNames = new ArrayList<>();
            while (resultSet.next()) {
                tableNames.add(resultSet.getString("name"));
            }
            for (String datasetName : vtlBindings.getDatasetNames()) {
                Assertions.assertThat(tableNames).contains(datasetName);
            }

            //Table has data
            resultSet = testDatabaseStatement.executeQuery("SELECT * FROM " + Constants.ROOT_GROUP_NAME);
            Assertions.assertThat(resultSet.next()).isTrue();
        }
    }

    @Test
    void getColumnNamesTest() throws SQLException {
        try(Statement testDatabaseStatement = SqlUtils.openConnection().createStatement()){
            //Given
            testDatabaseStatement.execute("CREATE TABLE columnnamestesttable(testint INT, teststring NVARCHAR)");

            //When
            List<String> columnNames = SqlUtils.getColumnNames(testDatabaseStatement, "columnnamestesttable");

            //Then
            Assertions.assertThat(columnNames).containsExactly("testint", "teststring");
        }
    }

    @Test
    void getAllDataTest() throws SQLException {
        try(Statement testDatabaseStatement = SqlUtils.openConnection().createStatement()) {
            //Given
            testDatabaseStatement.execute("CREATE TABLE getdatatesttable(testint INT, teststring NVARCHAR)");
            testDatabaseStatement.execute("INSERT INTO getdatatesttable VALUES (1, 'test1')");
            testDatabaseStatement.execute("INSERT INTO getdatatesttable VALUES (2, 'test2')");
            testDatabaseStatement.execute("INSERT INTO getdatatesttable VALUES (3, 'test3')");

            //When
            ResultSet resultSet = SqlUtils.getAllData(testDatabaseStatement, "getdatatesttable");

            //Then
            int i = 1;
            while (resultSet.next()){
                Assertions.assertThat(resultSet.getInt("testint")).isEqualTo(i);
                Assertions.assertThat(resultSet.getString("teststring")).isEqualTo("test"+i);
                i++;
            }
        }
    }

    @Test
    void readCsvFileTest() throws SQLException {
        try(Statement testDatabaseStatement = SqlUtils.openConnection().createStatement()) {
            //Given
            Path csvFilePath = Path.of(TestConstants.UNIT_TESTS_DIRECTORY).resolve("csv").resolve("testcsv.csv");
            //When
            SqlUtils.readCsvFile(testDatabaseStatement, csvFilePath);

            //Then
            ResultSet resultSet = testDatabaseStatement.executeQuery("SELECT * FROM testcsv");
            Assertions.assertThat(resultSet.next()).isTrue();
            Assertions.assertThat(resultSet.getString("teststring1")).isEqualTo("test1");
            Assertions.assertThat(resultSet.getString("teststring2")).isEqualTo("test2");
            Assertions.assertThat(resultSet.getString("testint1")).isEqualTo("1");
        }
    }

    @Test
    void readParquetFileTest() throws SQLException {
        try(Statement testDatabaseStatement = SqlUtils.openConnection().createStatement()) {
            //Given
            Path parquetFilePath = Path.of(TestConstants.UNIT_TESTS_DIRECTORY).resolve("parquet").resolve("testparquet.parquet");
            //When
            SqlUtils.readParquetFile(testDatabaseStatement, parquetFilePath);

            //Then
            ResultSet resultSet = testDatabaseStatement.executeQuery("SELECT * FROM testparquet");
            Assertions.assertThat(resultSet.next()).isTrue();
            Assertions.assertThat(resultSet.getString("teststring1")).isEqualTo("test1");
            Assertions.assertThat(resultSet.getString("teststring2")).isEqualTo("test2");
            Assertions.assertThat(resultSet.getInt("testint1")).isEqualTo(1);
        }
    }



    @AfterAll
    static void closeConnection() throws SQLException {
        testDatabase.close();
    }
}
