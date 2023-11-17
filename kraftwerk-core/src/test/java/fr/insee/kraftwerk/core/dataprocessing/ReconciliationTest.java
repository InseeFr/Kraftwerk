package fr.insee.kraftwerk.core.dataprocessing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Dataset.Role;
import fr.insee.vtl.model.InMemoryDataset;

class ReconciliationTest {

	private VtlBindings vtlBindings;
	private List<KraftwerkError> errors;

	InMemoryDataset capiDataset = new InMemoryDataset(
			List.of(
					Map.of("ID", "T01", "LOOP", "LOOP-01", "FOO", "foo1", "CAPI_SPECIFIC", "foo", "LOOP.FOO1", "foo11"),
					Map.of("ID", "T01", "LOOP", "LOOP-02", "FOO", "foo1", "CAPI_SPECIFIC", "foo", "LOOP.FOO1", "foo12")
			),
			Map.of("ID", String.class, "LOOP", String.class,
					"FOO", String.class, "CAPI_SPECIFIC", String.class, "LOOP.FOO1", String.class
			),
			Map.of("ID", Role.IDENTIFIER, "LOOP", Role.IDENTIFIER,
					"FOO", Role.MEASURE, "CAPI_SPECIFIC", Role.MEASURE, "LOOP.FOO1", Role.MEASURE
			)
	);

	InMemoryDataset cawiDataset = new InMemoryDataset(
			List.of(
					Map.of("ID", "T02", "LOOP", "LOOP-01", "FOO", "foo2", "CAWI_SPECIFIC", "foo", "LOOP.FOO1", "foo21"),
					Map.of("ID", "T03", "LOOP", "LOOP-01", "FOO", "foo3", "CAWI_SPECIFIC", "foo", "LOOP.FOO1", "foo31"),
					Map.of("ID", "T03", "LOOP", "LOOP-02", "FOO", "foo3", "CAWI_SPECIFIC", "foo", "LOOP.FOO1", "foo32")
			),
			Map.of("ID", String.class, "LOOP", String.class,
					"FOO", String.class, "CAWI_SPECIFIC", String.class, "LOOP.FOO1", String.class
			),
			Map.of("ID", Role.IDENTIFIER, "LOOP", Role.IDENTIFIER,
					"FOO", Role.MEASURE, "CAWI_SPECIFIC", Role.MEASURE, "LOOP.FOO1", Role.MEASURE
			)
	);

	InMemoryDataset papiDataset = new InMemoryDataset(
			List.of(
					Map.of("ID", "T02", "LOOP", "LOOP-0123", "LOOP.FOO1", "foo2paper1", "LOOP.PAPER_SPECIFIC", "foo"),
					Map.of("ID", "T04", "LOOP", "LOOP-1234", "LOOP.FOO1", "foo4paper1", "LOOP.PAPER_SPECIFIC", "foo"),
					Map.of("ID", "T04", "LOOP", "LOOP-2345", "LOOP.FOO1", "foo4paper2", "LOOP.PAPER_SPECIFIC", "foo"),
					Map.of("ID", "T04", "LOOP", "LOOP-3456", "LOOP.FOO1", "foo4paper3", "LOOP.PAPER_SPECIFIC", "foo")
			),
			Map.of("ID", String.class, "LOOP", String.class,
					"LOOP.FOO1", String.class, "LOOP.PAPER_SPECIFIC", String.class
			),
			Map.of("ID", Role.IDENTIFIER, "LOOP", Role.IDENTIFIER,
					"LOOP.FOO1", Role.MEASURE, "LOOP.PAPER_SPECIFIC", Role.MEASURE
			)
	);

	Map<String, Dataset> testDatasets = Map.of("CAPI", capiDataset, "CAWI", cawiDataset, "PAPI", papiDataset);

	@BeforeEach
	void initVtlBindings() {
		vtlBindings = new VtlBindings();
		errors = new ArrayList<>();
	}

	@ParameterizedTest
	@ValueSource(strings = {"CAPI", "CAWI", "PAPI"})
	void applyReconciliation_singleMode(String dsName) {
		//
		vtlBindings.put("SINGLE_MODE", testDatasets.get(dsName));
		//
		ReconciliationProcessing reconciliation = new ReconciliationProcessing(vtlBindings);
		reconciliation.applyVtlTransformations("MULTIMODE", null,errors);
		//
		Dataset multimodeDataset = vtlBindings.getDataset("MULTIMODE");
		assertNotNull(multimodeDataset);
	}

	private Dataset applyReconciliation_twoModes(String mode1, String mode2) {
		//
		vtlBindings.put(mode1, testDatasets.get(mode1));
		vtlBindings.put(mode2, testDatasets.get(mode2));
		//
		ReconciliationProcessing reconciliation = new ReconciliationProcessing(vtlBindings);
		reconciliation.applyVtlTransformations("MULTIMODE", null,errors);
		//
		return vtlBindings.getDataset("MULTIMODE");
	}

	@Test
	void reconciliation_capiCawi() {
		Dataset multimodeDataset = applyReconciliation_twoModes("CAPI", "CAWI");
		//
		assertNotNull(multimodeDataset);
		//
		assertEquals(Role.IDENTIFIER, multimodeDataset.getDataStructure().get(Constants.MODE_VARIABLE_NAME).getRole());
		//
		assertEquals(Set.of("ID", "LOOP", "FOO", "CAPI_SPECIFIC", "CAWI_SPECIFIC", "LOOP.FOO1", Constants.MODE_VARIABLE_NAME),
				multimodeDataset.getDataStructure().keySet());
	}

	@Test
	void reconciliation_capiPapi() {
		Dataset multimodeDataset = applyReconciliation_twoModes("CAPI", "PAPI");
		//
		assertNotNull(multimodeDataset);
		//
		assertEquals(Set.of("ID", "LOOP", "FOO", "CAPI_SPECIFIC", "LOOP.FOO1", "LOOP.PAPER_SPECIFIC", Constants.MODE_VARIABLE_NAME),
				multimodeDataset.getDataStructure().keySet());
	}

	@Test
	void reconciliation_cawiPapi() {
		Dataset multimodeDataset = applyReconciliation_twoModes("CAWI", "PAPI");
		//
		assertNotNull(multimodeDataset);
		//
		assertEquals(Set.of("ID", "LOOP", "FOO", "CAWI_SPECIFIC", "LOOP.FOO1", "LOOP.PAPER_SPECIFIC", Constants.MODE_VARIABLE_NAME),
				multimodeDataset.getDataStructure().keySet());
	}

	@Test
	void applyReconciliation_threeModes() {
		vtlBindings.put("CAPI", cawiDataset);
		vtlBindings.put("CAWI", capiDataset);
		vtlBindings.put("PAPI", papiDataset);
		//
		ReconciliationProcessing reconciliation = new ReconciliationProcessing(vtlBindings);
		reconciliation.applyVtlTransformations("MULTIMODE", null,errors);
		//
		Dataset multimodeDataset = vtlBindings.getDataset("MULTIMODE");
		//
		assertNotNull(multimodeDataset);
		// TODO: improve reconciliation class or see with Trevas devs how to simplify
		//assertEquals(Set.of("ID", "LOOP", "FOO", "CAPI_SPECIFIC", "CAWI_SPECIFIC", "LOOP.FOO1", "LOOP.PAPER_SPECIFIC", Constants.MODE_VARIABLE_NAME),
		//		multimodeDataset.getDataStructure().keySet());
	}
}
