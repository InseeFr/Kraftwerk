package fr.insee.kraftwerk.core.vtl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.vtl.jackson.TrevasModule;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import org.junit.jupiter.api.Test;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrevasTests {

    @Test
    void largeExpressionWithNoResult() throws IOException {
        //
        Bindings bindings = new SimpleBindings();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new TrevasModule());
        Dataset dataset = mapper.readValue(
                TrevasTests.class.getClassLoader().getResource("unit_tests/vtl/sample.json"),
                Dataset.class);
        bindings.put("ds", dataset);
        //
        String expression = """
                ds := ds [calc S2_MAA1AT := if (not(isnull(S2_MAA2AT))
                and isnull(S2_MAA2ATC) and isnull(S2_MAA3AT) )
                then S2_MAA2AT else (if (not(isnull(S2_MAA2AT))
                and not(isnull(S2_MAA2ATC)) and isnull(S2_MAA3AT) )
                then (if ((cast(S2_MAA2AT,integer)<cast(S2_MAA2ATC,integer)) and MARRIVC=\"2\" )
                then S2_MAA2ATC else S2_MAA2AT) else (if (not(isnull(S2_MAA2AT)) and isnull(S2_MAA2ATC)
                and not(isnull(S2_MAA3AT)) ) then (if ((cast(S2_MAA2AT,integer)<cast(S2_MAA3AT,integer))
                and MAA3=\"1\" ) then S2_MAA3AT else S2_MAA2AT)
                else (if (not(isnull(S2_MAA2AT)) and not(isnull(S2_MAA2ATC))
                and not(isnull(S2_MAA3AT)) ) then (if ((cast(S2_MAA2AT,integer)<cast(S2_MAA3AT,integer))
                and MAA3=\"1\" ) then (if (cast(S2_MAA3AT,integer)<cast(S2_MAA2ATC,integer)
                and MARRIVC=\"2\") then S2_MAA2ATC else S2_MAA3AT )
                else (if ((cast(S2_MAA2AT,integer)<cast(S2_MAA2ATC,integer))
                and MARRIVC=\"2\") then S2_MAA2ATC else S2_MAA2AT ) ) else \"\" )))
                ];
                """;
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("vtl");
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

        //
        assertThrows(NumberFormatException.class, () -> engine.eval(expression));
        //
        //assertTrue(((Dataset) bindings.get("ds")).getDataStructure().containsKey("S2_MAA1AT"));
    }

    @Test
    void expressionFailingForEveryone() throws ScriptException {
        //
        Bindings bindings = new SimpleBindings();
        Dataset dataset = new InMemoryDataset(
                List.of(
                        List.of(1L, "9"),
                        List.of(2L, "8"),
                        List.of(3L, "101")),
                List.of(
                        new Structured.Component("ID", Long.class, Dataset.Role.IDENTIFIER),
                        new Structured.Component("FOO", String.class, Dataset.Role.MEASURE))
        );
        bindings.put("ds", dataset);
        //
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("vtl");
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

        //
        engine.eval("ds := ds [calc FOO_NUM := cast(FOO, integer)];");

        //
        assertTrue(((Dataset) bindings.get("ds")).getDataStructure().containsKey("FOO_NUM"));
        assertEquals(9L, ((Dataset) bindings.get("ds")).getDataPoints().get(0).get("FOO_NUM"));
        assertEquals(8L, ((Dataset) bindings.get("ds")).getDataPoints().get(1).get("FOO_NUM"));
        assertEquals(101L, ((Dataset) bindings.get("ds")).getDataPoints().get(2).get("FOO_NUM"));
    }

    @Test
    void elseWithSameVariable() throws ScriptException {
        //
        Bindings bindings = new SimpleBindings();
        Dataset dataset = new InMemoryDataset(
                List.of(
                        List.of(1L, "9"),
                        List.of(2L, "8"),
                        List.of(3L, "7")),
                List.of(
                        new Structured.Component("ID", Long.class, Dataset.Role.IDENTIFIER),
                        new Structured.Component("FOO", String.class, Dataset.Role.MEASURE))
        );
        bindings.put("ds", dataset);
        //
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("vtl");
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

        //
        engine.eval("ds := ds [calc FOO := if FOO=\"9\" then \"5\" else FOO];");

        //
        assertTrue(((Dataset) bindings.get("ds")).getDataStructure().containsKey("FOO"));
        assertEquals("5", ((Dataset) bindings.get("ds")).getDataPoints().get(0).get("FOO"));
        assertEquals("8", ((Dataset) bindings.get("ds")).getDataPoints().get(1).get("FOO"));
        assertEquals("7", ((Dataset) bindings.get("ds")).getDataPoints().get(2).get("FOO"));
    }
}
