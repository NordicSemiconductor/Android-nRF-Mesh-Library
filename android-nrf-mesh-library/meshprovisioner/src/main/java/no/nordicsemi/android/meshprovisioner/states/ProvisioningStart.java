package no.nordicsemi.android.meshprovisioner.states;

import android.util.Log;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.meshprovisioner.MeshProvisioningStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.ParseInputOOBActions;
import no.nordicsemi.android.meshprovisioner.utils.ParseOutputOOBActions;
import no.nordicsemi.android.meshprovisioner.utils.ParseProvisioningAlgorithm;

public class ProvisioningStart extends ProvisioningState {

    private final String TAG = ProvisioningStart.class.getSimpleName();
    private final UnprovisionedMeshNode mUnprovisionedMeshNode;
    private final MeshProvisioningStatusCallbacks mMeshProvisioningStatusCallbacks;
    private final InternalTransportCallbacks mInternalTransportCallbacks;

    private int numberOfElements;
    private int algorithm;
    private int publicKeyType;
    private int staticOOBType;
    private int outputOOBSize;
    private int outputOOBAction;
    private int inputOOBSize;
    private int inputOOBAction;

    public ProvisioningStart(final UnprovisionedMeshNode unprovisionedMeshNode, final InternalTransportCallbacks mInternalTransportCallbacks, final MeshProvisioningStatusCallbacks meshProvisioningStatusCallbacks) {
        super();
        this.mUnprovisionedMeshNode = unprovisionedMeshNode;
        this.mInternalTransportCallbacks = mInternalTransportCallbacks;
        this.mMeshProvisioningStatusCallbacks = meshProvisioningStatusCallbacks;
    }

    @Override
    public State getState() {
        return State.PROVISIONING_START;
    }

    @Override
    public void executeSend() {
        final byte[] provisioningStartPDU = createProvisioningStartPDU();
        mMeshProvisioningStatusCallbacks.onProvisioningStartSent(mUnprovisionedMeshNode);
        mInternalTransportCallbacks.sendPdu(mUnprovisionedMeshNode, provisioningStartPDU);
    }

    @Override
    public boolean parseData(final byte[] data) {
        return true;
    }

    private byte[] createProvisioningStartPDU() {
        final byte[] provisioningPDU = new byte[7];
        provisioningPDU[0] = MeshManagerApi.PDU_TYPE_PROVISIONING;
        provisioningPDU[1] = TYPE_PROVISIONING_START;
        provisioningPDU[2] = ParseProvisioningAlgorithm.getAlgorithmValue(algorithm);
        provisioningPDU[3] = 0;//(byte) publicKeyType;
        provisioningPDU[4] = getAuthenticationMethod(); //So far its Output OOB
        provisioningPDU[5] = (byte) ParseOutputOOBActions.getOuputOOBActionValue(outputOOBAction);
        provisioningPDU[6] = (byte) outputOOBSize;
        Log.v(TAG, "Provisioning start PDU: " + MeshParserUtils.bytesToHex(provisioningPDU, true));

        return provisioningPDU;
    }

    private byte getAuthenticationMethod() {
        if (ParseOutputOOBActions.parseOuputOOBActionValue(outputOOBAction) == 0 && ParseInputOOBActions.parseInputOOBActionValue(inputOOBAction) > 0) {
            return 3;
        } else if (ParseOutputOOBActions.parseOuputOOBActionValue(outputOOBAction) > 0 && ParseInputOOBActions.parseInputOOBActionValue(inputOOBAction) == 0) {
            return 2;
        } else {
            return 0;
        }
    }

    public void setProvisioningCapabilities(final int numberOfElements, final int algorithm, final int publicKeyType, final int staticOOBType, final int outputOOBSize, final int outputOOBAction, final int inputOOBSize, final int inputOOBAction) {
        this.numberOfElements = numberOfElements;
        this.algorithm = algorithm;
        this.publicKeyType = publicKeyType;
        this.staticOOBType = staticOOBType;
        this.outputOOBSize = outputOOBSize;
        this.outputOOBAction = outputOOBAction;
        this.inputOOBSize = inputOOBSize;
        this.inputOOBAction = inputOOBAction;
    }
}
