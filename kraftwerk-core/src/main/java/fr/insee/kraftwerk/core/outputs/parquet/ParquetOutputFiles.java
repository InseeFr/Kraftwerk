package fr.insee.kraftwerk.core.outputs.parquet;

import static org.apache.spark.sql.types.DataTypes.BooleanType;
import static org.apache.spark.sql.types.DataTypes.DateType;
import static org.apache.spark.sql.types.DataTypes.DoubleType;
import static org.apache.spark.sql.types.DataTypes.LongType;
import static org.apache.spark.sql.types.DataTypes.StringType;
import static org.apache.spark.sql.types.DataTypes.TimestampType;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.MetadataBuilder;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.outputs.OutputFiles;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Structured.Component;
import fr.insee.vtl.model.Structured.DataStructure;

/**
 * Class to manage the writing of Parquet output tables.
 */
public class ParquetOutputFiles extends OutputFiles {


	/**
	 * When an instance is created, the output folder is created.
	 * 
	 * @param outDirectory Out directory defined in application properties.
	 * @param vtlBindings  Vtl bindings where datasets are stored.
	 * @param userInputs   Used to get the campaign name and to filter intermediate
	 *                     datasets that we don't want to output.
	 */
	public ParquetOutputFiles(Path outDirectory, VtlBindings vtlBindings, List<String> modes, String multimodeDatasetNames) {
		super(outDirectory, vtlBindings, modes, multimodeDatasetNames);
	}


	/**
	 * Method to write output tables from datasets that are in the bindings.
	 */
	@Override
	public void writeOutputTables(Map<String,VariablesMap> metadataVariables) {
		/* Creating Spark Session */
		SparkSession sparkSession = SparkSession.builder().config("spark.ui.enabled",false).appName("SparkWriteParquetFile").master("local").getOrCreate();
		
		for (String datasetName : getDatasetToCreate()) {
		
			/* Creating dataset using list of row of spark.sql */
	        List<Row> rows = getVtlBindings().getDataset(datasetName).getDataPoints().stream().map(points -> RowFactory.create(points.toArray(new Object[]{}))).collect(Collectors.toList());

	        StructType schema = toSparkSchema(getVtlBindings().getDataset(datasetName).getDataStructure());

	        Dataset<Row> sparkDataset = sparkSession.createDataFrame(rows, schema);
			
			/* Write parquet file using write & savemode method */ //Also we can use save method to write parquet file
			sparkDataset //.coalesce(1)
				.write()
				.mode(SaveMode.Append)
				.option("header", true)
				.parquet(outputFileName(datasetName));
			
		}

	}
	

	
    /**
     * Transforms a {@link DataStructure} into a Spark schema.
     *
     * @param structure the dataset structure to transform
     * @return The resulting Spark schema (<code>StructType</code> object).
     */
    public static StructType toSparkSchema(DataStructure structure) {
        List<StructField> schema = new ArrayList<>();
        for (Component component : structure.values()) {
        	MetadataBuilder metadataBuilder = new MetadataBuilder();
        	metadataBuilder.putString("vtlRole", component.getRole().name());
        	
        	schema.add(
        			DataTypes.createStructField(
        					component.getName(),
        					fromVtlType(component.getType()),
        					true,
        					metadataBuilder.build()
            ));
        }
        return DataTypes.createStructType(schema);
    }
	
    /**
     * Translates a VTL data type into a Spark data type.
     *
     * @param type the VTL data type to translate (as a class).
     * @return The corresponding Spark {@link DataType}.
     */
    public static DataType fromVtlType(Class<?> type) {
        if (String.class.equals(type)) {
            return StringType;
        } else if (Long.class.equals(type)) {
            return LongType;
        } else if (Double.class.equals(type)) {
            return DoubleType;
        } else if (Boolean.class.equals(type)) {
            return BooleanType;
        } else if (Instant.class.equals(type)) {
            return TimestampType;
        } else if (LocalDate.class.equals(type)) {
            return DateType;
        } else {
            throw new UnsupportedOperationException("unsupported type " + type);
        }
    }

    

	@Override
	public void writeImportScripts(Map<String, VariablesMap> metadataVariables, List<KraftwerkError> errors) {
		//TODO
	}

	/**
	 * Return the name of the file to be written from the dataset name.
	 */
	@Override
	public String outputFileName(String datasetName) {
		return getOutputFolder().getFileName() + "_" + datasetName + ".parquet";
	}

}
