package stubs;

import fr.insee.kraftwerk.core.utils.VaultContext;

public class VaultContextStub extends VaultContext {
    public VaultContextStub() {
        super(new VaultCallerStub(), "TESTVAULTPATH");
    }
}
