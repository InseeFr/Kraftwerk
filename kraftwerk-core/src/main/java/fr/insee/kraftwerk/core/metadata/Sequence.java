package fr.insee.kraftwerk.core.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Sequence {

    String name;

    public Sequence(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sequence sequence = (Sequence) o;
        return Objects.equals(name, sequence.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
