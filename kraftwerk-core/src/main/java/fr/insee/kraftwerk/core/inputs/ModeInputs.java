package fr.insee.kraftwerk.core.inputs;

import fr.insee.kraftwerk.core.parsers.DataFormat;
import lombok.Getter;
import lombok.Setter;

/**
 * POJO-like class to store different files associated to a collection mode.
 */
public class ModeInputs {

    @Getter @Setter protected String dataFile;
    @Getter @Setter protected String DDIFile;
    @Getter @Setter protected String LunaticFile;
    @Getter         protected DataFormat dataFormat;
    @Getter @Setter protected String dataMode;
    @Getter @Setter protected String modeVtlFile;
    @Getter @Setter protected String paradataFolder;
    @Getter @Setter protected String reportingDataFile;

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
        dataFormat = dataFormat.toUpperCase();
        switch(dataFormat) {
            case "XFORMS":
                this.dataFormat = DataFormat.XFORMS;
                break;
            case "PAPER":
                this.dataFormat = DataFormat.PAPER;
                break;
            case "LUNATIC_JSON":
                this.dataFormat = DataFormat.LUNATIC_JSON;
                break;
            case "LUNATIC_XML":
                this.dataFormat = DataFormat.LUNATIC_XML;
                break;
            default:
                throw new UnknownDataFormatException(
                        "Unknown data format: " + dataFormat);
        }
    }

}