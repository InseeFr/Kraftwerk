package fr.insee.kraftwerk.vtl;

import fr.insee.kraftwerk.TestConstants;
import fr.insee.kraftwerk.dataprocessing.DataProcessingManager;
import fr.insee.kraftwerk.dataprocessing.UnimodalDataProcessing;
import fr.insee.kraftwerk.inputs.ModeInputs;
import fr.insee.kraftwerk.inputs.UserInputs;
import fr.insee.kraftwerk.metadata.DDIReader;
import fr.insee.kraftwerk.parsers.DataParser;
import fr.insee.kraftwerk.parsers.DataParserManager;
import fr.insee.kraftwerk.rawdata.SurveyRawData;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import fr.insee.vtl.model.Structured.Component;
import fr.insee.vtl.model.Structured.DataPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

/**
 * Class to do some tests relative to Trevas VTL implementation, not properly
 * related with Kraftwerk goals.
 */
public class TrevasTests {

	VtlBindings vtlBindings;
	ScriptEngine engine;

	Dataset dsTest = new InMemoryDataset(List.of(List.of(1L, "3", 5L), List.of(2L, "4", 6L)),
			List.of(new Structured.Component("ID", Long.class, Dataset.Role.IDENTIFIER),
					new Structured.Component("VAR1", String.class, Dataset.Role.MEASURE),
					new Structured.Component("VAR2", Long.class, Dataset.Role.MEASURE)));

	@BeforeEach
	public void setUp() {
		vtlBindings = new VtlBindings();
		engine = new ScriptEngineManager().getEngineByName("vtl");
	}

	@Test
	public void testCast() {
		//
		vtlBindings.getBindings().put("ds", dsTest);
		//
		vtlBindings.evalVtlScript("ds := ds [calc VAR1_NUM := cast(VAR1, integer)];");
		vtlBindings.evalVtlScript("ds := ds [calc VAR2_STR := cast(VAR2, string)];");
		//
		Dataset dsOut = vtlBindings.getDataset("ds");

		//
		assertEquals(3L, dsOut.getDataPoints().get(0).get("VAR1_NUM"));
		assertEquals(4L, dsOut.getDataPoints().get(1).get("VAR1_NUM"));
		assertEquals("5", dsOut.getDataPoints().get(0).get("VAR2_STR"));
		assertEquals("6", dsOut.getDataPoints().get(1).get("VAR2_STR"));
	}

	@Test
	public void testUnionIncompatibleStructure() {

		InMemoryDataset dataset1 = new InMemoryDataset(List.of(),
				List.of(new Component("name", String.class, Dataset.Role.IDENTIFIER),
						new Component("age", Long.class, Dataset.Role.MEASURE),
						new Component("weight", Long.class, Dataset.Role.MEASURE)));
		InMemoryDataset dataset2 = new InMemoryDataset(List.of(),
				List.of(new Component("age", Long.class, Dataset.Role.MEASURE),
						new Component("name", String.class, Dataset.Role.IDENTIFIER),
						new Component("weight", Long.class, Dataset.Role.MEASURE)));
		InMemoryDataset dataset3 = new InMemoryDataset(List.of(),
				List.of(new Component("name2", String.class, Dataset.Role.IDENTIFIER),
						new Component("age", Long.class, Dataset.Role.MEASURE),
						new Component("weight", Long.class, Dataset.Role.MEASURE)));
		ScriptContext context = engine.getContext();
		context.getBindings(ScriptContext.ENGINE_SCOPE).put("ds1", dataset1);
		context.getBindings(ScriptContext.ENGINE_SCOPE).put("ds2", dataset2);
		context.getBindings(ScriptContext.ENGINE_SCOPE).put("ds3", dataset3);

		assertThatThrownBy(() -> engine.eval("result := union(ds1, ds2, ds3);"))
				.hasMessageContaining("ds3 is incompatible");
	}

