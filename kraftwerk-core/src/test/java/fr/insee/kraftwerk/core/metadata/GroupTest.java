package fr.insee.kraftwerk.core.metadata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

 class GroupTest {

    @Test
   // @SuppressWarnings({"null", "ConstantConditions"})
    void nullParentName() {
        assertThrows(NullPointerException.class, () -> new Group("TEST", null));
    }

    @Test
    void emptyParentName() {
        assertThrows(IllegalArgumentException.class, () -> new Group("TEST", ""));
    }
}
