package fr.insee.kraftwerk.core.utils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.parquet.column.Encoding;
import org.apache.parquet.column.EncodingStats;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.BlockMetaData;
import org.apache.parquet.hadoop.metadata.ColumnChunkMetaData;
import org.apache.parquet.io.InputFile;

import fr.insee.kraftwerk.core.outputs.parquet.LocalInputFile;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParquetUtils {

	private ParquetUtils(){
		throw new IllegalStateException("Utility class");
	}

	public static void describeParquetFile(Path path) throws IOException {
		ParquetFileReader reader = createParquetFileReader(path);
		writeFileLevelMetadata(reader);
		writeFileSpecificMetadata(reader);
		writeFileSchema(reader);
		writeRowGroups(reader);
		ecrireColumns(reader);
		reader.close();
	}

	private static void writeFileLevelMetadata(ParquetFileReader reader)  {
		log.debug("1. FILE LEVEL METADATA");
		log.debug("- Created by : {}", reader.getFileMetaData().getCreatedBy());
		log.debug("- Nb lines : {}", reader.getRecordCount());
		log.debug("- Nb rowgroups : " + reader.getRowGroups().size());
		writeCodecsCompression(reader);
	}

	private static void writeCodecsCompression(ParquetFileReader reader) {
		Set<String> codecs = new HashSet<>();
		for (BlockMetaData rowGroup : reader.getRowGroups()) {
			for (ColumnChunkMetaData colonne : rowGroup.getColumns()) {
				codecs.add(colonne.getCodec().toString());
			}
		}
		log.debug("- compression codecs (columnChunks level) : " + codecs.toString());
	}

	private static void writeFileSpecificMetadata(ParquetFileReader reader) {
		log.debug("2. FILE-LEVEL SPECIFIC METADATA");
		Map<String, String> keys = reader.getFileMetaData().getKeyValueMetaData();
		for (Entry<String, String> entree : keys.entrySet()) {
			log.debug(" - key : {} -> value : {}", entree.getKey().replace("\\n", ""),
					entree.getValue().replace("\\n", ""));
		}
	}

	private static void writeFileSchema(ParquetFileReader reader)  {
		log.debug("3. PARQUET FILE SCHEMA");
		reader.getFileMetaData().getSchema().getFields().stream().forEach(type -> log.debug("- {}", type));
	}

	private static void writeRowGroups(ParquetFileReader reader)  {
		log.debug("4. ROWGROUP INFORMATION (TABLE)");
		log.debug("idRowGroup;nbLignes;indexOffset;compressedSize;totalByteSize");
		for (BlockMetaData rowgroup : reader.getRowGroups()) {
			log.debug("{};{};{};{};{} \n", String.valueOf(rowgroup.getOrdinal()),
					String.valueOf(rowgroup.getRowCount()), String.valueOf(rowgroup.getRowIndexOffset()),
					String.valueOf(rowgroup.getCompressedSize()), String.valueOf(rowgroup.getTotalByteSize()));
		}
	}

	private static void ecrireColumns(ParquetFileReader reader)  {
		log.debug("5. METADATA AT COLUMN LEVEL");
		Map<String, Set<String>> metadonnees = new HashMap<>();
		int idRowGroup = 0;
		for (BlockMetaData rowgroup : reader.getRowGroups()) {
			metadonnees = extractColumnMetadataFromRowGroup(metadonnees, rowgroup, idRowGroup);
			idRowGroup++;
		}
		for (Entry<String, Set<String>> variable : metadonnees.entrySet()) {
			ecrireMetadonneesColonne(variable);
		}
	}

	private static Map<String, Set<String>> extractColumnMetadataFromRowGroup(Map<String, Set<String>> metadata,
			BlockMetaData rowgroup, int idRowGroup) {
		for (ColumnChunkMetaData column : rowgroup.getColumns()) {
			String key = column.getPath().toString();
			Set<String> rowGroups = metadata.get(key);
			if (rowGroups == null) {
				rowGroups = new LinkedHashSet<>();
			}
			StringBuilder sb = new StringBuilder();
			sb.append(idRowGroup);
			sb.append(";");
			sb.append(column.getTotalSize());
			sb.append(";");
			sb.append(column.getTotalUncompressedSize());
			sb.append(";");
			sb.append(column.getEncodings());
			sb.append(";");
			EncodingStats stats = column.getEncodingStats();
			if (stats != null) {
				Set<String> dicos = new LinkedHashSet<>();
				for (Encoding encoding : stats.getDictionaryEncodings()) {
					dicos.add(encoding.name() + ":" + stats.getNumDictionaryPagesEncodedAs(encoding));
				}
				sb.append(dicos.toString());
			}
			sb.append(";");
			if (stats != null) {
				Set<String> datas = new LinkedHashSet<>();
				for (Encoding encoding : stats.getDataEncodings()) {
					datas.add(encoding.name() + ":" + stats.getNumDataPagesEncodedAs(encoding));
				}
				sb.append(datas.toString());
			}
			sb.append(";");
			sb.append(column.getStatistics());
			sb.append(";");
			if (stats != null) {
				sb.append(stats.usesV2Pages());
			}
			rowGroups.add(sb.toString());
			metadata.put(key, rowGroups);
		}
		return metadata;
	}

	private static void ecrireMetadonneesColonne(Entry<String, Set<String>> variable) {
		log.debug("Metadata for column {} : ", variable.getKey());
		log.debug(
				"rowgroup;totalSize;totalUncompressedSize;encodings;nbDictionaryPagesEncodedAs;nbDataPagesEncodedAs;statistics;pagesV2");
		Set<String> metadonneesColonne = variable.getValue();
		for (String metadonneeColonne : metadonneesColonne) {
			log.debug(metadonneeColonne);
		}
	}

	private static ParquetFileReader createParquetFileReader(Path path) throws IOException {
		// On crée un ParquetFileReader pour pouvoir lire un fichier parquet
		InputFile fichier = new LocalInputFile(path);
		return ParquetFileReader.open(fichier);
	}

}
