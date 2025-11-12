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

import no.nordicsemi.android.mesh.logger.MeshLogger;

import java.nio.ByteBuffer;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.mesh.InternalProvisioningCallbacks;
import no.nordicsemi.android.mesh.InternalTransportCallbacks;
import no.nordicsemi.android.mesh.MeshManagerApi;
import no.nordicsemi.android.mesh.MeshProvisioningStatusCallbacks;
import no.nordicsemi.android.mesh.utils.InputOOBAction;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.mesh.utils.OutputOOBAction;
import no.nordicsemi.android.mesh.utils.SecureUtils;

public class ProvisioningConfirmationState extends ProvisioningState {

    private final String TAG = ProvisioningConfirmationState.class.getSimpleName();
    private static final byte[] NO_OOB_AUTH = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    public static final int AUTH_VALUE_LENGTH = 16;
    private final InternalProvisioningCallbacks provisioningCallbacks;
    private final UnprovisionedMeshNode mNode;
    private final MeshProvisioningStatusCallbacks mStatusCallbacks;
    private final InternalTransportCallbacks mInternalTransportCallbacks;
    private String authentication;

    /**
     * Constructs the provisioning confirmation state.
     *
     * @param node                        {@link UnprovisionedMeshNode} node.
     * @param callbacks                   {@link InternalProvisioningCallbacks} callbacks.
     * @param internalTransportCallbacks  {@link InternalTransportCallbacks} callbacks.
     * @param provisioningStatusCallbacks {@link MeshProvisioningStatusCallbacks} callbacks.
     */
    public ProvisioningConfirmationState(@NonNull final UnprovisionedMeshNode node,
                                         @NonNull final InternalProvisioningCallbacks callbacks,
                                         @NonNull final InternalTransportCallbacks internalTransportCallbacks,
                                         @NonNull final MeshProvisioningStatusCallbacks provisioningStatusCallbacks) {
        super();
        this.provisioningCallbacks = callbacks;
        this.mNode = node;
        this.mInternalTransportCallbacks = internalTransportCallbacks;
        this.mStatusCallbacks = provisioningStatusCallbacks;
    }

    /**
     * Sets the provisioning confirmation
     *
     * @param authentication authentication value
     */
    public void setProvisioningAuthentication(@Nullable final String authentication) {
        this.authentication = authentication;
    }

    @Override
    public State getState() {
        return State.PROVISIONING_CONFIRMATION;
    }

    @Override
    public void executeSend() {
        final byte[] provisioningConfirmationPDU = createProvisioningConfirmation();
        mStatusCallbacks.onProvisioningStateChanged(mNode, States.PROVISIONING_CONFIRMATION_SENT, provisioningConfirmationPDU);
        mInternalTransportCallbacks.sendProvisioningPdu(mNode, provisioningConfirmationPDU);
    }

    @Override
    public boolean parseData(@NonNull final byte[] data) {
        mStatusCallbacks.onProvisioningStateChanged(mNode, States.PROVISIONING_CONFIRMATION_RECEIVED, data);
        parseProvisioneeConfirmation(data);
        // Errata E16350 added an extra validation whether the received Confirmation is different
        // than Provisioner's one.
        return !Arrays.equals(mNode.provisioningConfirmation, mNode.provisioneeConfirmation);
    }

    private byte[] createProvisioningConfirmation() {

        final byte[] confirmationInputs = provisioningCallbacks.generateConfirmationInputs(mNode.getProvisionerPublicKeyXY(), mNode.getProvisioneePublicKeyXY());
        MeshLogger.verbose(TAG, "Confirmation inputs: " + MeshParserUtils.bytesToHex(confirmationInputs, false));

        //Generate a confirmation salt of the confirmation inputs
        final byte[] confirmationSalt = SecureUtils.calculateSalt(confirmationInputs);
        MeshLogger.verbose(TAG, "Confirmation salt: " + MeshParserUtils.bytesToHex(confirmationSalt, false));

        final byte[] ecdhSecret = mNode.getSharedECDHSecret();

        //Generate the confirmationKey by calculating the K1 of ECDH, confirmationSalt and ASCII value of "prck".
        final byte[] confirmationKey = SecureUtils.calculateK1(ecdhSecret, confirmationSalt, SecureUtils.PRCK);
        MeshLogger.verbose(TAG, "Confirmation key: " + MeshParserUtils.bytesToHex(confirmationKey, false));

        //Generate provisioner random number
        final byte[] provisionerRandom = SecureUtils.generateRandomNumber();
        mNode.setProvisionerRandom(provisionerRandom);
        MeshLogger.verbose(TAG, "Provisioner random: " + MeshParserUtils.bytesToHex(provisionerRandom, false));

        //Generate authentication value from the user input authentication
        final byte[] authenticationValue = generateAuthenticationValue();
        if (authenticationValue == null)
            throw new IllegalArgumentException("Invalid authentication value!");
        mNode.setAuthenticationValue(authenticationValue);
        MeshLogger.verbose(TAG, "Authentication value: " + MeshParserUtils.bytesToHex(authenticationValue, false));

        ByteBuffer buffer = ByteBuffer.allocate(provisionerRandom.length + 16);
        buffer.put(provisionerRandom);
        buffer.put(authenticationValue);
        final byte[] confirmationData = buffer.array();

        final byte[] confirmationValue = SecureUtils.calculateCMAC(confirmationData, confirmationKey);
        mNode.provisioningConfirmation = confirmationValue;

        buffer = ByteBuffer.allocate(confirmationValue.length + 2);
        buffer.put(new byte[]{MeshManagerApi.PDU_TYPE_PROVISIONING, TYPE_PROVISIONING_CONFIRMATION});
        buffer.put(confirmationValue);
        final byte[] provisioningConfirmationPDU = buffer.array();
        MeshLogger.verbose(TAG, "Provisioning confirmation: " + MeshParserUtils.bytesToHex(provisioningConfirmationPDU, false));

        return provisioningConfirmationPDU;
    }

    private byte[] generateAuthenticationValue() {
        switch (mNode.authMethodUsed) {
            case NO_OOB_AUTHENTICATION:
                return NO_OOB_AUTH;
            case STATIC_OOB_AUTHENTICATION:
                return MeshParserUtils.toByteArray(authentication);
            case OUTPUT_OOB_AUTHENTICATION:
                final OutputOOBAction action = OutputOOBAction.fromValue(mNode.getAuthActionUsed());
                return OutputOOBAction.generateOutputOOBAuthenticationValue(action, authentication);
            case INPUT_OOB_AUTHENTICATION:
                final InputOOBAction inputOOBAction = InputOOBAction.fromValue(mNode.getAuthActionUsed());
                return InputOOBAction.generateInputOOBAuthenticationValue(inputOOBAction, mNode.getInputAuthentication());
        }
        return null;
    }

    private void parseProvisioneeConfirmation(final byte[] provisioneeConfirmation) {
        final ByteBuffer buffer = ByteBuffer.allocate(provisioneeConfirmation.length - 2);
        buffer.put(provisioneeConfirmation, 2, buffer.limit());
        mNode.setProvisioneeConfirmation(buffer.array());
    }
}
