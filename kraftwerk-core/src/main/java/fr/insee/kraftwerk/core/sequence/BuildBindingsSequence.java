package fr.insee.kraftwerk.core.sequence;

import java.nio.file.Path;
import java.util.Map;

import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.extradata.paradata.Paradata;
import fr.insee.kraftwerk.core.extradata.paradata.ParadataParser;
import fr.insee.kraftwerk.core.extradata.reportingdata.CSVReportingDataParser;
import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingData;
import fr.insee.kraftwerk.core.extradata.reportingdata.XMLReportingDataParser;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.metadata.MetadataUtils;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.parsers.DataParser;
import fr.insee.kraftwerk.core.parsers.DataParserManager;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlExecute;

public class BuildBindingsSequence {

	VtlExecute vtlExecute;

	public BuildBindingsSequence() {
		vtlExecute = new VtlExecute();
	}

	public void buildVtlBindings(UserInputs userInputs, String dataMode, VtlBindings vtlBindings, Map<String, VariablesMap> metadataVariables) throws NullException {
		ModeInputs modeInputs = userInputs.getModeInputs(dataMode);
		SurveyRawData data = new SurveyRawData();

		/* Step 2.0 : Read the DDI file (and Lunatic Json for missing variables) to get survey variables */
		data.setVariablesMap(metadataVariables.get(dataMode));

		/* Step 2.1 : Fill the data object with the survey answers file */
		data.setDataFilePath(modeInputs.getDataFile());
		DataParser parser = DataParserManager.getParser(modeInputs.getDataFormat(), data);
		parser.parseSurveyData(modeInputs.getDataFile());


		/* Step 2.2 : Get paradata for the survey */
		parseParadata(modeInputs, data);

		/* Step 2.3 : Get reportingData for the survey */
		parseReportingData(modeInputs, data);

		/* Step 2.4a : Convert data object to a VTL Dataset */
		data.setDataMode(dataMode);
		vtlExecute.convertToVtlDataset(data, dataMode, vtlBindings);
	}
	

	private void parseParadata(ModeInputs modeInputs, SurveyRawData data) throws NullException {
		Path paraDataFolder = modeInputs.getParadataFolder();
		if (paraDataFolder != null) {
			ParadataParser paraDataParser = new ParadataParser();
			Paradata paraData = new Paradata(paraDataFolder);
			paraDataParser.parseParadata(paraData, data);
		}
	}

	private void parseReportingData(ModeInputs modeInputs, SurveyRawData data) throws NullException {
		Path reportingDataFile = modeInputs.getReportingDataFile();
		if (reportingDataFile != null) {
			ReportingData reportingData = new ReportingData(reportingDataFile);
			if (reportingDataFile.toString().contains(".xml")) {
				XMLReportingDataParser xMLReportingDataParser = new XMLReportingDataParser();
				xMLReportingDataParser.parseReportingData(reportingData, data);

			} else if (reportingDataFile.toString().contains(".csv")) {
					CSVReportingDataParser cSVReportingDataParser = new CSVReportingDataParser();
					cSVReportingDataParser.parseReportingData(reportingData, data);
			}
		}
	}


	
}
