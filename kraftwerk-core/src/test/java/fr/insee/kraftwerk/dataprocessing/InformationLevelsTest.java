package fr.insee.kraftwerk.dataprocessing;

import fr.insee.kraftwerk.Constants;
import fr.insee.kraftwerk.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Dataset.Role;
import fr.insee.vtl.model.InMemoryDataset;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InformationLevelsTest {
	
	private final String rootId = Constants.ROOT_IDENTIFIER_NAME;

	Dataset testDataset = new InMemoryDataset(
			List.of(
					Map.of(rootId, "T01", "LOOP", "LOOP-01", "FOO", "foo1", "LOOP.FOO1", "foo11"),
					Map.of(rootId, "T01", "LOOP", "LOOP-02", "FOO", "foo1", "LOOP.FOO1", "foo12"),
					Map.of(rootId, "T02", "LOOP", "LOOP-01", "FOO", "foo2", "LOOP.FOO1", "foo21")
			),
			Map.of(rootId, String.class, "LOOP", String.class,
					"FOO", String.class, "LOOP.FOO1", String.class),
			Map.of(rootId, Role.IDENTIFIER, "LOOP", Role.IDENTIFIER,
					"FOO", Role.MEASURE, "LOOP.FOO1", Role.MEASURE)
	);

	@Test
	public void applyInformationLevelsProcessing() {
		//
		VtlBindings vtlBindings = new VtlBindings();
		vtlBindings.getBindings().put("MULTIMODE", testDataset);
		//
		InformationLevelsProcessing processing = new InformationLevelsProcessing(vtlBindings);
		processing.applyAutomatedVtlInstructions("MULTIMODE");
		//
		Dataset rootDataset = vtlBindings.getDataset(Constants.ROOT_GROUP_NAME);
		Dataset loopDataset = vtlBindings.getDataset("LOOP");

		//
		assertNotNull(rootDataset);
		assertNotNull(loopDataset);
		//
		assertEquals(Set.of(rootId, "FOO"),
				rootDataset.getDataStructure().keySet());
		assertEquals(Set.of(rootId, "LOOP", "FOO1"),
				loopDataset.getDataStructure().keySet());
		//
		assertEquals(2, rootDataset.getDataPoints().size());
		assertEquals("T01", rootDataset.getDataPoints().get(0).get(rootId));
		assertEquals("foo1", rootDataset.getDataPoints().get(0).get("FOO"));
		assertEquals("T02", rootDataset.getDataPoints().get(1).get(rootId));
		assertEquals("foo2", rootDataset.getDataPoints().get(1).get("FOO"));
		//
		assertEquals(3, loopDataset.getDataPoints().size());
		assertEquals("T01", loopDataset.getDataPoints().get(0).get(rootId));
		assertEquals("LOOP-01", loopDataset.getDataPoints().get(0).get("LOOP"));
		assertEquals("foo11", loopDataset.getDataPoints().get(0).get("FOO1"));
		assertEquals("T01", loopDataset.getDataPoints().get(1).get(rootId));
		assertEquals("LOOP-02", loopDataset.getDataPoints().get(1).get("LOOP"));
		assertEquals("foo12", loopDataset.getDataPoints().get(1).get("FOO1"));
		assertEquals("T02", loopDataset.getDataPoints().get(2).get(rootId));
		assertEquals("LOOP-01", loopDataset.getDataPoints().get(2).get("LOOP"));
		assertEquals("foo21", loopDataset.getDataPoints().get(2).get("FOO1"));
	}

}
