package fr.insee.kraftwerk.core.metadata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class GroupTest {

    @Test
    @SuppressWarnings({"null", "ConstantConditions"})
    public void nullParentName() {
        assertThrows(NullPointerException.class, () -> new Group("TEST", null));
    }

    @Test
    public void emptyParentName() {
        assertThrows(IllegalArgumentException.class, () -> new Group("TEST", ""));
    }
}
