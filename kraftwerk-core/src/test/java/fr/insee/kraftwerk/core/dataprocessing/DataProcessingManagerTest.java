package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.parsers.DataFormat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataProcessingManagerTest {

    @Test
    void getProcessingClassXFORMS_test() {
        UnimodalDataProcessing udp = DataProcessingManager.getProcessingClass(DataFormat.XFORMS, null, null, null);
        assertInstanceOf(XformsDataProcessing.class, udp);
    }

    @Test
    void getProcessingClassPAPER_test() {
        UnimodalDataProcessing udp = DataProcessingManager.getProcessingClass(DataFormat.PAPER, null, null, null);
        assertInstanceOf(PaperDataProcessing.class, udp);
    }

    @Test
    void getProcessingClassLUNATIC_XML_test() {
        UnimodalDataProcessing udp = DataProcessingManager.getProcessingClass(DataFormat.LUNATIC_XML, null, null, null);
        assertInstanceOf(LunaticDataProcessing.class, udp);
    }

    @Test
    void getProcessingClassLUNATIC_JSON_test() {
        UnimodalDataProcessing udp = DataProcessingManager.getProcessingClass(DataFormat.LUNATIC_JSON, null, null, null);
        assertInstanceOf(LunaticDataProcessing.class, udp);
    }

}
