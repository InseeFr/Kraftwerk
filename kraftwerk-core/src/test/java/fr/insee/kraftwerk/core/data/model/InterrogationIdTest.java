package fr.insee.kraftwerk.core.data.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InterrogationIdTest {

    @Test
    void interrogationId_Test() {
        InterrogationId interrogationId = new InterrogationId();
        interrogationId.setId("xyz");
        assertEquals("xyz", interrogationId.getId());
    }

}
