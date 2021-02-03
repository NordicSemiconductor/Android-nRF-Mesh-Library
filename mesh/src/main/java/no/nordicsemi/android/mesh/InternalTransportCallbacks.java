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

package no.nordicsemi.android.mesh;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import no.nordicsemi.android.mesh.provisionerstates.UnprovisionedMeshNode;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.ProxyFilter;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface InternalTransportCallbacks {


    /**
     * Returns an application key with a given key index
     *
     * @param boundNetKeyIndex NetKey index
     */
    List<ApplicationKey> getApplicationKeys(final int boundNetKeyIndex);

    /**
     * Returns the node with the corresponding unicast address
     *
     * @param unicast unicast address
     */
    ProvisionedMeshNode getNode(final int unicast);

    /**
     * Returns the Provisioner with the corresponding unicast address
     *
     * @param unicast unicast address
     */
    Provisioner getProvisioner(final int unicast);

    /**
     * Send mesh pdu
     *
     * @param meshNode mesh node to send to
     * @param pdu      mesh pdu to be sent
     */
    void sendProvisioningPdu(final UnprovisionedMeshNode meshNode, final byte[] pdu);

    /**
     * Callback that is invoked when a mesh pdu is created
     *
     * @param dst Destination address to be sent
     * @param pdu mesh pdu to be sent
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    void onMeshPduCreated(final int dst, final byte[] pdu);


    ProxyFilter getProxyFilter();

    void setProxyFilter(@NonNull final ProxyFilter filter);

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


    /**
     * Returns the mesh network
     */
    MeshNetwork getMeshNetwork();

    void storeScene(final int address, final int currentScene, final List<Integer> scenes);

    void deleteScene(final int address, final int currentScene, final List<Integer> scenes);
}
