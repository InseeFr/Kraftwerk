package fr.insee.kraftwerk.core.inputs;

import fr.insee.bpm.metadata.model.CalculatedVariables;
import fr.insee.bpm.metadata.reader.lunatic.LunaticReader;
import fr.insee.kraftwerk.core.exceptions.NullLunaticFileException;
import fr.insee.kraftwerk.core.exceptions.UnknownDataFormatException;
import fr.insee.kraftwerk.core.parsers.DataFormat;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

/**
 * POJO-like class to store different files associated to a collection mode.
 */
@Getter
@Setter
@Slf4j
public class ModeInputs {

    private FileUtilsInterface fileUtilsInterface;

    public ModeInputs(FileUtilsInterface fileUtilsInterface) {
        this.fileUtilsInterface = fileUtilsInterface;
    }

    protected Path dataFile;
    protected String ddiUrl;
    protected Path lunaticFile;
    protected DataFormat dataFormat;
    protected String dataMode;
    protected Path modeVtlFile;
    protected Path paradataFolder;
    protected Path reportingDataFile;

    /**
     * Calculated variables cache for mode inputs
     */
    private CalculatedVariables calculatedVariablesCache;

    /**
     * Allow to specify the data format using a string argument.
     *
     * @param dataFormat
     * Data format name.
     *
     * @throws UnknownDataFormatException
     * An exception is raised if the name given in unknown.
     */
    public void setDataFormat(String dataFormat) throws UnknownDataFormatException {
    	try {
            this.dataFormat = DataFormat.valueOf(dataFormat.toUpperCase());
    	}catch( IllegalArgumentException e) {
    		throw new UnknownDataFormatException(dataFormat);
    	}
    }

    /**
     * Use the cache to get calculated variables, read from file system if null
     *
     * @return CalculatedVariables from BPM's getCalculatedFromLunatic
     * @throws NullLunaticFileException if this method is called when lunaticFile is null
     */
    public CalculatedVariables getCalculatedVariables() throws NullLunaticFileException {
        if(lunaticFile == null){
            throw new NullLunaticFileException(
                    "Call on getCalculatedVariables with null lunaticFile for mode %s".formatted(dataMode)
            );
        }
        if(calculatedVariablesCache == null){
            log.info("Parsing calculated variables from {}", lunaticFile);
            calculatedVariablesCache = LunaticReader.getCalculatedFromLunatic(
                    fileUtilsInterface.readFile(lunaticFile.toString())
            );
        }
        return calculatedVariablesCache;
    }
}
