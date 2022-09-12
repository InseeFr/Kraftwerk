package fr.insee.kraftwerk.core.vtl;

import java.util.ArrayList;
import java.util.Arrays;

public class VtlScript extends ArrayList<String> {

    private static final long serialVersionUID = -1324236008014518483L;

	public VtlScript() {
        super();
    }

    public VtlScript(String firstInstruction) {
        super();
        this.add(firstInstruction);
    }

    public VtlScript(String firstInstruction, String... instructions) {
        super();
        this.add(firstInstruction);
        this.addAll(Arrays.asList(instructions));
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        for(String instruction : this) {
            text.append(instruction).append("\n");
        }
        return text.toString();
    }
}
