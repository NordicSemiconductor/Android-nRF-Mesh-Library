package no.nordicsemi.android.meshprovisioner.states;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.meshprovisioner.MeshProvisioningStatusCallbacks;

public class ProvisioningInvite extends ProvisioningState {

    private final String TAG = ProvisioningInvite.class.getSimpleName();
    private final UnprovisionedMeshNode mUnprovisionedMeshNode;
    private final int attentionTimer;
    private final MeshProvisioningStatusCallbacks mMeshProvisioningStatusCallbacks;
    private final InternalTransportCallbacks mInternalTransportCallbacks;

    public ProvisioningInvite(final UnprovisionedMeshNode unprovisionedMeshNode, final int attentionTimer, final InternalTransportCallbacks mInternalTransportCallbacks, final MeshProvisioningStatusCallbacks meshProvisioningStatusCallbacks) {
        super();
        this.mUnprovisionedMeshNode = unprovisionedMeshNode;
        this.attentionTimer = attentionTimer;
        this.mMeshProvisioningStatusCallbacks = meshProvisioningStatusCallbacks;
        this.mInternalTransportCallbacks = mInternalTransportCallbacks;
    }

    @Override
    public State getState() {
        return State.PROVISIONING_INVITE;
    }

    @Override
    public void executeSend() {
        final byte[] invitePDU = createInvitePDU();
        mMeshProvisioningStatusCallbacks.onProvisioningInviteSent(mUnprovisionedMeshNode);
        mInternalTransportCallbacks.sendPdu(mUnprovisionedMeshNode, invitePDU);
    }

    @Override
    public boolean parseData(final byte[] data) {
        return true;
    }

    /**
     * Generates the invitePDU for provisioning based on the attention timer provided by the user.
     */
    private byte[] createInvitePDU() {

        final byte[] data = new byte[3];
        data[0] = MeshManagerApi.PDU_TYPE_PROVISIONING; //Provisioning Opcode;
        data[1] = TYPE_PROVISIONING_INVITE; //PDU type in
        data[2] = (byte) attentionTimer;
        return data;
    }
}
