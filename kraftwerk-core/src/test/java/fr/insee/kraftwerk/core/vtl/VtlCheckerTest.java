package fr.insee.kraftwerk.core.vtl;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VtlCheckerTest {

	Dataset ds1 = new InMemoryDataset(
			List.of(
					List.of("UE001", "Lille", "INDIVIDU-1", "Jean", 30),
					List.of("UE001", "Lille", "INDIVIDU-2", "Frédéric", 42),
					List.of("UE004", "Amiens", "INDIVIDU-1", "David", 26),
					List.of("UE005", "", "INDIVIDU-1", "Thibaud ", 18)
			),
			List.of(
					new Structured.Component(Constants.ROOT_IDENTIFIER_NAME, String.class, Dataset.Role.IDENTIFIER),
					new Structured.Component("LIB_COMMUNE", String.class, Dataset.Role.MEASURE),
					new Structured.Component("INDIVIDU", String.class, Dataset.Role.IDENTIFIER),
					new Structured.Component("INDIVIDU.PRENOM", String.class, Dataset.Role.MEASURE),
					new Structured.Component("INDIVIDU.AGE", Integer.class, Dataset.Role.MEASURE)
			)
	);


	private String vtlToFix = "TEST := TEST [calc test_month := substr(cast(current_date(), string, \"YYYY-MM-DD\"), 6, 2)];\n" +
			"TEST := TEST [calc test_sum := sum(cast(VAR1,integer))];\n" +
			"TEST := TEST [calc test_sum2 := sum(VAR2)];\n" +
			"TEST := TEST [calc test_first := first_value(VAR1 over())];\n";


	private String vtlFixed =
			"TEST := TEST [aggr test_month := substr(cast(OUTCOME_DATE, string, \"YYYY-MM-DD\"), 6, 2)];\n" +
			"TEST := TEST [aggr test_sum := sum(cast(VAR1,integer) group by IdUE, INDIVIDU )];\n" +
			"TEST := TEST [aggr test_sum2 := sum(VAR2 group by IdUE, INDIVIDU )];\n" +
			"TEST := TEST [aggr test_first := first_value(VAR1 over(PARTITION BY IdUE order by (INDIVIDU)))];\n";


	@Test
	void fixVtlExpressionNoChange() {
		String vtlExpression = "abc";
		String bindingName = "abc";
		VtlBindings vtlBindings = new VtlBindings();
		String expected = "abc";
		String actual = VtlChecker.fixVtlExpression(vtlExpression, bindingName, vtlBindings);

		assertEquals(expected, actual);
	}

	@Test
	void fixVtlExpression() {

		String bindingName = "TEST";
		VtlBindings vtlBindings = new VtlBindings();
		vtlBindings.put(bindingName, ds1);

		String actual = VtlChecker.fixVtlExpression(vtlToFix, bindingName, vtlBindings);

		assertEquals(vtlFixed, actual);
	}
}


