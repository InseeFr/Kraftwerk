package fr.insee.kraftwerk.core.outputs.parquet;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.apache.avro.data.TimeConversions;
import org.apache.avro.generic.GenericData;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.Preconditions;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.io.OutputFile;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.outputs.OutputFiles;
import fr.insee.kraftwerk.core.outputs.TableScriptInfo;
import fr.insee.kraftwerk.core.utils.ParquetUtils;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Structured.Component;
import fr.insee.vtl.model.Structured.DataPoint;
import fr.insee.vtl.model.Structured.DataStructure;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to manage the writing of Parquet output tables.
 */
@Slf4j
public class ParquetOutputFiles extends OutputFiles {

	/**
	 * When an instance is created, the output folder is created.
	 * 
	 * @param outDirectory Out directory defined in application properties.
	 * @param vtlBindings  Vtl bindings where datasets are stored.
	 * @param userInputs   Used to get the campaign name and to filter intermediate
	 *                     datasets that we don't want to output.
	 */
	public ParquetOutputFiles(Path outDirectory, VtlBindings vtlBindings, List<String> modes) {
		super(outDirectory, vtlBindings, modes);
	}

	
	/**
	 * Method to write output tables from datasets that are in the bindings.
	 * @throws KraftwerkException 
	 */
	@Override
	public void writeOutputTables(Map<String,VariablesMap> metadataVariables) throws KraftwerkException {

		for (String datasetName : getDatasetToCreate()) {
			/* Building metadata */
			Schema schema =  extractSchema(getVtlBindings().getDataset(datasetName).getDataStructure());

			/* Creating dataset using Avro GenericData */
			 List<GenericData.Record> dataset = 
	        getVtlBindings().getDataset(datasetName).getDataPoints().stream()
	        	.map(point -> extractGenericData(schema, point)).toList();

			Path fileToWrite = Path.of(getOutputFolder().toString(),outputFileName(datasetName));
			OutputFile parquetOutFile = new LocalOutputFile(fileToWrite);

			
	        GenericData genericData = GenericData.get();
	        genericData.addLogicalTypeConversion(new TimeConversions.DateConversion());
			
		    Preconditions.checkArgument(dataset != null && dataset.size() ==   getVtlBindings().getDataset(datasetName).getDataPoints().size(), "Invalid schemas");
		
		    // need to add logicalTime Support
		    GenericData timeSupport = new GenericData();
		    timeSupport.addLogicalTypeConversion(new TimeConversions.DateConversion());
		    timeSupport.addLogicalTypeConversion(new TimeConversions.TimeMillisConversion());
		    timeSupport.addLogicalTypeConversion(new TimeConversions.TimestampMillisConversion());
		    timeSupport.addLogicalTypeConversion(new TimeConversions.LocalTimestampMillisConversion());
		    
		        try (ParquetWriter<GenericData.Record> writer = AvroParquetWriter
		                .<GenericData.Record>builder(parquetOutFile)
		                .withSchema(schema)
		                .withDataModel(genericData)
		                .withDataModel(timeSupport)
		                .withConf(new Configuration())
		                .withCompressionCodec(CompressionCodecName.SNAPPY)
		                .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
		                .build()) {
		            for (GenericData.Record recordData : dataset) {
		                writer.write(recordData);
		            }
		            log.info("Parquet datasize for {} is : {} for {} records ",datasetName,writer.getDataSize(), dataset == null ? 0 : dataset.size());
		        } catch (IOException e) {
		        	log.error("IOException - Can't write parquet output tables :  {}", e.getMessage());
		        	throw new KraftwerkException(500, e.getMessage());
				}
		        
				try {
					ParquetUtils.describeParquetFile(fileToWrite);
				} catch (IOException e) {
					log.debug("Can't describe parquet file {}", e.getMessage());
				}
			
		}


	}
	
	private static GenericData.Record extractGenericData(Schema schema,  DataPoint value){
		GenericData.Record data =  new GenericData.Record(schema);
		for (Schema.Field key : schema.getFields()) {
			String varName = key.name();
			data.put(varName, value.get(varName));
		}
		return data;
	}

	/**
     * Transforms a {@link DataStructure} into a Spark schema.
     *
     * @param structure the dataset structure to transform
     * @return The resulting Spark schema (<code>StructType</code> object).
     */
    private static Schema extractSchema(DataStructure structure) {
    	
    	FieldAssembler<Schema> builder = SchemaBuilder.record("survey").namespace("any.data").fields();

        for (Component component : structure.values()) {
        	
    		Class<?> type = component.getType();
    		if (String.class.equals(type)) {
    	        builder.name(component.getName()).type().nullable().stringType().noDefault();

    	        } else if (Long.class.equals(type)) {
        	        builder.name(component.getName()).type().nullable().longType().noDefault();
    	        } else if (Double.class.equals(type)) {
        	        builder.name(component.getName()).type().nullable().doubleType().noDefault();
    	        } else if (Boolean.class.equals(type)) {
        	        builder.name(component.getName()).type().nullable().booleanType().noDefault();
    	        } else if (Instant.class.equals(type)) {
        	        builder.name(component.getName()).type().unionOf().nullBuilder().endNull().and().stringType().and().type(LogicalTypes.timestampMillis().addToSchema(SchemaBuilder.builder().longType())).endUnion().noDefault();
    	        } else if (LocalDate.class.equals(type)) {
        	        builder.name(component.getName()).type().unionOf().nullBuilder().endNull().and().stringType().and().type(LogicalTypes.date().addToSchema(SchemaBuilder.builder().intType())).endUnion().noDefault();
    	        } else {
    	            throw new UnsupportedOperationException("unsupported type " + type);
    	        }	
    		
        }    	
        return builder.endRecord();

    }



	@Override
	public void writeImportScripts(Map<String, VariablesMap> metadataVariables, List<KraftwerkError> errors) {
		// Assemble required info to write scripts
		List<TableScriptInfo> tableScriptInfoList = new ArrayList<>();
		for (String datasetName : getDatasetToCreate()) {
			TableScriptInfo tableScriptInfo = new TableScriptInfo(datasetName, outputFileName(datasetName),
					getVtlBindings().getDataset(datasetName).getDataStructure(), metadataVariables);
			tableScriptInfoList.add(tableScriptInfo);
		}
		// Write scripts
		TextFileWriter.writeFile(getOutputFolder().resolve("import_parquet.R"),
				new RImportScript(tableScriptInfoList).generateScript());
	}

	/**
	 * Return the name of the file to be written from the dataset name.
	 */
	@Override
	public String outputFileName(String datasetName) {
		return getOutputFolder().getFileName() + "_" + datasetName + ".parquet";
	}

}
