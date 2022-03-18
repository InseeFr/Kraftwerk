package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.metadata.Group;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.metadata.*;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Dataset.Role;
import fr.insee.vtl.model.InMemoryDataset;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class PaperDataProcessingTest {

	Dataset paperDataset = new InMemoryDataset(
			List.of(
					// Correct answers
					Map.of("ID", "P01",
							"FOO", 1L,
							"GENDER_1", "1", "GENDER_2", "0",
							"HAPPINESS_1", "0", "HAPPINESS_2", "0", "HAPPINESS_3", "1", "HAPPINESS_4", "0"),
					// None of the modalities are ticked
					Map.of("ID", "P02",
							"FOO", 2L,
							"GENDER_1", "0", "GENDER_2", "0",
							"HAPPINESS_1", "0", "HAPPINESS_2", "0", "HAPPINESS_3", "0", "HAPPINESS_4", "0"),
					// Several modalities are ticked
					Map.of("ID", "P03",
							"FOO", 3L,
							"GENDER_1", "1", "GENDER_2", "1",
							"HAPPINESS_1", "1", "HAPPINESS_2", "0", "HAPPINESS_3", "0", "HAPPINESS_4", "1")
					),
			Map.of("ID", String.class,
					"FOO", Double.class,
					"GENDER_1", String.class, "GENDER_2", String.class,
					"HAPPINESS_1", String.class, "HAPPINESS_2", String.class, "HAPPINESS_3", String.class, "HAPPINESS_4", String.class
			),
			Map.of("ID", Dataset.Role.IDENTIFIER,
					"FOO", Role.MEASURE,
					"GENDER_1", Role.MEASURE, "GENDER_2", Role.MEASURE,
					"HAPPINESS_1", Role.MEASURE, "HAPPINESS_2", Role.MEASURE, "HAPPINESS_3", Role.MEASURE, "HAPPINESS_4", Role.MEASURE
			)
	);

	private void addPaperUcq(VariablesMap variablesMap, Group group, String variableName, int modalitiesNumber) {
		Variable ucq = new Variable(variableName, group, VariableType.STRING);
		variablesMap.putVariable(ucq);
		for (int k=1; k < modalitiesNumber+1; k++) {
			Variable ucqModality = new Variable(String.format("%s_%s", variableName, k), group, VariableType.STRING);
			//ucqModality.setMcqName(ucq.getName());
			variablesMap.putVariable(ucqModality);
		}
	}

	@Test
	public void testPaperDataProcessing() {
		//
		VariablesMap variablesMap = new VariablesMap();
		Group rootGroup = variablesMap.getRootGroup();
		variablesMap.putVariable(new Variable("FOO", rootGroup, VariableType.NUMBER));
		//addPaperUcq(variablesMap, rootGroup, "GENDER", 2);
		//addPaperUcq(variablesMap, rootGroup, "HAPPINESS", 4);
		//
		VtlBindings vtlBindings = new VtlBindings();
		vtlBindings.getBindings().put("TEST", paperDataset);
		//
		PaperDataProcessing paperDataProcessing = new PaperDataProcessing(vtlBindings);
		paperDataProcessing.applyAutomatedVtlInstructions("TEST", variablesMap);
		//
		Dataset paperDsModified = vtlBindings.getDataset("TEST");

		//
		assertNotNull(paperDsModified);
		//
		assertEquals(1, 1);
		// TODO: UPDATE THIS TEST CLASS
	}
}
