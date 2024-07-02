package fr.insee.kraftwerk.core.inputs;

import fr.insee.kraftwerk.core.exceptions.UnknownDataFormatException;
import fr.insee.kraftwerk.core.parsers.DataFormat;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

/**
 * POJO-like class to store different files associated to a collection mode.
 */
@Getter
@Setter
public class ModeInputs {

    protected Path dataFile;
    protected String ddiUrl;
    protected Path lunaticFile;
    protected DataFormat dataFormat;
    protected String dataMode;
    protected Path modeVtlFile;
    protected Path paradataFolder;
    protected Path reportingDataFile;

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

}
