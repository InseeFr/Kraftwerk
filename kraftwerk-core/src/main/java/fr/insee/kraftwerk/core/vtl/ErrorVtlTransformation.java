package fr.insee.kraftwerk.core.vtl;

import fr.insee.kraftwerk.core.KraftwerkError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
@AllArgsConstructor
public class ErrorVtlTransformation extends KraftwerkError {

    private String vtlScript;

    private String message;


    @Override
    public String toString() {
        return "VTL Transformation error detected on :" + "\n" +
                "Script='" + vtlScript + '\'' + "\n" +
                "Message='" + message + '\'' + "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorVtlTransformation that = (ErrorVtlTransformation) o;
        return Objects.equals(vtlScript, that.vtlScript) && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vtlScript, message);
    }
}
