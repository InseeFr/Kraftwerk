package fr.insee.kraftwerk.core.extradata.reportingdata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class StateTypeTest {


        @BeforeEach
        void setUp() {
            // Initialisation du mapping avant les tests
            StateType.getStateType("NVM"); // Force l'initialisation
        }

        @Test
        void testGetStateType_invalidKey() {
            assertNull(StateType.getStateType("UNKNOWN"), "Une clé inconnue doit retourner null");
        }

        @Test
        void testAllEnumValuesAreMapped() {
            for (StateType type : StateType.values()) {
                assertNotNull(StateType.getStateType(type.getKey()), "La clé " + type.name() + " doit être dans le mapping");
            }
        }
}


