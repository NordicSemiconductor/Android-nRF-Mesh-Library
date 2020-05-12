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

import androidx.annotation.NonNull;
import android.util.Log;

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
    private final UnprovisionedMeshNode mNode;
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
    private short outputActionType;
    private short inputActionType;

    public ProvisioningStartState(final UnprovisionedMeshNode node,
                                  final InternalTransportCallbacks internalTransportCallbacks,
                                  final MeshProvisioningStatusCallbacks provisioningStatusCallbacks) {
        super();
        this.mNode = node;
        this.mInternalTransportCallbacks = internalTransportCallbacks;
        this.mMeshProvisioningStatusCallbacks = provisioningStatusCallbacks;
    }

    public void setUseStaticOOB(final StaticOOBType actionType) {
        mNode.setAuthMethodUsed(AuthenticationOOBMethods.STATIC_OOB_AUTHENTICATION);
        mNode.setAuthActionUsed(actionType.getStaticOobType());
    }

    public void setUseOutputOOB(final OutputOOBAction actionType) {
        mNode.setAuthMethodUsed(AuthenticationOOBMethods.OUTPUT_OOB_AUTHENTICATION);
        this.outputActionType = actionType.getOutputOOBAction();
        mNode.setAuthActionUsed(actionType.getOutputOOBAction());
    }

    public void setUseInputOOB(final InputOOBAction actionType) {
        mNode.setAuthMethodUsed(AuthenticationOOBMethods.INPUT_OOB_AUTHENTICATION);
        this.inputActionType = actionType.getInputOOBAction();
        mNode.setAuthActionUsed(actionType.getInputOOBAction());
    }

    @Override
    public State getState() {
        return State.PROVISIONING_START;
    }

    @Override
    public void executeSend() {
        final byte[] provisioningStartPDU = createProvisioningStartPDU();
        //We store the provisioning start pdu to be used when generating confirmation inputs
        mNode.setProvisioningStartPdu(provisioningStartPDU);
        mMeshProvisioningStatusCallbacks.onProvisioningStateChanged(mNode, States.PROVISIONING_START, provisioningStartPDU);
        mInternalTransportCallbacks.sendProvisioningPdu(mNode, provisioningStartPDU);
    }

    @Override
    public boolean parseData(@NonNull final byte[] data) {
        return true;
    }

    private byte[] createProvisioningStartPDU() {
        final byte[] provisioningPDU = new byte[7];
        provisioningPDU[0] = MeshManagerApi.PDU_TYPE_PROVISIONING;
        provisioningPDU[1] = TYPE_PROVISIONING_START;
        provisioningPDU[2] = AlgorithmType.getAlgorithmValue((short) algorithm);
        provisioningPDU[3] = 0; // (byte) publicKeyType;
        provisioningPDU[4] = (byte) mNode.getAuthMethodUsed().ordinal();
        switch (mNode.getAuthMethodUsed()) {
            case NO_OOB_AUTHENTICATION:
                provisioningPDU[5] = 0;
                provisioningPDU[6] = 0;
                break;
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
                mNode.setInputAuthentication(InputOOBAction.getInputOOOBAuthenticationValue(inputActionType, (byte) inputOOBSize));
                break;

        }
        Log.v(TAG, "Provisioning start PDU: " + MeshParserUtils.bytesToHex(provisioningPDU, true));

        return provisioningPDU;
    }

    public void setProvisioningCapabilities(final int numberOfElements,
                                            final int algorithm,
                                            final int publicKeyType,
                                            final int staticOOBType,
                                            final int outputOOBSize,
                                            final int outputOOBAction,
                                            final int inputOOBSize,
                                            final int inputOOBAction) {
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
