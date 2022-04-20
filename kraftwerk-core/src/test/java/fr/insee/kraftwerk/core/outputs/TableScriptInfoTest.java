package fr.insee.kraftwerk.core.outputs;

import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.metadata.VariablesMapTest;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawDataTest;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Dataset.Role;
import fr.insee.vtl.model.Structured.DataStructure;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TableScriptInfoTest {
	
	VtlBindings vtlBindings = new VtlBindings();

	DataStructure dataStructure;

	TableScriptInfo tableScriptInfo;

	Map<String, VariablesMap> metadataVariables = new LinkedHashMap<String, VariablesMap>();


	private void instantiateMap() {
		metadataVariables.put("CAWI", VariablesMapTest.createFakeVariablesMap());
		SurveyRawData srdWeb = SurveyRawDataTest.createFakeCawiSurveyRawData();
		srdWeb.setVariablesMap(VariablesMapTest.createFakeVariablesMap());
		vtlBindings.convertToVtlDataset(srdWeb, "CAWI");
		
		metadataVariables.put("PAPI", VariablesMapTest.createAnotherFakeVariablesMap());
		SurveyRawData srdPaper = SurveyRawDataTest.createFakePapiSurveyRawData();
		srdPaper.setVariablesMap(VariablesMapTest.createAnotherFakeVariablesMap());
		vtlBindings.convertToVtlDataset(srdPaper, "PAPI");
		
		dataStructure = vtlBindings.getDataset("CAWI").getDataStructure();
		tableScriptInfo = new TableScriptInfo("MULTIMODE", "TEST", dataStructure, metadataVariables);
		System.out.println(dataStructure.keySet());
		System.out.println(metadataVariables.get("CAWI").getVariableNames());
		System.out.println(metadataVariables.get("CAWI").getFullyQualifiedNames());
	}
	
	@Test
	public void getAllLengthTest() {
		instantiateMap();
		Map<String, Variable> listVariables = tableScriptInfo.getAllLength(dataStructure, metadataVariables);
		assertEquals("50", listVariables.get("LAST_NAME").getLength());
		assertEquals("50", listVariables.get("FIRST_NAME").getLength());
		assertEquals("50", listVariables.get("AGE").getLength());
		assertEquals("500", listVariables.get("CARS_LOOP.CAR_COLOR").getLength());
	}

}
