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

package no.nordicsemi.android.mesh.transport;

import androidx.annotation.NonNull;

/**
 * This generic state class handles the proxy configuration messages received or sent.
 * <p>
 * Each message sent by the library has its own state.
 * </p>
 */
@SuppressWarnings("unused")
class ProxyConfigMessageState extends MeshMessageState {

    private static final String TAG = ProxyConfigMessageState.class.getSimpleName();

    /**
     * Constructs the ProxyConfigMessageState for sending/receiving proxy configuration messages
     *
     * @param src           Source address
     * @param dst           Destination address
     * @param meshMessage   {@link MeshMessage} Mesh proxy config message
     * @param meshTransport {@link MeshTransport} Mesh transport
     * @param callbacks     {@link InternalMeshMsgHandlerCallbacks} Internal callbacks
     */
    ProxyConfigMessageState(final int src,
                            final int dst,
                            @NonNull final MeshMessage meshMessage,
                            @NonNull final MeshTransport meshTransport,
                            @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        super(meshMessage, meshTransport, callbacks);
        this.mSrc = src;
        this.mDst = dst;
        createControlMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.PROXY_CONFIG_MESSAGE_STATE;
    }

    /**
     * Creates the control message to be sent to the node
     */
    private void createControlMessage() {
        final ProxyConfigMessage proxyConfigMessage = (ProxyConfigMessage) mMeshMessage;
        final int opCode = proxyConfigMessage.getOpCode();
        final byte[] parameters = proxyConfigMessage.getParameters();
        message = mMeshTransport.createProxyConfigurationMessage(mSrc, mDst, opCode, parameters);
        proxyConfigMessage.setMessage(message);
    }
}
