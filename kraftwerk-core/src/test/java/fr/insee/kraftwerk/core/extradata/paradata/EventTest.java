package fr.insee.kraftwerk.core.extradata.paradata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    void event_constructor() {
        Event event = new Event("aaa", "bbb");
        assertEquals("aaa", event.getIdSurveyUnit());
        assertEquals("bbb", event.getIdSession());
    }

}
