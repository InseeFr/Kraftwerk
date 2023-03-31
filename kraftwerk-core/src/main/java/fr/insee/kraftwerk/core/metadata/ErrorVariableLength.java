package fr.insee.kraftwerk.core.metadata;

import fr.insee.kraftwerk.core.KraftwerkError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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

}
