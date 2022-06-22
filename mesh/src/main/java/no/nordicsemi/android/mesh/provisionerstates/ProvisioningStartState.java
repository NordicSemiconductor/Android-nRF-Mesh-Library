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

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import no.nordicsemi.android.mesh.InternalTransportCallbacks;
import no.nordicsemi.android.mesh.MeshManagerApi;
import no.nordicsemi.android.mesh.MeshProvisioningStatusCallbacks;
import no.nordicsemi.android.mesh.utils.AlgorithmType;
import no.nordicsemi.android.mesh.utils.AuthenticationOOBMethods;
import no.nordicsemi.android.mesh.utils.InputOOBAction;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.mesh.utils.OutputOOBAction;
import no.nordicsemi.android.mesh.utils.StaticOOBType;


public class ProvisioningStartState extends ProvisioningState {

    private final String TAG = ProvisioningStartState.class.getSimpleName();
    private final UnprovisionedMeshNode node;
    private final MeshProvisioningStatusCallbacks mMeshProvisioningStatusCallbacks;
    private final InternalTransportCallbacks mInternalTransportCallbacks;

    private List<AlgorithmType> algorithmTypes;
    private int outputOOBSize;
    private int inputOOBSize;
    private int publicKeyType;
    private short outputActionType;
    private short inputActionType;


    /**
     * Constructs the provisioning start state.
     *
     * @param node                        {@link UnprovisionedMeshNode} node.
     * @param capabilities                Provisioning capabilities.
     * @param internalTransportCallbacks  {@link InternalTransportCallbacks} callbacks.
     * @param provisioningStatusCallbacks {@link MeshProvisioningStatusCallbacks} callbacks.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public ProvisioningStartState(final UnprovisionedMeshNode node,
                                  final ProvisioningCapabilities capabilities,
                                  final InternalTransportCallbacks internalTransportCallbacks,
                                  final MeshProvisioningStatusCallbacks provisioningStatusCallbacks) {
        super();
        this.node = node;
        this.mInternalTransportCallbacks = internalTransportCallbacks;
        this.mMeshProvisioningStatusCallbacks = provisioningStatusCallbacks;
        setProvisioningCapabilities(capabilities);
    }

    public void setUseStaticOOB(final StaticOOBType actionType) {
        node.setAuthMethodUsed(AuthenticationOOBMethods.STATIC_OOB_AUTHENTICATION);
        node.setAuthActionUsed(actionType.getStaticOobType());
    }

    public void setUseOutputOOB(final OutputOOBAction actionType) {
        node.setAuthMethodUsed(AuthenticationOOBMethods.OUTPUT_OOB_AUTHENTICATION);
        this.outputActionType = actionType.getOutputOOBAction();
        node.setAuthActionUsed(actionType.getOutputOOBAction());
    }

    public void setUseInputOOB(final InputOOBAction actionType) {
        node.setAuthMethodUsed(AuthenticationOOBMethods.INPUT_OOB_AUTHENTICATION);
        this.inputActionType = actionType.getInputOOBAction();
        node.setAuthActionUsed(actionType.getInputOOBAction());
    }

    @Override
    public State getState() {
        return State.PROVISIONING_START;
    }

    @Override
    public void executeSend() {
        final byte[] provisioningStartPDU = createProvisioningStartPDU();
        //We store the provisioning start pdu to be used when generating confirmation inputs
        node.setProvisioningStartPdu(provisioningStartPDU);
        mMeshProvisioningStatusCallbacks.onProvisioningStateChanged(node, States.PROVISIONING_START, provisioningStartPDU);
        mInternalTransportCallbacks.sendProvisioningPdu(node, provisioningStartPDU);
    }

    @Override
    public boolean parseData(@NonNull final byte[] data) {
        return true;
    }

    private byte[] createProvisioningStartPDU() {
        final byte[] provisioningPDU = new byte[7];
        provisioningPDU[0] = MeshManagerApi.PDU_TYPE_PROVISIONING;
        provisioningPDU[1] = TYPE_PROVISIONING_START;
        provisioningPDU[2] = AlgorithmType.getSupportedAlgorithmValue(algorithmTypes);
        provisioningPDU[3] = (byte) (node.getProvisioneePublicKeyXY() == null ? 0 : publicKeyType);
        provisioningPDU[4] = (byte) node.getAuthMethodUsed().ordinal();
        switch (node.getAuthMethodUsed()) {
            case NO_OOB_AUTHENTICATION:
            case STATIC_OOB_AUTHENTICATION:
                provisioningPDU[5] = 0;
                provisioningPDU[6] = 0;
                break;
            case OUTPUT_OOB_AUTHENTICATION:
                provisioningPDU[5] = (byte) OutputOOBAction.getOutputOOBActionValue(this.outputActionType);
                provisioningPDU[6] = (byte) outputOOBSize;
                break;
            case INPUT_OOB_AUTHENTICATION:
                provisioningPDU[5] = (byte) InputOOBAction.getInputOOBActionValue(this.inputActionType);
                provisioningPDU[6] = (byte) inputOOBSize;
                node.setInputAuthentication(InputOOBAction.getInputOOOBAuthenticationValue(inputActionType, (byte) inputOOBSize));
                break;

        }
        MeshLogger.verbose(TAG, "Provisioning start PDU: " + MeshParserUtils.bytesToHex(provisioningPDU, true));

        return provisioningPDU;
    }

    private void setProvisioningCapabilities(final ProvisioningCapabilities capabilities) {
        this.algorithmTypes = capabilities.getSupportedAlgorithmTypes();
        this.outputOOBSize = capabilities.getOutputOOBSize();
        this.inputOOBSize = capabilities.getInputOOBSize();
        this.publicKeyType = capabilities.getRawPublicKeyType();
    }
}
