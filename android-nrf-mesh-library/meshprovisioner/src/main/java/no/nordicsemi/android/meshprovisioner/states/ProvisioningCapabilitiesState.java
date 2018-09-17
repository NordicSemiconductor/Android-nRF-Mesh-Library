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
import no.nordicsemi.android.meshprovisioner.utils.AlgorithmInformationParser;
import no.nordicsemi.android.meshprovisioner.utils.ParseInputOOBActions;
import no.nordicsemi.android.meshprovisioner.utils.ParseOutputOOBActions;
import no.nordicsemi.android.meshprovisioner.utils.ParsePublicKeyInformation;
import no.nordicsemi.android.meshprovisioner.utils.ParseStaticOutputOOBInformation;

public class ProvisioningCapabilitiesState extends ProvisioningState {
    private static final String TAG = ProvisioningInviteState.class.getSimpleName();

    private final UnprovisionedMeshNode mUnprovisionedMeshNode;
    private final MeshProvisioningStatusCallbacks mCallbacks;

    private ProvisioningCapabilities capabilities;

    public ProvisioningCapabilitiesState(final UnprovisionedMeshNode unprovisionedMeshNode, final MeshProvisioningStatusCallbacks callbacks) {
        super();
        this.mCallbacks = callbacks;
        this.mUnprovisionedMeshNode = unprovisionedMeshNode;
    }

    @Override
    public State getState() {
        return State.PROVISIONING_CAPABILITIES;
    }

    public ProvisioningCapabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public void executeSend() {

    }

    @Override
    public boolean parseData(final byte[] data) {
        final boolean flag = parseProvisioningCapabilities(data);
        mUnprovisionedMeshNode.setProvisioningCapabilities(capabilities);
        mCallbacks.onProvisioningCapabilitiesReceived(mUnprovisionedMeshNode);
        return flag;
    }

    private boolean parseProvisioningCapabilities(final byte[] provisioningCapabilities) {

        final ProvisioningCapabilities capabilities = new ProvisioningCapabilities();
        capabilities.setRawCapabilities(provisioningCapabilities);

        if (provisioningCapabilities[2] == 0) {
            throw new IllegalArgumentException("Number of elements cannot be zero");
        }

        final byte numberOfElements = (provisioningCapabilities[2]);
        Log.v(TAG, "Number of elements: " + numberOfElements);
        capabilities.setNumberOfElements(numberOfElements);

        final short algorithm = (short) (((provisioningCapabilities[3] & 0xff) << 8) | (provisioningCapabilities[4] & 0xff));
        Log.v(TAG, "Algorithm: " + AlgorithmInformationParser.parseAlgorithm(algorithm));
        capabilities.setSupportedAlgorithm(algorithm);

        final byte publicKeyType = (provisioningCapabilities[5]); // 0 is unavailable and 1 is available
        Log.v(TAG, "Public key type: " + ParsePublicKeyInformation.parsePublicKeyInformation(publicKeyType));
        capabilities.setPublicKeyType(publicKeyType);

        final byte staticOOBType = (provisioningCapabilities[6]); // 0 is unavailable and 1 is available
        Log.v(TAG, "Static OOB type: " + ParseStaticOutputOOBInformation.parseStaticOOBActionInformation(staticOOBType));
        capabilities.setStaticOOBType(staticOOBType);

        final byte outputOOBSize = (provisioningCapabilities[7]);
        Log.v(TAG, "Output OOB size: " + outputOOBSize);
        capabilities.setOutputOOBSize(outputOOBSize);

        final short outputOOBAction = (short) (((provisioningCapabilities[8] & 0xff) << 8) | (provisioningCapabilities[9] & 0xff));
        ParseOutputOOBActions.parseOutputActionsFromBitMask(outputOOBAction);
        capabilities.setOutputOOBAction(outputOOBAction);

        final byte inputOOBSize = (provisioningCapabilities[10]);
        Log.v(TAG, "Input OOB size: " + inputOOBSize);
        capabilities.setInputOOBSize(inputOOBSize);

        final short inputOOBAction = (short) (((provisioningCapabilities[11] & 0xff) << 8) | (provisioningCapabilities[12] & 0xff));
        ParseInputOOBActions.parseInputActionsFromBitMask(inputOOBAction);
        capabilities.setInputOOBAction(inputOOBAction);
        this.capabilities = capabilities;

        return true;
    }
}
