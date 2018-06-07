package no.nordicsemi.android.meshprovisioner.states;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.meshprovisioner.MeshProvisioningHandler;
import no.nordicsemi.android.meshprovisioner.MeshProvisioningStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

public class ProvisioningRandomConfirmation extends ProvisioningState {

    private final String TAG = ProvisioningRandomConfirmation.class.getSimpleName();
    private final UnprovisionedMeshNode mUnprovisionedMeshNode;
    private final MeshProvisioningStatusCallbacks mMeshProvisioningStatusCallbacks;
    private final MeshProvisioningHandler pduHandler;
    private final InternalTransportCallbacks mInternalTransportCallbacks;

    public ProvisioningRandomConfirmation(final MeshProvisioningHandler pduHandler, final UnprovisionedMeshNode unprovisionedMeshNode, final InternalTransportCallbacks mInternalTransportCallbacks, final MeshProvisioningStatusCallbacks meshProvisioningStatusCallbacks) {
        super();
        this.pduHandler = pduHandler;
        this.mUnprovisionedMeshNode = unprovisionedMeshNode;
        this.mInternalTransportCallbacks = mInternalTransportCallbacks;
        this.mMeshProvisioningStatusCallbacks = meshProvisioningStatusCallbacks;
    }

    @Override
    public State getState() {
        return State.PROVISINING_RANDOM;
    }

    @Override
    public void executeSend() {
        final byte[] provisionerRandomConfirmationPDU = createProvisionerRandomPDU();
        mMeshProvisioningStatusCallbacks.onProvisioningRandomSent(mUnprovisionedMeshNode);
        mInternalTransportCallbacks.sendPdu(mUnprovisionedMeshNode, provisionerRandomConfirmationPDU);
    }

    @Override
    public boolean parseData(final byte[] data) {
        mMeshProvisioningStatusCallbacks.onProvisioningRandomReceived(mUnprovisionedMeshNode);
        parseProvisioneeRandom(data);
        return provisioneeMatches();
    }

    private byte[] createProvisionerRandomPDU() {
        final byte[] provisionerRandom = mUnprovisionedMeshNode.getProvisionerRandom();
        final ByteBuffer buffer = ByteBuffer.allocate(provisionerRandom.length + 2);
        buffer.put(new byte[]{MeshManagerApi.PDU_TYPE_PROVISIONING, TYPE_PROVISIONING_RANDOM_CONFIRMATION});
        buffer.put(provisionerRandom);
        final byte[] data = buffer.array();
        Log.v(TAG, "Provisioner random PDU: " + MeshParserUtils.bytesToHex(data, false));
        return data;
    }

    private boolean provisioneeMatches() {
        final byte[] provisioneeRandom = mUnprovisionedMeshNode.getProvisioneeRandom();

        final byte[] confirmationInputs = pduHandler.generateConfirmationInputs();
        Log.v(TAG, "Confirmation inputs: " + MeshParserUtils.bytesToHex(confirmationInputs, false));

        //Generate a confirmation salt of the confirmation inputs
        final byte[] confirmationSalt = SecureUtils.calculateSalt(confirmationInputs);
        Log.v(TAG, "Confirmation salt: " + MeshParserUtils.bytesToHex(confirmationSalt, false));

        final byte[] ecdhSecret = mUnprovisionedMeshNode.getSharedECDHSecret();

        //Generate the confirmationKey by calculating the K1 of ECDH, confirmationSalt and ASCII value of "prck".
        final byte[] confirmationKey = SecureUtils.calculateK1(ecdhSecret, confirmationSalt, SecureUtils.PRCK);
        Log.v(TAG, "Confirmation key: " + MeshParserUtils.bytesToHex(confirmationKey, false));

        //Generate authentication value from the user input pin
        final byte[] authenticationValue = mUnprovisionedMeshNode.getAuthenticationValue();
        Log.v(TAG, "Authentication value: " + MeshParserUtils.bytesToHex(authenticationValue, false));

        ByteBuffer buffer = ByteBuffer.allocate(provisioneeRandom.length + authenticationValue.length);
        buffer.put(provisioneeRandom);
        buffer.put(authenticationValue);
        final byte[] confirmationData = buffer.array();

        final byte[] confirmationValue = SecureUtils.calculateCMAC(confirmationData, confirmationKey);

        if (Arrays.equals(confirmationValue, mUnprovisionedMeshNode.getProvisioneeConfirmation())) {
            Log.v(TAG, "Confirmation values match!!!!: " + MeshParserUtils.bytesToHex(confirmationValue, false));
            return true;
        }

        return false;
    }

    private void parseProvisioneeRandom(final byte[] provisioneeRandomPDU) {
        final ByteBuffer buffer = ByteBuffer.allocate(provisioneeRandomPDU.length - 2);
        buffer.put(provisioneeRandomPDU, 2, buffer.limit());
        mUnprovisionedMeshNode.setProvisioneeRandom(buffer.array());
    }
}
