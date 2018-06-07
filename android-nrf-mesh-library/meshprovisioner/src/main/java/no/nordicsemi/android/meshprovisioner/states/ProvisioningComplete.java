package no.nordicsemi.android.meshprovisioner.states;

public class ProvisioningComplete extends ProvisioningState {

    private final UnprovisionedMeshNode unprovisionedMeshNode;

    public ProvisioningComplete(final UnprovisionedMeshNode unprovisionedMeshNode) {
        super();
        this.unprovisionedMeshNode = unprovisionedMeshNode;
        unprovisionedMeshNode.setIsProvisioned(true);
        unprovisionedMeshNode.setProvisionedTime(System.currentTimeMillis());
    }

    @Override
    public State getState() {
        return State.PROVISINING_COMPLETE;
    }

    @Override
    public void executeSend() {

    }

    @Override
    public boolean parseData(final byte[] data) {
        return true;
    }

}
