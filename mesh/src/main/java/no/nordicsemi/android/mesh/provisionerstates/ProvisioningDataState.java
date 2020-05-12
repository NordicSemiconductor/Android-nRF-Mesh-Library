/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.mesh.provisionerstates;

import android.util.Log;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.InternalProvisioningCallbacks;
import no.nordicsemi.android.mesh.InternalTransportCallbacks;
import no.nordicsemi.android.mesh.MeshManagerApi;
import no.nordicsemi.android.mesh.MeshProvisioningStatusCallbacks;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.mesh.utils.SecureUtils;

public class ProvisioningDataState extends ProvisioningState {

    private final String TAG = ProvisioningDataState.class.getSimpleName();
    private final UnprovisionedMeshNode mUnprovisionedMeshNode;
    private final MeshProvisioningStatusCallbacks mStatusCallbacks;
    private final InternalProvisioningCallbacks provisioningCallbacks;
    private final InternalTransportCallbacks mInternalTransportCallbacks;

    public ProvisioningDataState(@NonNull final InternalProvisioningCallbacks callbacks,
                                 @NonNull final UnprovisionedMeshNode unprovisionedMeshNode,
                                 @NonNull final InternalTransportCallbacks mInternalTransportCallbacks,
                                 @NonNull final MeshProvisioningStatusCallbacks meshProvisioningStatusCallbacks) {
        super();
        this.provisioningCallbacks = callbacks;
        this.mUnprovisionedMeshNode = unprovisionedMeshNode;
        this.mInternalTransportCallbacks = mInternalTransportCallbacks;
        this.mStatusCallbacks = meshProvisioningStatusCallbacks;
    }

    @Override
    public State getState() {
        return State.PROVISIONING_DATA;
    }

    @Override
    public void executeSend() {
        sendProvisioningData();
    }

    @Override
    public boolean parseData(@NonNull final byte[] data) {
        return true;
    }

    private void sendProvisioningData() {
        final byte[] provisioningDataPDU = createProvisioningDataPDU();
        mStatusCallbacks.onProvisioningStateChanged(mUnprovisionedMeshNode, States.PROVISIONING_DATA_SENT, provisioningDataPDU);
        mInternalTransportCallbacks.sendProvisioningPdu(mUnprovisionedMeshNode, provisioningDataPDU);
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
        final byte[] keyIndex = MeshParserUtils.addKeyIndexPadding(mUnprovisionedMeshNode.getKeyIndex());
        Log.v(TAG, "Key index: " + MeshParserUtils.bytesToHex(keyIndex, false));

        /* Generate random 1 byte Flags */
        byte[] flags = mUnprovisionedMeshNode.getFlags();
        Log.v(TAG, "Flags: " + MeshParserUtils.bytesToHex(flags, false));

        /* Generate random 4 byte IV Index */
        final byte[] ivIndex = mUnprovisionedMeshNode.getIvIndex();
        Log.v(TAG, "IV index: " + MeshParserUtils.bytesToHex(ivIndex, false));

        /* Generate random 2 byte unicast address*/
        final byte[] unicastAddress = MeshAddress.addressIntToBytes(mUnprovisionedMeshNode.getUnicastAddress());

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

        final byte[] confirmationSalt = SecureUtils.calculateSalt(provisioningCallbacks.generateConfirmationInputs(mUnprovisionedMeshNode.getProvisionerPublicKeyXY(), mUnprovisionedMeshNode.getProvisioneePublicKeyXY()));
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
