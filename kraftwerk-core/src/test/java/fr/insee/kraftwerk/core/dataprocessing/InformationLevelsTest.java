package fr.insee.kraftwerk.core.dataprocessing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Dataset.Role;
import fr.insee.vtl.model.InMemoryDataset;

@Slf4j
class InformationLevelsTest {
	private final FileUtilsInterface fileUtilsInterface = new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY);
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
	
	Dataset testDatasetWithPartialIdNull = new InMemoryDataset(
			List.of(
					Map.of(rootId, "T01", "LOOP", "LOOP-01", "FOO", "foo1", "LOOP.FOO1", "foo11"),
					Map.of(rootId, "T01", "LOOP", "LOOP-02", "FOO", "foo1", "LOOP.FOO1", "foo12"),
					Map.of(rootId, "T02", "FOO", "foo2", "LOOP.FOO1", "foo21")
			),
			Map.of(rootId, String.class, "LOOP", String.class,
					"FOO", String.class, "LOOP.FOO1", String.class),
			Map.of(rootId, Role.IDENTIFIER, "LOOP", Role.IDENTIFIER,
					"FOO", Role.MEASURE, "LOOP.FOO1", Role.MEASURE)
	);

	@Test
	void applyInformationLevelsProcessing() {
		KraftwerkExecutionContext kraftwerkExecutionContext = TestConstants.getKraftwerkExecutionContext();
		//
		VtlBindings vtlBindings = new VtlBindings();
		vtlBindings.put("MULTIMODE", testDataset);
		//
		InformationLevelsProcessing processing = new InformationLevelsProcessing(
				vtlBindings,
				fileUtilsInterface,
				kraftwerkExecutionContext
		);
		processing.applyAutomatedVtlInstructions("MULTIMODE", kraftwerkExecutionContext);
		//
		Dataset rootDataset = vtlBindings.getDataset(Constants.ROOT_GROUP_NAME);
		Dataset loopDataset = vtlBindings.getDataset("LOOP");

		//
		assertNotNull(rootDataset);
		assertNotNull(loopDataset);
		// We have to keep the LOOP identifier since VTL 2
		assertEquals(Set.of(rootId, "LOOP", "FOO"),
				rootDataset.getDataStructure().keySet());
		assertEquals(Set.of(rootId, "LOOP", "FOO1"),
				loopDataset.getDataStructure().keySet());
		//
		assertEquals(3, rootDataset.getDataPoints().size());
		assertEquals("T01", rootDataset.getDataPoints().get(0).get(rootId));
		assertEquals("foo1", rootDataset.getDataPoints().get(0).get("FOO"));
		assertEquals("T02", rootDataset.getDataPoints().get(2).get(rootId));
		assertEquals("foo2", rootDataset.getDataPoints().get(2).get("FOO"));
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
	
	@Test
	void applyInformationLevelsProcessingWithPartialIdNUll() {
		KraftwerkExecutionContext kraftwerkExecutionContext = TestConstants.getKraftwerkExecutionContext();
		//
		VtlBindings vtlBindings = new VtlBindings();
		vtlBindings.put("MULTIMODE", testDatasetWithPartialIdNull);
		//
		InformationLevelsProcessing processing = new InformationLevelsProcessing(
				vtlBindings,
				fileUtilsInterface,
				kraftwerkExecutionContext
		);
		processing.applyAutomatedVtlInstructions("MULTIMODE", kraftwerkExecutionContext);
		//
		Dataset rootDataset = vtlBindings.getDataset(Constants.ROOT_GROUP_NAME);
		Dataset loopDataset = vtlBindings.getDataset("LOOP");

		//
		assertNotNull(rootDataset);
		assertNotNull(loopDataset);
		// We have to keep the LOOP identifier since VTL 2
		// TODO Think about adding loop identifier as measure (fr/insee/kraftwerk/core/vtl/VtlJsonDatasetWriter.java:148)
		assertEquals(Set.of(rootId, "LOOP", "FOO"),
				rootDataset.getDataStructure().keySet());
		assertEquals(Set.of(rootId, "LOOP", "FOO1"),
				loopDataset.getDataStructure().keySet());
		//
		assertEquals(3, rootDataset.getDataPoints().size());
		assertEquals("T01", rootDataset.getDataPoints().get(0).get(rootId));
		assertEquals("foo1", rootDataset.getDataPoints().get(0).get("FOO"));
		assertEquals("T02", rootDataset.getDataPoints().get(2).get(rootId));
		assertEquals("foo2", rootDataset.getDataPoints().get(2).get("FOO"));
		//
		assertEquals(2, loopDataset.getDataPoints().size());
		assertEquals("T01", loopDataset.getDataPoints().get(0).get(rootId));
		assertEquals("LOOP-01", loopDataset.getDataPoints().get(0).get("LOOP"));
		assertEquals("foo11", loopDataset.getDataPoints().get(0).get("FOO1"));
		assertEquals("T01", loopDataset.getDataPoints().get(1).get(rootId));
		assertEquals("LOOP-02", loopDataset.getDataPoints().get(1).get("LOOP"));
		assertEquals("foo12", loopDataset.getDataPoints().get(1).get("FOO1"));
	}

}
