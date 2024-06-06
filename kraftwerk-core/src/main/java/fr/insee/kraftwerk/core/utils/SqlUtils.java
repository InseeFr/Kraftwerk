package fr.insee.kraftwerk.core.utils;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Structured;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class SqlUtils {

    private SqlUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Convert vtl bindings to SQL DuckDB tables for further export
     * @param vtlBindings vtl bindings to send into database
     * @param statement statement associated to database
     */
    public static void convertVtlBindingsIntoSqlDatabase(VtlBindings vtlBindings, Statement statement) {
        try {
            createOutputSQLTables(statement, vtlBindings);
        } catch (SQLException e) {
            log.error("SQL Error during VTL bindings conversion :\n{}",e.toString());
        }
    }


    /**
     * send CREATE TABLE queries into DB
     *
     * @param statement   DuckDB connection
     * @param vtlBindings datasets to convert
     * @throws SQLException if sql error
     */
    private static void createOutputSQLTables(Statement statement, VtlBindings vtlBindings) throws SQLException {
        for (String datasetName : vtlBindings.getDatasetNames()) {
            Map<String, VariableType> sqlSchema = extractSqlSchema(vtlBindings.getDataset(datasetName).getDataStructure());

            if (sqlSchema.isEmpty()) {
                log.warn("Empty schema for dataset {}", datasetName);
                return;
            }
            StringBuilder createTableQuery = new StringBuilder(String.format("CREATE TABLE '%s' (", datasetName));

            //Column order map to use in INSERT VALUES statement
            List<String> schemaOrder = extractColumnsOrder(vtlBindings.getDataset(datasetName));

            for (String columnName : schemaOrder) {
                createTableQuery.append("\"").append(columnName).append("\"").append(" ").append(sqlSchema.get(columnName).getSqlType());
                createTableQuery.append(", ");
            }

            //Remove last delimiter and replace by ")"
            createTableQuery.delete(createTableQuery.length() - 2, createTableQuery.length());
            createTableQuery.append(")");

            log.debug("SQL Query : {}", createTableQuery);
            statement.execute(createTableQuery.toString());

            insertDataIntoTable(statement, datasetName, vtlBindings.getDataset(datasetName), sqlSchema, schemaOrder);
        }
    }

    /**
     * Extract variable types from a dataset
     * @param structure the structure of the dataset
     * @return a (variable name,type) map
     */
    private static Map<String, VariableType> extractSqlSchema(Structured.DataStructure structure) {
        //Deduplicate column names by case
        Set<String> deduplicatedColumns = new HashSet<>();
        for(Structured.Component component : structure.values()) {
            deduplicatedColumns.add(component.getName().toLowerCase());
        }

        Map<String, VariableType> schema = new HashMap<>();
        for (Structured.Component component : structure.values()) {
            VariableType type = VariableType.getTypeFromJavaClass(component.getType());
            if (type != null){
                //If column not added yet
                if(deduplicatedColumns.contains(component.getName().toLowerCase())){
                    schema.put(component.getName(), type);
                    deduplicatedColumns.remove(component.getName().toLowerCase());
                }
            } else {
                log.warn("Cannot export variable {} to SQL, unrecognized type", component.getName());
            }
        }
        return schema;
    }

    /**
     * Extract columns order from a VTL dataset
     * @return a list of columns names in the right order
     */
    private static List<String> extractColumnsOrder(Dataset dataset){
        List<String> columnsOrder = new ArrayList<>();
        for(Structured.Component component : dataset.getDataStructure().values()){
            columnsOrder.add(component.getName());
        }

        return columnsOrder;
    }

    /**
     * INSERT queries into a specific table
     *
     * @param statement DuckDB connection
     * @param dataset   dataset to convert
     * @param schema    schema of the table
     */
    private static void insertDataIntoTable(Statement statement, String datasetName, Dataset dataset, Map<String, VariableType> schema, List<String> schemaOrder) throws SQLException {
        if (dataset.getDataAsMap().isEmpty()) {
            return;
        }
        StringBuilder insertDataQuery = new StringBuilder("BEGIN TRANSACTION;\n");
        insertDataQuery.append(String.format("INSERT INTO \"%s\" VALUES ", datasetName));

        //For each row of the dataset
        for (Map<String, Object> dataRow : dataset.getDataAsMap()) {
            insertDataQuery.append("(");
            for (String columnName : schemaOrder) {
                String data = dataRow.get(columnName) == null ? "null" : dataRow.get(columnName).toString();
                if (schema.get(columnName).equals(VariableType.STRING) && dataRow.get(columnName) != null) {
                    data = String.format("$$%s$$", data); //Surround with dollar quotes if string
                }
                insertDataQuery.append(data).append(",");
            }
            insertDataQuery.delete(insertDataQuery.length() - 1, insertDataQuery.length());
            insertDataQuery.append("),");
        }
        insertDataQuery.delete(insertDataQuery.length() - 1, insertDataQuery.length()).append(";"); //Replace last "," by ";"
        insertDataQuery.append("COMMIT;");
        log.debug("SQL Query : {}", insertDataQuery);
        statement.execute(insertDataQuery.toString());
    }

    /**
     * Opens an in-memory duckdb connection
     * WARNING : Close the connection when finished or surround with try with ressources !
     * @return a Statement object associated to this connection
     */
    public static Connection openConnection() throws SQLException {
        return DriverManager.getConnection(Constants.DUCKDB_URL);
    }

    /**
     * Opens a file duckdb connection
     * WARNING : Close the connection when finished or surround with try with ressources !
     * @param databaseFilePath path of the databae
     * @return a Statement object associated to this connection
     */
    public static Connection openConnection(Path databaseFilePath) throws SQLException {
        try {
            Files.createDirectories(Path.of("duckdb/"));
            return DriverManager.getConnection(Constants.DUCKDB_URL + databaseFilePath);
        } catch (IOException e) {
            log.error("IO error during file duckdb connection !");
            log.error(e.toString());
            return null;
        }
    }

    /**
     * Opens a duckdb connection with URL
     * WARNING : Close the connection when finished or surround with try with ressources !
     * @param databaseURL Url of duckdb database
     * @return a Statement object associated to this connection
     */
    public static Connection openConnection(String databaseURL) throws SQLException {
        return DriverManager.getConnection(databaseURL);
    }

    /**
     * Connect to DuckDB and retrieve column names of a table
     *
     * @param tableName name of table to retrieve column names
     * @return table columns names
     * @throws SQLException if SQL error
     */
    public static List<String> getColumnNames(Statement statement, String tableName) throws SQLException {
        ResultSet resultSet = statement.executeQuery(String.format("DESCRIBE \"%s\"", tableName));
        List<String> columnNames = new ArrayList<>();
        while (resultSet.next()) {
            columnNames.add(resultSet.getString("column_name"));
        }
        return columnNames;
    }

    /**
     * Connect to DuckDB and retrieve column names of a table
     *
     * @param tableName name of table to retrieve column names
     * @param statement database connection
     * @return data table column names
     * @throws SQLException if SQL error
     */
    public static ResultSet getAllData(Statement statement, String tableName) throws SQLException {
        return statement.executeQuery(String.format("SELECT * FROM \"%s\"", tableName));
    }

    /**
     * Connect to DuckDB and import CSV file in a table named after the file
     * @param statement database connection
     * @param filePath path of the file to import into duckdb
     */
    public static void readCsvFile(Statement statement, Path filePath) throws SQLException {
        statement.execute(String.format("CREATE TABLE '%s' AS SELECT * FROM read_csv('%s')", filePath.getFileName().toString().split("\\.")[0], filePath));
    }

    /**
     * Connect to DuckDB and import PARQUET file in a table named after the file
     * @param statement database connection
     * @param filePath path of the file to import into duckdb
     */
    public static void readParquetFile(Statement statement, Path filePath) throws SQLException {
        statement.execute(String.format("CREATE TABLE '%s' AS SELECT * FROM read_parquet('%s')", filePath.getFileName().toString().split("\\.")[0], filePath));
    }

}