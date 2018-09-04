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

import android.util.Log;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.meshprovisioner.MeshProvisioningStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.ParseOutputOOBActions;
import no.nordicsemi.android.meshprovisioner.utils.ParseProvisioningAlgorithm;

public class ProvisioningStartState extends ProvisioningState {

    private final String TAG = ProvisioningStartState.class.getSimpleName();
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

    public ProvisioningStartState(final UnprovisionedMeshNode unprovisionedMeshNode, final InternalTransportCallbacks mInternalTransportCallbacks, final MeshProvisioningStatusCallbacks meshProvisioningStatusCallbacks) {
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
        final short outputOobActionType = (byte) ParseOutputOOBActions.selectOutputActionsFromBitMask(outputOOBAction);
        if(outputOobActionType == ParseOutputOOBActions.NO_OUTPUT){
            provisioningPDU[4] = 0;
            //prefer no oob
            provisioningPDU[5] = 0;
            provisioningPDU[6] = 0;
        } else {
            provisioningPDU[4] = 0x02;
            provisioningPDU[5] = (byte) ParseOutputOOBActions.getOuputOOBActionValue(outputOobActionType);//(byte) ParseOutputOOBActions.getOuputOOBActionValue(outputOOBAction);
            provisioningPDU[6] = (byte) outputOOBSize;
        }
        Log.v(TAG, "Provisioning start PDU: " + MeshParserUtils.bytesToHex(provisioningPDU, true));

        return provisioningPDU;
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
