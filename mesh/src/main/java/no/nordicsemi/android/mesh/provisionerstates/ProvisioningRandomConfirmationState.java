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

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import no.nordicsemi.android.mesh.InternalTransportCallbacks;
import no.nordicsemi.android.mesh.MeshManagerApi;
import no.nordicsemi.android.mesh.MeshProvisioningStatusCallbacks;
import no.nordicsemi.android.mesh.logger.MeshLogger;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

public class ProvisioningRandomConfirmationState extends ProvisioningState {

    private final String TAG = ProvisioningRandomConfirmationState.class.getSimpleName();
    private final UnprovisionedMeshNode node;
    private final MeshProvisioningStatusCallbacks mStatusCallbacks;
    private final InternalTransportCallbacks mInternalTransportCallbacks;

    /**
     * Constructs the provisioning random confirmation state.
     *
     * @param node                          {@link UnprovisionedMeshNode} node.
     * @param internalTransportCallbacks    {@link InternalTransportCallbacks} callbacks.
     * @param provisioningStatusCallbacks   {@link MeshProvisioningStatusCallbacks} callbacks.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public ProvisioningRandomConfirmationState(@NonNull final UnprovisionedMeshNode node,
                                               @NonNull final InternalTransportCallbacks internalTransportCallbacks,
                                               @NonNull final MeshProvisioningStatusCallbacks provisioningStatusCallbacks) {
        super();
        this.node = node;
        this.mInternalTransportCallbacks = internalTransportCallbacks;
        this.mStatusCallbacks = provisioningStatusCallbacks;
    }

    @Override
    public State getState() {
        return State.PROVISIONING_RANDOM;
    }

    @Override
    public void executeSend() {
        final byte[] provisionerRandomConfirmationPDU = createProvisionerRandomPDU();
        mStatusCallbacks.onProvisioningStateChanged(node, States.PROVISIONING_CONFIRMATION_SENT, provisionerRandomConfirmationPDU);
        mInternalTransportCallbacks.sendProvisioningPdu(node, provisionerRandomConfirmationPDU);
    }

    @Override
    public boolean parseData(@NonNull final byte[] data) {
        mStatusCallbacks.onProvisioningStateChanged(node, States.PROVISIONING_RANDOM_RECEIVED, data);
        parseProvisioneeRandom(data);
        return true;
    }

    private byte[] createProvisionerRandomPDU() {
        final byte[] provisionerRandom = node.getProvisionerRandom();
        final ByteBuffer buffer = ByteBuffer.allocate(provisionerRandom.length + 2);
        buffer.put(new byte[]{MeshManagerApi.PDU_TYPE_PROVISIONING, TYPE_PROVISIONING_RANDOM_CONFIRMATION});
        buffer.put(provisionerRandom);
        final byte[] data = buffer.array();
        MeshLogger.verbose(TAG, "Provisioner random PDU: " + MeshParserUtils.bytesToHex(data, false));
        return data;
    }

    private void parseProvisioneeRandom(final byte[] provisioneeRandomPDU) {
        final ByteBuffer buffer = ByteBuffer.allocate(provisioneeRandomPDU.length - 2);
        buffer.put(provisioneeRandomPDU, 2, buffer.limit());
        node.setProvisioneeRandom(buffer.array());
    }
}
