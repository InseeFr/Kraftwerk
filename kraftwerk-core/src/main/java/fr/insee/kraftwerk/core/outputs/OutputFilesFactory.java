package fr.insee.kraftwerk.core.outputs;

import fr.insee.kraftwerk.core.encryption.EncryptionUtils;
import fr.insee.kraftwerk.core.outputs.csv.CsvOutputFiles;
import fr.insee.kraftwerk.core.outputs.parquet.ParquetOutputFiles;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.sql.Statement;
import java.util.List;

@Component
public class OutputFilesFactory {

    private final EncryptionUtils encryptionUtils;

    @Autowired
    public OutputFilesFactory(EncryptionUtils encryptionUtils) {
        this.encryptionUtils = encryptionUtils;
    }

    public CsvOutputFiles createCsv(Path outDir, VtlBindings bindings, List<String> modes,
                                    Statement db, FileUtilsInterface fs, KraftwerkExecutionContext ctx) {
        return newInstanceOfCsvOutputFiles(outDir, bindings, modes, db, fs, ctx, encryptionUtils);
    }

    /**
     * package-protected method for unit tests spying purpose
     * (as we can't test new instance creation with "new" keyword.)
     */
    CsvOutputFiles newInstanceOfCsvOutputFiles(Path outDir, VtlBindings bindings, List<String> modes, Statement db,
                                               FileUtilsInterface fs, KraftwerkExecutionContext ctx, EncryptionUtils encUtils) {
        return new CsvOutputFiles(outDir, bindings, modes, db, fs, ctx, encUtils);
    }

    public ParquetOutputFiles createParquet(Path outDir, VtlBindings bindings, List<String> modes,
                                            Statement db, FileUtilsInterface fs, KraftwerkExecutionContext ctx) {
        return newInstanceOfParquetOutputFiles(outDir, bindings, modes, db, fs, ctx, encryptionUtils);
    }

    /**
     * package-protected method for unit tests spying purpose
     * (as we can't test new instance creation with "new" keyword.)
     */
    ParquetOutputFiles newInstanceOfParquetOutputFiles(Path outDir, VtlBindings bindings, List<String> modes, Statement db,
                                               FileUtilsInterface fs, KraftwerkExecutionContext ctx, EncryptionUtils encUtils) {
        return new ParquetOutputFiles(outDir, bindings, modes, db, fs, ctx, encUtils);
    }

}
