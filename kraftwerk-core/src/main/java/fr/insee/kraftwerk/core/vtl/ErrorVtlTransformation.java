package fr.insee.kraftwerk.core.vtl;

import lombok.Getter;
import lombok.Setter;

public class ErrorVtlTransformation {

    @Getter @Setter
    private String vtlScript;

    @Getter @Setter
    private String message;

    public ErrorVtlTransformation() {
    }

    public ErrorVtlTransformation(String vtlScript, String message) {
        this.vtlScript = vtlScript;
        this.message = message;
    }

    @Override
    public String toString() {
        return "VTL Transformation error detected on :" + "\n" +
                "Script='" + vtlScript + '\'' + "\n" +
                "Message='" + message + '\'' + "\n";
    }
}
