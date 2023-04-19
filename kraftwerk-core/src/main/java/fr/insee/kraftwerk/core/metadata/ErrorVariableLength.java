package fr.insee.kraftwerk.core.metadata;

import fr.insee.kraftwerk.core.KraftwerkError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@AllArgsConstructor
public class ErrorVariableLength extends KraftwerkError {

    @Getter
    @Setter
    private Variable variable;

    @Getter
    @Setter
    private String dataMode;

    @Override
    public String toString() {
        return  String.format("Warning : The maximum length read for variable %s (DataMode: %s) exceed expected length",variable.getName(),dataMode) + "\n" +
                String.format("Expected: %s but received: %d",variable.getExpectedLength(),variable.getMaxLengthData()) + "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorVariableLength that = (ErrorVariableLength) o;
        return Objects.equals(variable.getName(), that.variable.getName()) && Objects.equals(dataMode, that.dataMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable, dataMode);
    }
}
