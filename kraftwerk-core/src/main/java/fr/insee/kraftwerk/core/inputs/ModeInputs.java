package fr.insee.kraftwerk.core.inputs;

import java.net.URL;
import java.nio.file.Path;

import fr.insee.kraftwerk.core.parsers.DataFormat;
import lombok.Getter;
import lombok.Setter;

/**
 * POJO-like class to store different files associated to a collection mode.
 */
@Getter
@Setter
public class ModeInputs {

	protected Path dataFile;
	protected URL ddiUrl;
	protected Path lunaticFile;
	protected DataFormat dataFormat;
	protected String dataMode;
	protected Path modeVtlFile;
	protected Path paradataFolder;
	protected Path reportingDataFile;

	/**
	 * Allow to specify the data format using a string argument.
	 *
	 * @param dataFormat Data format name.
	 *
	 * @throws UnknownDataFormatException An exception is raised if the name given
	 *                                    in unknown.
	 */
	public void setDataFormat(String dataFormatString) throws UnknownDataFormatException {
		try {
			dataFormat = DataFormat.valueOf(dataFormatString.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new UnknownDataFormatException("Unknown data format: " + dataFormat);
		}
	}

}
