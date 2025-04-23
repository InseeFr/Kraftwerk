package stubs;

public class VaultContextStubForTests implements VaultContext {

    @Override
    public String getVaultPath() {
        return "TESTVAULTPATH";
    }

    @Override
    public Object getVaultCaller() {
        return null;
    }
}