	@Test
	public void testUnionSimple() throws ScriptException {

		InMemoryDataset dataset = new InMemoryDataset(List.of(Map.of("name", "Hadrien", "age", 10L, "weight", 11L),
				Map.of("name", "Nico", "age", 11L, "weight", 10L), Map.of("name", "Franck", "age", 12L, "weight", 9L)),
				Map.of("name", String.class, "age", Long.class, "weight", Long.class),
				Map.of("name", Dataset.Role.IDENTIFIER, "age", Dataset.Role.MEASURE, "weight", Dataset.Role.MEASURE));
		ScriptContext context = engine.getContext();
		context.getBindings(ScriptContext.ENGINE_SCOPE).put("ds1", dataset);
		context.getBindings(ScriptContext.ENGINE_SCOPE).put("ds2", dataset);

		engine.eval("result := union(ds1, ds2);");
		Object result = engine.getContext().getAttribute("result");
		assertThat(result).isInstanceOf(Dataset.class);
		assertThat(((Dataset) result).getDataAsMap()).containsAll(dataset.getDataAsMap());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUnionDifferentStructure() throws ScriptException {

		// Given
		var structure = new Structured.DataStructure(Map.of("id", String.class), Map.of("id", Dataset.Role.IDENTIFIER));
		InMemoryDataset ds1 = new InMemoryDataset(structure, List.of("1"), List.of("2"), List.of("3"), List.of("4"));
		InMemoryDataset ds2 = new InMemoryDataset(structure, List.of("3"), List.of("4"), List.of("5"), List.of("6"));
		var bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
		bindings.put("ds1", ds1);
		bindings.put("ds2", ds2);

		// When
		engine.eval("ds1 := ds1 [calc A := \"A\"]; " + "ds1 := ds1 [calc B := \"B\"];");
		engine.eval("ds2 := ds2 [calc B := \"B\"]; " + "ds2 := ds2 [calc A := \"A\"];");
		engine.eval("ds3 := union(ds1, ds2);");
		engine.eval("ds4 := union(ds2, ds1);");

		// Then
		var ds3 = (Dataset) bindings.get("ds3");
		var ds4 = (Dataset) bindings.get("ds4");

		var onlyA = ds3.getDataAsMap().stream().map(m -> m.get("A")).distinct().collect(Collectors.toList());
		assertThat(onlyA).containsExactly("A");

		var onlyB = ds4.getDataAsMap().stream().map(m -> m.get("B")).distinct().collect(Collectors.toList());
		assertThat(onlyB).containsExactly("B");

		var onlyAList = ds3.getDataAsList().stream().map(l -> l.get(ds3.getDataStructure().indexOfKey("A"))).distinct()
				.collect(Collectors.toList());
		assertThat(onlyAList).containsExactly("A");

		var onlyBList = ds4.getDataAsList().stream().map(l -> l.get(ds4.getDataStructure().indexOfKey("B"))).distinct()
				.collect(Collectors.toList());
		assertThat(onlyBList).containsExactly("B");

		var onlyADatapoint = ds3.getDataPoints().stream().map(d -> d.get("A")).distinct().collect(Collectors.toList());
		assertThat(onlyADatapoint).containsExactly("A");

		var onlyBDatapoint = ds4.getDataPoints().stream().map(d -> d.get("B")).distinct().collect(Collectors.toList());
		assertThat(onlyBDatapoint).containsExactly("B");
	}

	@Test
	public void testUnion() throws ScriptException {

		InMemoryDataset dataset1 = new InMemoryDataset(
				List.of(Map.of("name", "Hadrien", "age", 10L, "weight", 11L),
						Map.of("name", "Nico", "age", 11L, "weight", 10L)),
				Map.of("name", String.class, "age", Long.class, "weight", Long.class),
				Map.of("name", Dataset.Role.IDENTIFIER, "age", Dataset.Role.MEASURE, "weight", Dataset.Role.MEASURE));
		InMemoryDataset dataset2 = new InMemoryDataset(
				List.of(Map.of("name", "Hadrien", "weight", 11L, "age", 10L),
						Map.of("name", "Franck", "weight", 9L, "age", 12L)),
				Map.of("name", String.class, "weight", Long.class, "age", Long.class),
				Map.of("name", Dataset.Role.IDENTIFIER, "age", Dataset.Role.MEASURE, "weight", Dataset.Role.MEASURE));
		ScriptContext context = engine.getContext();
		context.getBindings(ScriptContext.ENGINE_SCOPE).put("ds1", dataset1);
		context.getBindings(ScriptContext.ENGINE_SCOPE).put("ds2", dataset2);

		engine.eval("result := union(ds1, ds2);");
		Object result = engine.getContext().getAttribute("result");
		assertThat(result).isInstanceOf(Dataset.class);
		assertThat(((Dataset) result).getDataAsMap()).containsExactlyInAnyOrder(
				Map.of("name", "Hadrien", "age", 10L, "weight", 11L), Map.of("name", "Nico", "age", 11L, "weight", 10L),
				Map.of("name", "Franck", "age", 12L, "weight", 9L));

	}

	@Test
	public void testUnionMultiple() throws ScriptException {

		InMemoryDataset dataset1 = new InMemoryDataset(
				List.of(Map.of("name", "Hadrien", "age", 10L, "weight", 11L),
						Map.of("name", "Nico", "age", 11L, "weight", 10L)),
				Map.of("name", String.class, "age", Long.class, "weight", Long.class),
				Map.of("name", Dataset.Role.IDENTIFIER, "age", Dataset.Role.MEASURE, "weight", Dataset.Role.MEASURE));
		InMemoryDataset dataset2 = new InMemoryDataset(
				List.of(Map.of("name", "Hadrien2", "age", 10L, "weight", 11L),
						Map.of("name", "Franck", "age", 12L, "weight", 9L)),
				Map.of("name", String.class, "age", Long.class, "weight", Long.class),
				Map.of("name", Dataset.Role.IDENTIFIER, "age", Dataset.Role.MEASURE, "weight", Dataset.Role.MEASURE));
		InMemoryDataset dataset3 = new InMemoryDataset(
				List.of(Map.of("name", "Hadrien", "age", 10L, "weight", 11L),
						Map.of("name", "Franck2", "age", 12L, "weight", 9L)),
				Map.of("name", String.class, "age", Long.class, "weight", Long.class),
				Map.of("name", Dataset.Role.IDENTIFIER, "age", Dataset.Role.MEASURE, "weight", Dataset.Role.MEASURE));

		ScriptContext context = engine.getContext();
		context.getBindings(ScriptContext.ENGINE_SCOPE).put("ds1", dataset1);
		context.getBindings(ScriptContext.ENGINE_SCOPE).put("ds2", dataset2);
		context.getBindings(ScriptContext.ENGINE_SCOPE).put("ds3", dataset3);

		engine.eval("result := union(ds1, ds2, ds3);");
		Object result = engine.getContext().getAttribute("result");
		assertThat(result).isInstanceOf(Dataset.class);
		assertThat(((Dataset) result).getDataAsMap()).containsExactlyInAnyOrder(
				Map.of("name", "Hadrien", "age", 10L, "weight", 11L), Map.of("name", "Nico", "age", 11L, "weight", 10L),
				Map.of("name", "Franck", "age", 12L, "weight", 9L), Map.of("name", "Franck2", "age", 12L, "weight", 9L),
				Map.of("name", "Hadrien2", "age", 10L, "weight", 11L));
	}

	@Test
	public void testUnionKraftwerk() throws ScriptException {

		UserInputs userInputs = new UserInputs(
				TestConstants.TEST_FUNCTIONAL_INPUT_DIRECTORY + "/VQS-2021-x00/kraftwerk-union.json",
				Paths.get(TestConstants.TEST_FUNCTIONAL_INPUT_DIRECTORY + "/VQS-2021-x00"));

		for (String dataMode : userInputs.getModeInputsMap().keySet()) {

			ModeInputs modeInputs = userInputs.getModeInputs(dataMode);

			SurveyRawData data = new SurveyRawData();

			data.setDataFilePath(modeInputs.getDataFile());
			data.setVariablesMap(DDIReader.getVariablesFromDDI(modeInputs.getDDIFile()));

			DataParser parser = DataParserManager.getParser(modeInputs.getDataFormat());
			parser.parseSurveyData(data);

			vtlBindings.convertToVtlDataset(data, dataMode);

			UnimodalDataProcessing dataProcessing = DataProcessingManager.getProcessingClass(modeInputs.getDataFormat(),
					vtlBindings);
			dataProcessing.applyVtlTransformations(dataMode, modeInputs.getModeVtlFile(), data.getVariablesMap());

		}

		StringBuilder vtlScript = new StringBuilder();
		vtlScript.append(
				"MULTIMODE := WEB [keep IdUE, BOUCLEINDIV, BOUCLEINDIV.DECISION, BOUCLEINDIV.LIMITAT, BOUCLEINDIV.SORTIES, BOUCLEINDIV.RECONNAI, BOUCLEINDIV.COMPRENDRE, BOUCLEINDIV.AMENALOG, BOUCLEINDIV.HPSY, BOUCLEINDIV.ETAT_SANT, BOUCLEINDIV.SEXE, BOUCLEINDIV.MAIN, BOUCLEINDIV.AIDREG_A, BOUCLEINDIV.AIDREG_B, BOUCLEINDIV.AIDREG_C, BOUCLEINDIV.MARCHE, BOUCLEINDIV.BRAS, BOUCLEINDIV.AIDREG_D, BOUCLEINDIV.AIDTECH, BOUCLEINDIV.DTNAIS, BOUCLEINDIV.AIDENT, BOUCLEINDIV.VUE, BOUCLEINDIV.CONCENTRA, BOUCLEINDIV.NOM, BOUCLEINDIV.PRENOM, BOUCLEINDIV.MAL_CHRO, BOUCLEINDIV.PSY, BOUCLEINDIV.RESIDANCIEN, BOUCLEINDIV.AUDITIF, BOUCLEINDIV.HANDICAP, BOUCLEINDIV.APPRENT, BOUCLEINDIV.AIDPRO];");

		vtlScript.append(
				"MULTIMODE := union(MULTIMODE, PAPER [keep IdUE, BOUCLEINDIV, BOUCLEINDIV.DECISION, BOUCLEINDIV.LIMITAT, BOUCLEINDIV.SORTIES, BOUCLEINDIV.RECONNAI, BOUCLEINDIV.COMPRENDRE, BOUCLEINDIV.AMENALOG, BOUCLEINDIV.HPSY, BOUCLEINDIV.ETAT_SANT, BOUCLEINDIV.SEXE, BOUCLEINDIV.MAIN, BOUCLEINDIV.AIDREG_A, BOUCLEINDIV.AIDREG_B, BOUCLEINDIV.AIDREG_C, BOUCLEINDIV.MARCHE, BOUCLEINDIV.BRAS, BOUCLEINDIV.AIDREG_D, BOUCLEINDIV.AIDTECH, BOUCLEINDIV.DTNAIS, BOUCLEINDIV.AIDENT, BOUCLEINDIV.VUE, BOUCLEINDIV.CONCENTRA, BOUCLEINDIV.NOM, BOUCLEINDIV.PRENOM, BOUCLEINDIV.MAL_CHRO, BOUCLEINDIV.PSY, BOUCLEINDIV.RESIDANCIEN, BOUCLEINDIV.AUDITIF, BOUCLEINDIV.HANDICAP, BOUCLEINDIV.APPRENT, BOUCLEINDIV.AIDPRO]);");
		// Save dataset at this step
		vtlScript.append("MULTIMODE_POSTUNION := MULTIMODE;");
		vtlScript.append("\n");

		vtlScript.append(
				"CAWI_keep := WEB [keep IdUE, BOUCLEINDIV, BOUCLEINDIV.PRENOMOK, BOUCLEINDIV.RELATION1, BOUCLEINDIV.RELATION2, BOUCLEINDIV.RELATION3, BOUCLEINDIV.RELATION4, NBPRENOMOK, LIBINSTRUCT, ADRESSE, LIBCONTACT, OKADRESS, RESIDM, NVOI, NVOIC, NLIB, NCOMP, NCODPOS, NCOMMU, NHAB];");

		// Save dataset at this step
		vtlScript.append("MULTIMODE_PREJOIN_CAWI := MULTIMODE;");
		vtlScript.append("\n");
		vtlScript.append("MULTIMODE := left_join(MULTIMODE, CAWI_keep using IdUE, BOUCLEINDIV);");

		// Save dataset at this step
		vtlScript.append("MULTIMODE_POSTJOIN_CAWI := MULTIMODE;");
		vtlScript.append("\n");

		vtlScript.append(
				"PAPI_keep := PAPER [keep IdUE, BOUCLEINDIV, NBQUEST, BOUCLEINDIV.BOUCLEINDIV.SEXE_1, BOUCLEINDIV.BOUCLEINDIV.SEXE_2, BOUCLEINDIV.BOUCLEINDIV.LIMITAT_1, BOUCLEINDIV.LIMITAT_2, BOUCLEINDIV.LIMITAT_3, BOUCLEINDIV.VUE_1, BOUCLEINDIV.VUE_2, BOUCLEINDIV.VUE_3, BOUCLEINDIV.VUE_4, BOUCLEINDIV.AUDITIF_1, BOUCLEINDIV.AUDITIF_2, BOUCLEINDIV.AUDITIF_3, BOUCLEINDIV.AUDITIF_4, BOUCLEINDIV.MARCHE_1, BOUCLEINDIV.MARCHE_2, BOUCLEINDIV.MARCHE_3, BOUCLEINDIV.MARCHE_4, BOUCLEINDIV.BRAS_1, BOUCLEINDIV.BRAS_2, BOUCLEINDIV.BRAS_3, BOUCLEINDIV.BRAS_4, BOUCLEINDIV.MAIN_1, BOUCLEINDIV.MAIN_2, BOUCLEINDIV.MAIN_3, BOUCLEINDIV.MAIN_4, BOUCLEINDIV.CONCENTRA_1, BOUCLEINDIV.CONCENTRA_2, BOUCLEINDIV.CONCENTRA_3, BOUCLEINDIV.CONCENTRA_4, BOUCLEINDIV.DECISION_1, BOUCLEINDIV.DECISION_2, BOUCLEINDIV.DECISION_3, BOUCLEINDIV.DECISION_4, BOUCLEINDIV.SORTIES_1, BOUCLEINDIV.SORTIES_2, BOUCLEINDIV.SORTIES_3, BOUCLEINDIV.SORTIES_4, BOUCLEINDIV.PSY_1, BOUCLEINDIV.PSY_2_, BOUCLEINDIV.PSY_3, BOUCLEINDIV.HPSY_1, BOUCLEINDIV.HPSY_2, BOUCLEINDIV.AIDPRO_1, BOUCLEINDIV.AIDPRO_2, BOUCLEINDIV.AIDENT_1, BOUCLEINDIV.AIDENT_2, BOUCLEINDIV.AMENALOG_1, BOUCLEINDIV.AMENALOG_2, BOUCLEINDIV.AIDTECH_1, BOUCLEINDIV.AIDTECH_2, BOUCLEINDIV.HANDICAP_1, BOUCLEINDIV.HANDICAP_2, BOUCLEINDIV.RECONNAI_1, BOUCLEINDIV.RECONNAI_2, BOUCLEINDIV.APPRENT_1, BOUCLEINDIV.APPRENT_2, BOUCLEINDIV.APPRENT_3, BOUCLEINDIV.RESID_1, BOUCLEINDIV.RESID_2, BOUCLEINDIV.RESIDANCIEN_1, BOUCLEINDIV.RESIDANCIEN_2, BOUCLEINDIV.RESID];");

		vtlScript.append("\n");
		// Save dataset at this step
		vtlScript.append("MULTIMODE_PREJOIN_PAPI := MULTIMODE;");
		vtlScript.append("\n");
		vtlScript.append("MULTIMODE := left_join(MULTIMODE, PAPI_keep using IdUE, BOUCLEINDIV);");
		// Save dataset at this step
		vtlScript.append("MULTIMODE_POSTJOIN_PAPI := MULTIMODE;");
		vtlScript.append("\n");
		vtlBindings.evalVtlScript(vtlScript.toString());
		DataPoint dataPoint = vtlBindings.getDataset("PAPER").getDataPoints().stream()
				.filter(dataPointToSearch -> "VQS0588832".equals(dataPointToSearch.get("IdUE"))).findAny().orElse(null);

		assertEquals("3", dataPoint.get("BOUCLEINDIV.LIMITAT"));
		dataPoint = vtlBindings.getDataset("MULTIMODE_POSTUNION").getDataPoints().stream()
				.filter(dataPointToSearch -> "VQS0588832".equals(dataPointToSearch.get("IdUE"))).findAny().orElse(null);
		assertEquals("3", dataPoint.get("BOUCLEINDIV.LIMITAT"));

		dataPoint = vtlBindings.getDataset("MULTIMODE_PREJOIN_CAWI").getDataPoints().stream()
				.filter(dataPointToSearch -> "VQS0588832".equals(dataPointToSearch.get("IdUE"))).findAny().orElse(null);
		assertEquals("3", dataPoint.get("BOUCLEINDIV.LIMITAT"));

		// At this point we should still have LIMITAT = 3 in the Datapoint VQS0588832
		// which is only in PAPER, not in WEB, but it returns 0, meaning the value has
		// been wrongly changed in the former version of Trevas
		// -> Need to check every step

		dataPoint = vtlBindings.getDataset("MULTIMODE_POSTJOIN_CAWI").getDataPoints().stream()
				.filter(dataPointToSearch -> "VQS0588832".equals(dataPointToSearch.get("IdUE"))).findAny().orElse(null);
		assertEquals("3", dataPoint.get("BOUCLEINDIV.LIMITAT"));

		dataPoint = vtlBindings.getDataset("MULTIMODE_PREJOIN_PAPI").getDataPoints().stream()
				.filter(dataPointToSearch -> "VQS0588832".equals(dataPointToSearch.get("IdUE"))).findAny().orElse(null);
		assertEquals("3", dataPoint.get("BOUCLEINDIV.LIMITAT"));

		dataPoint = vtlBindings.getDataset("MULTIMODE_POSTJOIN_PAPI").getDataPoints().stream()
				.filter(dataPointToSearch -> "VQS0588832".equals(dataPointToSearch.get("IdUE"))).findAny().orElse(null);
		assertEquals("3", dataPoint.get("BOUCLEINDIV.LIMITAT"));

		dataPoint = vtlBindings.getDataset("MULTIMODE_POSTUNION").getDataPoints().stream()
				.filter(dataPointToSearch -> "VQS0588832".equals(dataPointToSearch.get("IdUE"))).findAny().orElse(null);
		assertEquals("3", dataPoint.get("BOUCLEINDIV.LIMITAT"));

	}
/*
	@Test
	public void testLeftJoinKraftwerk() throws ScriptException {

		InMemoryDataset dataset1 = new InMemoryDataset(
				List.of(Map.of("name", "Hadrien", "age", "age50", "yeux", "yeuxmarrons", "weight", "weight85"),
						Map.of("name", "Nico", "age", "age40", "yeux", "yeuxbleus", "weight", "weight75")),
				Map.of("name", String.class, "age", String.class, "yeux", String.class, "weight", String.class),
				Map.of("name", Dataset.Role.IDENTIFIER, "age", Dataset.Role.MEASURE, "yeux", Dataset.Role.MEASURE,
						"weight", Dataset.Role.MEASURE));
		InMemoryDataset dataset2 = new InMemoryDataset(List.of(
				Map.of("name", "Hadrien", "newAge", "", "weight", "weight25", "age", "age30", "weight2", "weight95"),
				Map.of("name", "Franck", "newAge", "", "weight", "weight65", "age", "age70", "weight2", "weight105")),
				Map.of("name", String.class, "newAge", String.class, "weight", String.class, "age", String.class,
						"weight2", String.class),
				Map.of("name", Dataset.Role.IDENTIFIER, "newAge", Dataset.Role.MEASURE, "weight", Dataset.Role.MEASURE,
						"age", Dataset.Role.MEASURE, "weight2", Dataset.Role.MEASURE));

		vtlBindings.getBindings().put("ds1", dataset1);
		vtlBindings.getBindings().put("ds2", dataset2);
		vtlBindings.evalVtlScript("unionData := union(ds1[keep name, age, weight], ds2[keep name, age, weight]);");

		vtlBindings.evalVtlScript("ds1_keep := ds1[keep name, yeux];");
		vtlBindings.evalVtlScript("joinData := left_join(unionData, ds1_keep using name);");

	}
*/
}