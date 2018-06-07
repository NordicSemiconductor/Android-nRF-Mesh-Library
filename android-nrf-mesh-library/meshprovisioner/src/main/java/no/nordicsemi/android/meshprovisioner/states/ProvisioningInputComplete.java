package no.nordicsemi.android.meshprovisioner.states;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.meshprovisioner.MeshProvisioningStatusCallbacks;

public class ProvisioningInputComplete extends ProvisioningState {

    private final UnprovisionedMeshNode mUnprovisionedMeshNode;
    private final InternalTransportCallbacks mInternalTransportCallbacks;
    private final MeshProvisioningStatusCallbacks mMeshProvisioningStatusCallbacks;


    public ProvisioningInputComplete(final UnprovisionedMeshNode unprovisionedMeshNode, final InternalTransportCallbacks mInternalTransportCallbacks, final MeshProvisioningStatusCallbacks meshProvisioningStatusCallbacks) {
        super();
        this.mUnprovisionedMeshNode = unprovisionedMeshNode;
        this.mInternalTransportCallbacks = mInternalTransportCallbacks;
        this.mMeshProvisioningStatusCallbacks = meshProvisioningStatusCallbacks;
    }

    @Override
    public State getState() {
        return State.PROVISINING_INPUT_COMPLETE;
    }

    @Override
    public void executeSend() {
        mMeshProvisioningStatusCallbacks.onProvisioningInputCompleteSent(mUnprovisionedMeshNode);
        mInternalTransportCallbacks.sendPdu(mUnprovisionedMeshNode, createProvisioningInputComplete());
    }

    @Override
    public boolean parseData(final byte[] data) {
        return true;
    }

    private byte[] createProvisioningInputComplete() {
        final byte[] provisioningPDU = new byte[2];
        provisioningPDU[0] = MeshManagerApi.PDU_TYPE_PROVISIONING;
        provisioningPDU[1] = TYPE_PROVISIONING_INPUT_COMPLETE;
        return provisioningPDU;
    }

}
