package fr.insee.kraftwerk.core.encryption;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationContextProviderTest {

    @Test
    void applicationContextProvider_getterSetter_test() {
        ApplicationContext currentContextTest = new GenericXmlApplicationContext();
        ApplicationContextProvider acp = new ApplicationContextProvider();
        acp.setApplicationContext(currentContextTest);
        assertEquals(currentContextTest, ApplicationContextProvider.getApplicationContext());
    }

}
