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

import no.nordicsemi.android.mesh.MeshProvisioningStatusCallbacks;

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
    public boolean parseData(@NonNull final byte[] data) {
        final boolean flag = parseProvisioningCapabilities(data);
        //We store the provisioning capabilities pdu to be used when generating confirmation inputs
        mUnprovisionedMeshNode.setProvisioningCapabilitiesPdu(data);
        mUnprovisionedMeshNode.setProvisioningCapabilities(capabilities);
        mCallbacks.onProvisioningStateChanged(mUnprovisionedMeshNode, States.PROVISIONING_CAPABILITIES, data);
        return flag;
    }

    private boolean parseProvisioningCapabilities(final byte[] provisioningCapabilities) {
        this.capabilities = new ProvisioningCapabilities(provisioningCapabilities);
        return true;
    }
}
