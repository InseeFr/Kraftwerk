package fr.insee.kraftwerk.core.inputs;

import fr.insee.kraftwerk.core.exceptions.UnknownDataFormatException;
import fr.insee.kraftwerk.core.parsers.DataFormat;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.nio.file.Path;

/**
 * POJO-like class to store different files associated to a collection mode.
 */
@Getter
public class ModeInputs {

    @Setter protected Path dataFile;
    @Setter protected URL ddiUrl;
    @Setter protected Path lunaticFile;
    protected DataFormat dataFormat;
    @Setter protected String dataMode;
    @Setter protected Path modeVtlFile;
    @Setter protected Path paradataFolder;
    @Setter protected Path reportingDataFile;

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
