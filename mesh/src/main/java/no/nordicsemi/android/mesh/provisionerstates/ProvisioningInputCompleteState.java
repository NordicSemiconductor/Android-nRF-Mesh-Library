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

import no.nordicsemi.android.mesh.InternalTransportCallbacks;
import no.nordicsemi.android.mesh.MeshManagerApi;
import no.nordicsemi.android.mesh.MeshProvisioningStatusCallbacks;

public class ProvisioningInputCompleteState extends ProvisioningState {

    private final UnprovisionedMeshNode mNode;
    private final InternalTransportCallbacks mInternalTransportCallbacks;
    private final MeshProvisioningStatusCallbacks mMeshProvisioningStatusCallbacks;

    /**
     * Constructs the provisioning input complete state
     *
     * @param node                        {@link UnprovisionedMeshNode} node
     * @param internalTransportCallbacks  {@link InternalTransportCallbacks} callbacks
     * @param provisioningStatusCallbacks {@link MeshProvisioningStatusCallbacks} callbacks
     */
    public ProvisioningInputCompleteState(@NonNull final UnprovisionedMeshNode node,
                                          @NonNull final InternalTransportCallbacks internalTransportCallbacks,
                                          @NonNull final MeshProvisioningStatusCallbacks provisioningStatusCallbacks) {
        super();
        this.mNode = node;
        this.mInternalTransportCallbacks = internalTransportCallbacks;
        this.mMeshProvisioningStatusCallbacks = provisioningStatusCallbacks;
    }

    @Override
    public State getState() {
        return State.PROVISIONING_INPUT_COMPLETE;
    }

    @Override
    public void executeSend() {
        //Do nothing here
    }

    @Override
    public boolean parseData(@NonNull final byte[] data) {
        if (data.length == 2 &&
                data[0] == MeshManagerApi.PDU_TYPE_PROVISIONING &&
                data[1] == TYPE_PROVISIONING_INPUT_COMPLETE) {
            mMeshProvisioningStatusCallbacks.onProvisioningStateChanged(mNode, States.PROVISIONING_AUTHENTICATION_INPUT_ENTERED, null);
            return true;
        }
        return false;
    }
}
