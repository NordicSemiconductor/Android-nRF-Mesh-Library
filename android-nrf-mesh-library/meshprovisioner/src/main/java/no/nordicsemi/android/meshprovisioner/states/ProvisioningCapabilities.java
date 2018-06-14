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
