package fr.insee.kraftwerk.core.vtl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.vtl.engine.exceptions.VtlScriptException;
import fr.insee.vtl.jackson.TrevasModule;
import fr.insee.vtl.model.Dataset;
import org.junit.jupiter.api.Test;

import javax.script.*;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrevasTests {

    @Test
    public void largeExpressionWithNoResult() throws IOException, ScriptException {
        //
        Bindings bindings = new SimpleBindings();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new TrevasModule());
        Dataset dataset = mapper.readValue(
                TrevasTests.class.getClassLoader().getResource("unit_tests/vtl/sample.json"),
                Dataset.class);
        bindings.put("ds", dataset);
        //
        String expression = "ds := ds [calc S2_MAA1AT := if (not(isnull(S2_MAA2AT)) \n" +
                "and isnull(S2_MAA2ATC) and isnull(S2_MAA3AT) ) \n" +
                "then S2_MAA2AT else (if (not(isnull(S2_MAA2AT)) \n" +
                "and not(isnull(S2_MAA2ATC)) and isnull(S2_MAA3AT) ) \n" +
                "then (if ((cast(S2_MAA2AT,integer)<cast(S2_MAA2ATC,integer)) and MARRIVC=\"2\" ) \n" +
                "then S2_MAA2ATC else S2_MAA2AT) else (if (not(isnull(S2_MAA2AT)) and isnull(S2_MAA2ATC) \n" +
                "and not(isnull(S2_MAA3AT)) ) then (if ((cast(S2_MAA2AT,integer)<cast(S2_MAA3AT,integer)) \n" +
                "and MAA3=\"1\" ) then S2_MAA3AT else S2_MAA2AT) \n" +
                "else (if (not(isnull(S2_MAA2AT)) and not(isnull(S2_MAA2ATC)) \n" +
                "and not(isnull(S2_MAA3AT)) ) then (if ((cast(S2_MAA2AT,integer)<cast(S2_MAA3AT,integer)) \n" +
                "and MAA3=\"1\" ) then (if (cast(S2_MAA3AT,integer)<cast(S2_MAA2ATC,integer) \n" +
                "and MARRIVC=\"2\") then S2_MAA2ATC else S2_MAA3AT ) \n" +
                "else (if ((cast(S2_MAA2AT,integer)<cast(S2_MAA2ATC,integer)) \n" +
                "and MARRIVC=\"2\") then S2_MAA2ATC else S2_MAA2AT ) ) else \"\" )))\n" +
                "];";
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("vtl");
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

        //
        assertThrows(VtlScriptException.class, () -> engine.eval(expression));
        //
        //assertTrue(((Dataset) bindings.get("ds")).getDataStructure().containsKey("S2_MAA1AT"));
    }
}
