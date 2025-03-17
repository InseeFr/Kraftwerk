package fr.insee.kraftwerk.core.extradata.reportingdata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ContactOutcomeTypeTest {


        @BeforeEach
        void setUp() {
            // Initialisation du mapping avant les tests
            ContactOutcomeType.getOutcomeType("INA"); // Force l'initialisation
        }

        @Test
        void testGetOutcomeType_invalidKey() {
            assertNull(ContactOutcomeType.getOutcomeType("UNKNOWN"), "Une clé inconnue doit retourner null");
        }

        @Test
        void testAllEnumValuesAreMapped() {
            for (ContactOutcomeType type : ContactOutcomeType.values()) {
                assertNotNull(ContactOutcomeType.getOutcomeType(type.getKey()), "La clé " + type.name() + " doit être dans le mapping");
            }
        }
}


