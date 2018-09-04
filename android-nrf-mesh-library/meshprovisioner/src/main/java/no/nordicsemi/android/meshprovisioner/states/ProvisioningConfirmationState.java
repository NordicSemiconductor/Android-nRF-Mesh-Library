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

package no.nordicsemi.android.meshprovisioner.states;

import android.text.TextUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.meshprovisioner.MeshProvisioningHandler;
import no.nordicsemi.android.meshprovisioner.MeshProvisioningStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

public class ProvisioningConfirmationState extends ProvisioningState {

    private final String TAG = ProvisioningConfirmationState.class.getSimpleName();

    private final MeshProvisioningHandler pduHandler;
    private final UnprovisionedMeshNode mUnprovisionedMeshNode;
    private final MeshProvisioningStatusCallbacks mMeshProvisioningStatusCallbacks;
    private final InternalTransportCallbacks mInternalTransportCallbacks;
    private String pin;

    public ProvisioningConfirmationState(final MeshProvisioningHandler pduHandler, final UnprovisionedMeshNode unprovisionedMeshNode, final InternalTransportCallbacks mInternalTransportCallbacks, final MeshProvisioningStatusCallbacks meshProvisioningStatusCallbacks) {
        super();
        this.pduHandler = pduHandler;
        this.mUnprovisionedMeshNode = unprovisionedMeshNode;
        this.mInternalTransportCallbacks = mInternalTransportCallbacks;
        this.mMeshProvisioningStatusCallbacks = meshProvisioningStatusCallbacks;
    }

    public void setPin(final String pin) {
        this.pin = pin;
    }

    @Override
    public State getState() {
        return State.PROVISIONING_CONFIRMATION;
    }

    @Override
    public void executeSend() {

        final byte[] provisioningConfirmationPDU;
        if (!TextUtils.isEmpty(pin)) {
            provisioningConfirmationPDU = createProvisioningConfirmation(pin.getBytes());
        } else {
            provisioningConfirmationPDU = createProvisioningConfirmation(null);
        }
        mMeshProvisioningStatusCallbacks.onProvisioningConfirmationSent(mUnprovisionedMeshNode);
        mInternalTransportCallbacks.sendPdu(mUnprovisionedMeshNode, provisioningConfirmationPDU);
    }

    @Override
    public boolean parseData(final byte[] data) {
        mMeshProvisioningStatusCallbacks.onProvisioningConfirmationReceived(mUnprovisionedMeshNode);
        parseProvisioneeConfirmation(data);
        return true;
    }

    private byte[] createProvisioningConfirmation(final byte[] userInput) {

        final byte[] confirmationInputs = pduHandler.generateConfirmationInputs(mUnprovisionedMeshNode.getProvisionerPublicKeyXY(), mUnprovisionedMeshNode.getProvisioneePublicKeyXY());
        Log.v(TAG, "Confirmation inputs: " + MeshParserUtils.bytesToHex(confirmationInputs, false));

        //Generate a confirmation salt of the confirmation inputs
        final byte[] confirmationSalt = SecureUtils.calculateSalt(confirmationInputs);
        Log.v(TAG, "Confirmation salt: " + MeshParserUtils.bytesToHex(confirmationSalt, false));

        final byte[] ecdhSecret = mUnprovisionedMeshNode.getSharedECDHSecret();

        //Generate the confirmationKey by calculating the K1 of ECDH, confirmationSalt and ASCII value of "prck".
        final byte[] confirmationKey = SecureUtils.calculateK1(ecdhSecret, confirmationSalt, SecureUtils.PRCK);
        Log.v(TAG, "Confirmation key: " + MeshParserUtils.bytesToHex(confirmationKey, false));

        //Generate provisioner random number
        final byte[] provisionerRandom = SecureUtils.generateRandomNumber();
        mUnprovisionedMeshNode.setProvisionerRandom(provisionerRandom);
        Log.v(TAG, "Provisioner random: " + MeshParserUtils.bytesToHex(provisionerRandom, false));

        //Generate authentication value from the user input pin
        final byte[] authenticationValue = generateAuthenticationValue(userInput);
        mUnprovisionedMeshNode.setAuthenticationValue(authenticationValue);
        Log.v(TAG, "Authentication value: " + MeshParserUtils.bytesToHex(authenticationValue, false));

        ByteBuffer buffer = ByteBuffer.allocate(provisionerRandom.length + authenticationValue.length);
        buffer.put(provisionerRandom);
        buffer.put(authenticationValue);
        final byte[] confirmationData = buffer.array();

        final byte[] confirmationValue = SecureUtils.calculateCMAC(confirmationData, confirmationKey);

        buffer = ByteBuffer.allocate(confirmationValue.length + 2);
        buffer.put(new byte[]{MeshManagerApi.PDU_TYPE_PROVISIONING, TYPE_PROVISIONING_CONFIRMATION});
        buffer.put(confirmationValue);
        final byte[] provisioningConfirmationPDU = buffer.array();
        Log.v(TAG, "Provisioning confirmation: " + MeshParserUtils.bytesToHex(provisioningConfirmationPDU, false));

        return provisioningConfirmationPDU;
    }

    private byte[] generateAuthenticationValue(final byte[] pin) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        if (pin != null) {
            final Integer authValue = Integer.valueOf(new String(pin, Charset.forName("UTF-8")));
            buffer.position(12);
            buffer.putInt(authValue);
        }
        return buffer.array();
    }

    private void parseProvisioneeConfirmation(final byte[] provisioneeConfirmation) {
        final ByteBuffer buffer = ByteBuffer.allocate(provisioneeConfirmation.length - 2);
        buffer.put(provisioneeConfirmation, 2, buffer.limit());
        mUnprovisionedMeshNode.setProvisioneeConfirmation(buffer.array());
    }
}
