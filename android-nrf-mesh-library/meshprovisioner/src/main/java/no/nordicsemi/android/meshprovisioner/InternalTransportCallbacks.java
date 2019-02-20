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

package no.nordicsemi.android.meshprovisioner;

import android.support.annotation.RestrictTo;

import no.nordicsemi.android.meshprovisioner.provisionerstates.UnprovisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface InternalTransportCallbacks {

    /**
     * Returns the node with the corresponding unicast address
     *
     * @param unicast unicast address
     * @deprecated in favour of {@link #getProvisionedNode(int)}
     */
    ProvisionedMeshNode getProvisionedNode(final byte[] unicast);

    /**
     * Returns the node with the corresponding unicast address
     *
     * @param unicast unicast address
     */
    ProvisionedMeshNode getProvisionedNode(final int unicast);

    /**
     * Send mesh pdu
     *
     * @param meshNode mesh node to send to
     * @param pdu      mesh pdu to be sent
     */
    void sendProvisioningPdu(final UnprovisionedMeshNode meshNode, final byte[] pdu);

    /**
     * Send mesh pdu
     *
     * @param dst Destination address to be sent
     * @param pdu mesh pdu to be sent
     * @deprecated in favour of {@link #sendMeshPdu(int, byte[])}
     */
    @Deprecated
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    void sendMeshPdu(final byte[] dst, final byte[] pdu);

    /**
     * Send mesh pdu
     *
     * @param dst Destination address to be sent
     * @param pdu mesh pdu to be sent
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    void sendMeshPdu(final int dst, final byte[] pdu);

    /**
     * Update mesh network
     *
     * @param message mesh message
     */
    void updateMeshNetwork(final MeshMessage message);

    /**
     * This callback is invoked when the mesh node is successfully reset
     *
     * @param meshNode mesh to be updated
     */
    void onMeshNodeReset(final ProvisionedMeshNode meshNode);


}
