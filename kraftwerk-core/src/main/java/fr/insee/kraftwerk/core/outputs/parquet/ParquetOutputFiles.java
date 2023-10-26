package fr.insee.kraftwerk.core.outputs.parquet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.apache.avro.generic.GenericData;
import org.apache.parquet.Preconditions;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.io.OutputFile;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.outputs.OutputFiles;
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
	public ParquetOutputFiles(Path outDirectory, VtlBindings vtlBindings, List<String> modes,
			String multimodeDatasetNames) {
		super(outDirectory, vtlBindings, modes, multimodeDatasetNames);
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
			OutputFile parquetOutFile = new NioPathOutputFile(fileToWrite);
			
		    Preconditions.checkArgument(dataset != null && dataset.size() ==   getVtlBindings().getDataset(datasetName).getDataPoints().size(), "Invalid schemas");
			try {
				ParquetWriter<GenericData.Record> writerTest = AvroParquetWriter
				        .<GenericData.Record>builder(parquetOutFile)
				        .withSchema(schema)
				   //     .withDataModel(dataset)
				  //      .withConf(new Configuration())
				        .withCompressionCodec(CompressionCodecName.SNAPPY)
				        .build();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		        try (ParquetWriter<GenericData.Record> writer = AvroParquetWriter
		                .<GenericData.Record>builder(parquetOutFile)
		                .withSchema(schema)
		                .withDataModel(GenericData.get())
		           //     .withDataModel(dataset)
		          //      .withConf(new Configuration())
		                .withCompressionCodec(CompressionCodecName.SNAPPY)
		                .build()) {
		            for (GenericData.Record recordData : dataset) {
		                writer.write(recordData);
		            }
		        } catch (IOException e) {
		        	log.error("IOException - Can't write parquet output tables :  {}", e.getMessage());
		        	throw new KraftwerkException(500, e.getMessage());
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
	        builder.name(component.getName()).type().nullable().stringType().stringDefault("");//.stringType().noDefault();

//        	
//    		Class<?> type = component.getType();
//    		if (String.class.equals(type)) {
//    	        builder.name(component.getName()).type().nullable().stringType().noDefault();
//
//    	        } else if (Long.class.equals(type)) {
//        	       // builder.name(component.getRole().name()).type().unionOf().nullBuilder().endNull().and().longType().endUnion().noDefault();
//        	        builder.name(component.getName());//.type().nullable().longType().noDefault();
//    	        } else if (Double.class.equals(type)) {
//        	        builder.name(component.getName());//.type().nullable().doubleType().noDefault();
//    	        } else if (Boolean.class.equals(type)) {
//        	        builder.name(component.getName());//.type().nullable().booleanType().noDefault();
//    	        } else if (Instant.class.equals(type)) {
//        	        builder.name(component.getName());//.type().unionOf().nullBuilder().endNull().and().stringType().and().type(LogicalTypes.timestampMillis().addToSchema(SchemaBuilder.builder().intType())).endUnion().noDefault();
//    	        } else if (LocalDate.class.equals(type)) {
//        	        builder.name(component.getName());//.type().unionOf().nullBuilder().endNull().and().stringType().and().type(LogicalTypes.date().addToSchema(SchemaBuilder.builder().intType())).endUnion().noDefault();
//    	        } else {
//    	            throw new UnsupportedOperationException("unsupported type " + type);
//    	        }	
    		
        }    	
        return builder.endRecord();

    }



	@Override
	public void writeImportScripts(Map<String, VariablesMap> metadataVariables, List<KraftwerkError> errors) {
		// TODO
	}

	/**
	 * Return the name of the file to be written from the dataset name.
	 */
	@Override
	public String outputFileName(String datasetName) {
		return getOutputFolder().getFileName() + "_" + datasetName + ".parquet";
	}

}
