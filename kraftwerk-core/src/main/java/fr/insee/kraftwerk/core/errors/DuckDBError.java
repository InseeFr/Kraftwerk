package fr.insee.kraftwerk.core.errors;

import fr.insee.kraftwerk.core.KraftwerkError;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DuckDBError extends KraftwerkError {
    String errorMessage;

    @Override
    public String toString() {
        return errorMessage;
    }
}
