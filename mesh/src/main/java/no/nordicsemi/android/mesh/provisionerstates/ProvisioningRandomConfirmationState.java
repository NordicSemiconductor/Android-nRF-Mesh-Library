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
import java.util.Arrays;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.InternalProvisioningCallbacks;
import no.nordicsemi.android.mesh.InternalTransportCallbacks;
import no.nordicsemi.android.mesh.MeshManagerApi;
import no.nordicsemi.android.mesh.MeshProvisioningStatusCallbacks;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.mesh.utils.SecureUtils;

public class ProvisioningRandomConfirmationState extends ProvisioningState {

    private final String TAG = ProvisioningRandomConfirmationState.class.getSimpleName();
    private final UnprovisionedMeshNode mUnprovisionedMeshNode;
    private final MeshProvisioningStatusCallbacks mStatusCallbacks;
    private final InternalProvisioningCallbacks provisioningCallbacks;
    private final InternalTransportCallbacks mInternalTransportCallbacks;

    public ProvisioningRandomConfirmationState(@NonNull final InternalProvisioningCallbacks callbacks,
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
        return State.PROVISIONING_RANDOM;
    }

    @Override
    public void executeSend() {
        final byte[] provisionerRandomConfirmationPDU = createProvisionerRandomPDU();
        mStatusCallbacks.onProvisioningStateChanged(mUnprovisionedMeshNode, States.PROVISIONING_CONFIRMATION_SENT, provisionerRandomConfirmationPDU);
        mInternalTransportCallbacks.sendProvisioningPdu(mUnprovisionedMeshNode, provisionerRandomConfirmationPDU);
    }

    @Override
    public boolean parseData(@NonNull final byte[] data) {
        mStatusCallbacks.onProvisioningStateChanged(mUnprovisionedMeshNode, States.PROVISIONING_RANDOM_RECEIVED, data);
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

        final byte[] confirmationInputs = provisioningCallbacks.generateConfirmationInputs(mUnprovisionedMeshNode.getProvisionerPublicKeyXY(),
                mUnprovisionedMeshNode.getProvisioneePublicKeyXY());
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
