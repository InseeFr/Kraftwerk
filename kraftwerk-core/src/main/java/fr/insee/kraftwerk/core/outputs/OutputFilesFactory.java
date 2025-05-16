package fr.insee.kraftwerk.core.outputs;

import fr.insee.kraftwerk.core.encryption.EncryptionUtils;
import fr.insee.kraftwerk.core.outputs.csv.CsvOutputFiles;
import fr.insee.kraftwerk.core.outputs.parquet.ParquetOutputFiles;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.sql.Statement;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OutputFilesFactory {

    private final EncryptionUtils encryptionUtils;

    public CsvOutputFiles createCsv(Path outDir, VtlBindings bindings, List<String> modes,
                                    Statement db, FileUtilsInterface fs, KraftwerkExecutionContext ctx) {
        return new CsvOutputFiles(outDir, bindings, modes, db, fs, ctx, encryptionUtils);
    }

    public ParquetOutputFiles createParquet(Path outDir, VtlBindings bindings, List<String> modes,
                                            Statement db, FileUtilsInterface fs, KraftwerkExecutionContext ctx) {
        return new ParquetOutputFiles(outDir, bindings, modes, db, fs, ctx, encryptionUtils);
    }
}
