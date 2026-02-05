package fr.insee.kraftwerk.core.utils;

import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Structured;
import lombok.extern.slf4j.Slf4j;
import org.duckdb.DuckDBAppender;
import org.duckdb.DuckDBConnection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
            for (String datasetName : vtlBindings.getDatasetNames()) {
                //Variables types map
                LinkedHashMap<String, VariableType> sqlSchema = extractSqlSchema(vtlBindings.getDataset(datasetName).getDataStructure());
                createDataSQLTables(statement, datasetName, sqlSchema);
                insertDataIntoTable(statement, datasetName, vtlBindings.getDataset(datasetName), sqlSchema);
            }
        } catch (SQLException e) {
            log.error("SQL Error during VTL bindings conversion :\n{}",e.toString());
        }
    }


    /**
     * send CREATE TABLE query into DB for a dataset
     *
     * @param statement   DuckDB connection
     * @param datasetName dataset to convert
     * @param sqlSchema schema of dataset
     * @throws SQLException if sql error
     */
    private static void createDataSQLTables(
            Statement statement,
            String datasetName,
            LinkedHashMap<String, VariableType> sqlSchema
    ) throws SQLException {

        //Skip if no variable
        if (sqlSchema.isEmpty()) {
            log.warn("Empty schema for dataset {}", datasetName);
            return;
        }

        //Don't CREATE if table already exists (ex: file-by-file)
        List<String> tableNames = getTableNames(statement);
        if (!tableNames.contains(datasetName)) {
            String createTableQuery = getCreateTableQuery(datasetName, sqlSchema);

            //Execute query
            log.debug("SQL Query : {}", createTableQuery);
            statement.execute(createTableQuery);
            return;
        }
        //add missing columns if necessary
        LinkedHashMap<String, VariableType> variablesToAdd = getVariablesToAdd(statement, datasetName, sqlSchema);
        if(variablesToAdd.isEmpty()){
            return;
        }
        String updateTableQuery = getUpdateTableQuery(datasetName, variablesToAdd);
        statement.execute(updateTableQuery);
    }

    private static String getCreateTableQuery(String datasetName, LinkedHashMap<String, VariableType> variablesToAdd) {
        //CREATE query building
        StringBuilder createTableQuery = new StringBuilder(String.format("CREATE TABLE \"%s\" (", datasetName));

        for (Map.Entry<String, VariableType> column : variablesToAdd.entrySet()) {
            createTableQuery.append("\"").append(column.getKey()).append("\"").append(" ").append(variablesToAdd.get(column.getKey()).getSqlType());
            createTableQuery.append(", ");
        }

        //Remove last delimiter and replace by ")"
        createTableQuery.delete(createTableQuery.length() - 2, createTableQuery.length());
        createTableQuery.append(")");
        return createTableQuery.toString();
    }

    private static LinkedHashMap<String, VariableType> getVariablesToAdd(
            Statement statement,
            String datasetName,
            LinkedHashMap<String, VariableType> sqlSchema
    ) throws SQLException {
        LinkedHashMap<String,VariableType> variablesToAdd = new LinkedHashMap<>();
        Set<String> columnsAlreadyInDatabase = new HashSet<>(getColumnNames(statement, datasetName));
        //Filter out variable names already in database table
        sqlSchema.keySet().stream()
                .filter(variableName -> !columnsAlreadyInDatabase.contains(variableName))
                .forEach(variableNameToAdd -> variablesToAdd.put(variableNameToAdd,sqlSchema.get(variableNameToAdd)));
        return variablesToAdd;
    }

    private static String getUpdateTableQuery(String datasetName, LinkedHashMap<String, VariableType> variablesToAdd) {
        //ALTER TABLE query building
        StringBuilder alterTableQuery = new StringBuilder();

        for (Map.Entry<String, VariableType> column : variablesToAdd.entrySet()) {
            alterTableQuery.append(String.format("ALTER TABLE \"%s\" ADD COLUMN \"", datasetName)).append(column.getKey()).append("\"").append(" ").append(variablesToAdd.get(column.getKey()).getSqlType());
            alterTableQuery.append("; ");
        }

        //Remove last delimiter and replace by ";"
        alterTableQuery.delete(alterTableQuery.length() - 2, alterTableQuery.length());
        alterTableQuery.append(";");
        return alterTableQuery.toString();
    }

    /**
     * Extract variable types from a dataset
     * @param structure the structure of the dataset
     * @return a (variable name,type) map
     */
    private static LinkedHashMap<String, VariableType> extractSqlSchema(Structured.DataStructure structure) {
        LinkedHashMap<String, VariableType> schema = new LinkedHashMap<>();
        for (Structured.Component component : structure.values()) {
            VariableType type = VariableType.getTypeFromJavaClass(component.getType());
            if (type != null){
                //If column not added yet (ignore case)
                if(!schema.keySet().stream().filter(
                        s -> s.equalsIgnoreCase(component.getName())
                ).toList().contains(component.getName())){
                    schema.put(component.getName(), type);
                }
            } else {
                log.warn("Cannot export variable {} to SQL, unrecognized type", component.getName());
            }
        }
        return schema;
    }

    /**
     * Get all table names from database
     * @param statement database statement
     * @return list of table names
     */
    public static List<String> getTableNames(Statement statement) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery("SHOW TABLES")) {
            while (resultSet.next()) {
                tableNames.add(resultSet.getString("name"));
            }
        }
        return tableNames;
    }

    /**
     * insert data into table associated with dataset
     *
     * @param database DuckDB connection
     * @param dataset   dataset to convert
     * @param sqlSchema schema
     */
    private static void insertDataIntoTable(Statement database, String datasetName, Dataset dataset, LinkedHashMap<String, VariableType> sqlSchema) throws SQLException {
        if (dataset.getDataAsMap().isEmpty()) {
            return;
        }

        DuckDBConnection duckDBConnection = (DuckDBConnection) database.getConnection();
        log.debug("URL de connexion : {}", duckDBConnection.getMetaData().getURL());
        try(var appender = duckDBConnection.createAppender(DuckDBConnection.DEFAULT_SCHEMA,datasetName)){
            for (Map<String, Object> dataRow : dataset.getDataAsMap()) {
                appender.beginRow();
                for (String columnName : sqlSchema.keySet()) {
                    String data = dataRow.get(columnName) == null ? null : dataRow.get(columnName).toString().replace("\n","");
                    appendValueWithType(appender, data, sqlSchema.get(columnName));
                }
                appender.endRow();
            }
        }
        database.execute("CHECKPOINT;"); //Force to write data on disk
    }

    private static void appendValueWithType(DuckDBAppender appender,
                                            String data,
                                            VariableType variableType) throws SQLException {
        if (data == null){
            appender.appendNull();
            return;
        }
        switch (variableType) {
            case NUMBER -> appender.append(Double.parseDouble(data));
            case BOOLEAN -> appender.append(Boolean.parseBoolean(data));
            case INTEGER -> appender.append(Long.parseLong(data));
            case STRING -> appender.append(data);
            case DATE -> appender.append(LocalDateTime.parse(data));
        }
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
    public static Connection openConnection(Path databaseFilePath){
        int trycount = 0;
        while(true) {
            try {
                Files.createDirectories(databaseFilePath.getParent());
                return DriverManager.getConnection(Constants.DUCKDB_URL + "/" + databaseFilePath.toAbsolutePath().subpath(0, databaseFilePath.toAbsolutePath().getNameCount()));
            } catch (SQLException e) {
                try {
                    if (trycount < Constants.DB_CONNECTION_TRY_COUNT) {
                        trycount++;
                        log.warn(e.toString());
                        log.warn("Waiting 2s and retry....");
                        Thread.sleep(2000);
                    }else{
                        log.error("Still failing after {} tries !", Constants.DB_CONNECTION_TRY_COUNT);
                        log.error(e.toString());
                        break;
                    }
                }catch (InterruptedException ie){
                    log.error("InterruptedException");
                    Thread.currentThread().interrupt();
                }
            } catch (IOException e){
                log.error(e.toString());
            }
        }
        return null;
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
        List<String> columnNames = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery(String.format("DESCRIBE \"%s\"", tableName))) {
            while (resultSet.next()) {
                columnNames.add(resultSet.getString("column_name"));
            }
        }
        return columnNames;
    }

    /**
     * Connect to DuckDB and retrieve column names of a table with a specific type
     *
     * @param tableName name of table to retrieve column names
     * @return table columns names
     * @throws SQLException if SQL error
     */
    public static List<String> getColumnNames(Statement statement, String tableName, VariableType variableType) throws SQLException {
        List<String> columnNames = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery(String.format("SELECT * FROM (DESCRIBE \"%s\") WHERE column_type = '%s'", tableName, variableType.getSqlType()))){
            while (resultSet.next()) {
                columnNames.add(resultSet.getString("column_name"));
            }
        }
        return columnNames;
    }

    public static Map<String, String> getColumnTypes(Statement statement, String tableName) throws SQLException {
        Map<String, String> columnTypes = new LinkedHashMap<>(); // conserve l'ordre des colonnes

        try (ResultSet resultSet = statement.executeQuery(String.format("DESCRIBE \"%s\"", tableName))) {
            while (resultSet.next()) {
                String columnName = resultSet.getString("column_name");
                String columnType = resultSet.getString("column_type").toUpperCase(); // ex: BOOLEAN, VARCHAR, INTEGER
                columnTypes.put(columnName, columnType);
            }
        }

        return columnTypes;
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
     * Connect to DuckDB and import CSV file in a table named after the file with a custom delimiter
     * @param statement database connection
     * @param filePath path of the file to import into duckdb
     */
    public static void readCsvFile(Statement statement, Path filePath, String delimiter) throws SQLException {
        statement.execute(String.format("CREATE OR REPLACE TABLE '%s' AS SELECT * FROM read_csv('%s', delim = '%s')",
                filePath.getFileName().toString().split("\\.")[0], filePath, delimiter));
    }

    /**
     * Connect to DuckDB and import PARQUET file in a table named after the file
     * @param statement database connection
     * @param filePath path of the file to import into duckdb
     */
    public static void readParquetFile(Statement statement, Path filePath) throws SQLException {
        statement.execute(String.format("CREATE OR REPLACE TABLE '%s' AS SELECT * FROM read_parquet('%s')",
                filePath.getFileName().toString().split("\\.")[0], filePath));
    }

    public static void deleteDatabaseFile(String databasePath) {
        // Connection should be close before (after try-with-resources)
        File dbFile = new File(databasePath);
        try{
            Files.deleteIfExists(dbFile.toPath());
        } catch (IOException e){
            log.warn("❌ Can't delete DB File ! \n {}", e.toString());
        }
    }
}