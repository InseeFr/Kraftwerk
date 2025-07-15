package fr.insee.kraftwerk.core;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConstantsTest {

    @Test
    void getInputPath_3params_null_Test() {
        String userPath = null;
        assertNull(Constants.getInputPath(null, null, userPath));
    }

    @Test
    void getInputPath_3params_Test() {
        assertEquals("aaa/bbb/ccc", Constants.getInputPath("aaa", "bbb", "ccc"));
    }

    @Test
    void getInputPath_2params_null_Test() {
        String userPath = null;
        assertNull(Constants.getInputPath(null, null));
    }

    @Test
    void getInputPath_2params_Test() {
        assertEquals(Path.of("aaa" + File.separator + "bbb"), Constants.getInputPath(Path.of("aaa"), "bbb"));
    }

    @Test
    void getCsvOutputQuoteChar_unchanged_Test() {
        assertEquals('"', Constants.getCsvOutputQuoteChar());
    }

    @Test
    void getCsvOutputQuoteChar_changed_Test() {
        Constants.setCsvOutputQuoteChar('¤');
        assertEquals('¤', Constants.getCsvOutputQuoteChar());
    }

    @Test
    void getEnoVariables_Test() {
        assertEquals(4, Constants.getEnoVariables().length);
        assertEquals("COMMENT_QE", Constants.getEnoVariables()[0]);
        assertEquals("COMMENT_UE", Constants.getEnoVariables()[1]);
        assertEquals("HEURE_REMPL", Constants.getEnoVariables()[2]);
        assertEquals("MIN_REMPL", Constants.getEnoVariables()[3]);
    }

}
