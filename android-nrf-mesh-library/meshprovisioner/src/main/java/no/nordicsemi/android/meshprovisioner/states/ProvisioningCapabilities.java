package no.nordicsemi.android.meshprovisioner.states;

import android.util.Log;

import no.nordicsemi.android.meshprovisioner.MeshProvisioningStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.utils.ParseInputOOBActions;
import no.nordicsemi.android.meshprovisioner.utils.ParseOutputOOBActions;
import no.nordicsemi.android.meshprovisioner.utils.ParsePublicKeyInformation;
import no.nordicsemi.android.meshprovisioner.utils.ParseStaticOutputOOBInformation;

public class ProvisioningCapabilities extends ProvisioningState {
    private static final String TAG = ProvisioningInvite.class.getSimpleName();

    private final UnprovisionedMeshNode mUnprovisionedMeshNode;
    private final MeshProvisioningStatusCallbacks mCallbacks;

    private int numberOfElements;
    private int algorithm;
    private int publicKeyType;
    private int staticOOBType;
    private int outputOOBSize;
    private int outputOOBAction;
    private int inputOOBSize;
    private int inputOOBAction;

    public ProvisioningCapabilities(final UnprovisionedMeshNode unprovisionedMeshNode, final MeshProvisioningStatusCallbacks callbacks) {
        super();
        this.mCallbacks = callbacks;
        this.mUnprovisionedMeshNode = unprovisionedMeshNode;
    }

    @Override
    public State getState() {
        return State.PROVISIONING_CAPABILITIES;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public int getAlgorithm() {
        return algorithm;
    }

    public int getPublicKeyType() {
        return publicKeyType;
    }

    public int getStaticOOBType() {
        return staticOOBType;
    }

    public int getOutputOOBSize() {
        return outputOOBSize;
    }

    public int getOutputOOBAction() {
        return outputOOBAction;
    }

    public int getInputOOBSize() {
        return inputOOBSize;
    }

    public int getInputOOBAction() {
        return inputOOBAction;
    }

    @Override
    public void executeSend() {

    }

    @Override
    public boolean parseData(final byte[] data) {
        mCallbacks.onProvisioningCapabilitiesReceived(mUnprovisionedMeshNode);
        return parseProvisioningCapabilities(data);
    }

    private boolean parseProvisioningCapabilities(final byte[] provisioningCapabilities) {

        if (provisioningCapabilities[2] == 0)
            throw new IllegalArgumentException("Number of elements cannot be zero");

        numberOfElements = (provisioningCapabilities[2]);
        algorithm = (((provisioningCapabilities[3] & 0xff) << 8) | (provisioningCapabilities[4] & 0xff));
        publicKeyType = (provisioningCapabilities[5]); // 0 is unavailable and 1 is available
        staticOOBType = (provisioningCapabilities[6]); // 0 is unavailable and 1 is available
        outputOOBSize = (provisioningCapabilities[7]);
        outputOOBAction = (((provisioningCapabilities[8] & 0xff) << 8) | (provisioningCapabilities[9] & 0xff));
        inputOOBSize = (provisioningCapabilities[10]);
        inputOOBAction = (((provisioningCapabilities[11] & 0xff) << 8) | (provisioningCapabilities[12] & 0xff));

        Log.v(TAG, "Number of elements: " + numberOfElements);
        Log.v(TAG, "Algorithm: " + algorithm);
        Log.v(TAG, "Public key type: " + ParsePublicKeyInformation.getPublicKeyInformation(publicKeyType));
        Log.v(TAG, "Static OOB type: " + ParseStaticOutputOOBInformation.getStaticOOBActionInformationAvailability(staticOOBType));
        Log.v(TAG, "Output OOB size: " + outputOOBSize);
        Log.v(TAG, "Output OOB action: " + ParseOutputOOBActions.getOuputOOBActionDescription(outputOOBAction));
        Log.v(TAG, "Input OOB size: " + inputOOBSize);
        Log.v(TAG, "Input OOB action: " + ParseInputOOBActions.getInputOOBActionDescription(inputOOBAction));

        return true;
    }
}
