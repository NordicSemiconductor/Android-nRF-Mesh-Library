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

import no.nordicsemi.android.mesh.provisionerstates.UnprovisionedMeshNode;

/**
 * Implement this class in order to get the transport callbacks from the {@link MeshManagerApi}
 */
public interface MeshManagerCallbacks {

    /**
     * Returns the network that was loaded
     *
     * @param meshNetwork{@link MeshNetwork that was loaded}
     */
    void onNetworkLoaded(final MeshNetwork meshNetwork);

    /**
     * Returns the network that was updated
     * <p>
     * This callback is invoked for every message that was sent or received as it changes the contents of the network
     * </p>
     *
     * @param meshNetwork{@link MeshNetwork that was loaded}
     */
    void onNetworkUpdated(final MeshNetwork meshNetwork);

    /**
     * Callback that notifies in case the mesh network was unable to load
     *
     * @param error error
     */
    void onNetworkLoadFailed(final String error);

    /**
     * Callbacks notifying the network was imported
     *
     * @param meshNetwork{@link MeshNetwork that was loaded}
     */
    void onNetworkImported(final MeshNetwork meshNetwork);

    /**
     * Callback that notifies in case the mesh network was unable to imported
     *
     * @param error error
     */
    void onNetworkImportFailed(final String error);

    /**
     * Send mesh pdu
     *
     * @param meshNode {@link UnprovisionedMeshNode}
     * @param pdu      mesh pdu to be sent
     */
    void sendProvisioningPdu(final UnprovisionedMeshNode meshNode, final byte[] pdu);

    /**
     * Send mesh pdu
     *
     * @param pdu mesh pdu to be sent
     */
    void onMeshPduCreated(final byte[] pdu);

    /**
     * Get mtu size supported by the peripheral node
     * <p>
     * This is used to get the supported mtu size from the ble module, so that the messages
     * that are larger than the supported mtu size could be segmented
     * </p>
     *
     * @return mtu size
     */
    int getMtu();
}
