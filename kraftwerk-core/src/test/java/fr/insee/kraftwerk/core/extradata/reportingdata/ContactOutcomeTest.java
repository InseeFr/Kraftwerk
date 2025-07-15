package fr.insee.kraftwerk.core.extradata.reportingdata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ContactOutcomeTest {

    @Test
    void interrogationId_null_Test() {
        ContactOutcome contactOutcome = new ContactOutcome();
        assertNull(contactOutcome.getOutcomeType());
        assertEquals(0L, contactOutcome.getDateEndContact());
        assertEquals(0, contactOutcome.getTotalNumberOfContactAttempts());
    }

    @Test
    void interrogationId_Test() {
        ContactOutcome contactOutcome = new ContactOutcome("abc", 123, 456L);
        assertEquals("abc", contactOutcome.getOutcomeType());
        assertEquals(456, contactOutcome.getDateEndContact());
        assertEquals(123, contactOutcome.getTotalNumberOfContactAttempts());
    }

}
