package no.nordicsemi.android.meshprovisioner.states;

import android.util.Log;

import java.nio.ByteBuffer;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.meshprovisioner.MeshProvisioningHandler;
import no.nordicsemi.android.meshprovisioner.MeshProvisioningStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

public class ProvisioningData extends ProvisioningState {

    private final String TAG = ProvisioningRandomConfirmation.class.getSimpleName();
    private final UnprovisionedMeshNode mUnprovisionedMeshNode;
    private final MeshProvisioningStatusCallbacks mMeshProvisioningStatusCallbacks;
    private final MeshProvisioningHandler pduHandler;
    private final InternalTransportCallbacks mInternalTransportCallbacks;

    public ProvisioningData(final MeshProvisioningHandler pduHandler, final UnprovisionedMeshNode unprovisionedMeshNode, final InternalTransportCallbacks mInternalTransportCallbacks, final MeshProvisioningStatusCallbacks meshProvisioningStatusCallbacks) {
        super();
        this.pduHandler = pduHandler;
        this.mUnprovisionedMeshNode = unprovisionedMeshNode;
        this.mInternalTransportCallbacks = mInternalTransportCallbacks;
        this.mMeshProvisioningStatusCallbacks = meshProvisioningStatusCallbacks;
    }

    @Override
    public State getState() {
        return State.PROVISINING_DATA;
    }

    @Override
    public void executeSend() {
        sendProvisioningData();
    }

    @Override
    public boolean parseData(final byte[] data) {
        return true;
    }

    private void sendProvisioningData() {
        final byte[] provisioningDataPDU = createProvisioningDataPDU();
        mMeshProvisioningStatusCallbacks.onProvisioningDataSent(mUnprovisionedMeshNode);
        mInternalTransportCallbacks.sendPdu(mUnprovisionedMeshNode, provisioningDataPDU);
    }

    private byte[] createProvisioningDataPDU() {

        final byte[] provisioningSalt = generateProvisioningSalt();
        Log.v(TAG, "Provisioning salt: " + MeshParserUtils.bytesToHex(provisioningSalt, false));

        final byte[] ecdh = mUnprovisionedMeshNode.getSharedECDHSecret();

        final byte[] t = SecureUtils.calculateCMAC(ecdh, provisioningSalt);
        /* Calculating the session key */
        final byte[] sessionKey = SecureUtils.calculateCMAC(SecureUtils.PRSK, t);
        Log.v(TAG, "Session key: " + MeshParserUtils.bytesToHex(sessionKey, false));

        /* Calculate the Session nonce */
        final byte[] sessionNonce = generateSessionNonce(ecdh, provisioningSalt);
        Log.v(TAG, "Session nonce: " + MeshParserUtils.bytesToHex(sessionNonce, false));

        /* Calculate the Device key */
        final byte[] deviceKey = SecureUtils.calculateCMAC(SecureUtils.PRDK, t);
        Log.v(TAG, "Device key: " + MeshParserUtils.bytesToHex(deviceKey, false));
        mUnprovisionedMeshNode.setDeviceKey(deviceKey);

        /* Generate 16 byte Random network key */
        final byte[] networkKey = mUnprovisionedMeshNode.getNetworkKey();
        Log.v(TAG, "Network key: " + MeshParserUtils.bytesToHex(networkKey, false));

        /* Generate random 2 byte Key index*/
        final byte[] keyIndex = mUnprovisionedMeshNode.getKeyIndex();
        Log.v(TAG, "Key index: " + MeshParserUtils.bytesToHex(keyIndex, false));

        /* Generate random 1 byte Flags */
        byte[] flags = mUnprovisionedMeshNode.getFlags();
        Log.v(TAG, "Flags: " + MeshParserUtils.bytesToHex(flags, false));

        /* Generate random 4 byte IV Index */
        final byte[] ivIndex = mUnprovisionedMeshNode.getIvIndex();
        Log.v(TAG, "IV index: " + MeshParserUtils.bytesToHex(ivIndex, false));

        /* Generate random 2 byte unicast address*/
        final byte[] unicastAddress = mUnprovisionedMeshNode.getUnicastAddress();

        Log.v(TAG, "Unicast address: " + MeshParserUtils.bytesToHex(unicastAddress, false));
        ByteBuffer buffer = ByteBuffer.allocate(networkKey.length + keyIndex.length + flags.length + ivIndex.length + unicastAddress.length);
        buffer.put(networkKey);
        buffer.put(keyIndex);
        buffer.put(flags);
        buffer.put(ivIndex);
        buffer.put(unicastAddress);

        final byte[] provisioningData = buffer.array();
        Log.v(TAG, "Provisioning data: " + MeshParserUtils.bytesToHex(provisioningData, false));

        final byte[] encryptedProvisioningData = SecureUtils.encryptCCM(provisioningData, sessionKey, sessionNonce, 8);
        Log.v(TAG, "Encrypted provisioning data: " + MeshParserUtils.bytesToHex(encryptedProvisioningData, false));

        buffer = ByteBuffer.allocate(2 + encryptedProvisioningData.length);
        buffer.put(MeshManagerApi.PDU_TYPE_PROVISIONING);
        buffer.put(TYPE_PROVISIONING_DATA);
        buffer.put(encryptedProvisioningData);

        final byte[] provisioningPDU = buffer.array();
        Log.v(TAG, "Prov Data: " + MeshParserUtils.bytesToHex(provisioningPDU, false));
        return provisioningPDU;
    }

    /**
     * Generate the provisioning salt.
     * This is done by calculating the salt containing array created by appending the confirmationSalt, provisionerRandom and the provisioneeRandom.
     *
     * @return a byte array
     */
    private byte[] generateProvisioningSalt() {

        final byte[] confirmationSalt = SecureUtils.calculateSalt(pduHandler.generateConfirmationInputs());
        final byte[] provisionerRandom = mUnprovisionedMeshNode.getProvisionerRandom();
        final byte[] provisioneeRandom = mUnprovisionedMeshNode.getProvisioneeRandom();

        final ByteBuffer buffer = ByteBuffer.allocate(confirmationSalt.length + provisionerRandom.length + provisioneeRandom.length);
        buffer.put(confirmationSalt);
        buffer.put(provisionerRandom);
        buffer.put(provisioneeRandom);

        /* After appending calculate the salt */
        return SecureUtils.calculateSalt(buffer.array());
    }

    /**
     * Calculate the Session nonce
     *
     * @param ecdh             shared ECDH secret
     * @param provisioningSalt provisioning salt
     * @return sessionNonce
     */
    private byte[] generateSessionNonce(final byte[] ecdh, final byte[] provisioningSalt) {
        final byte[] nonce = SecureUtils.calculateK1(ecdh, provisioningSalt, SecureUtils.PRSN);
        final ByteBuffer buffer = ByteBuffer.allocate(nonce.length - 3);
        buffer.put(nonce, 3, buffer.limit());
        return buffer.array();
    }
}
