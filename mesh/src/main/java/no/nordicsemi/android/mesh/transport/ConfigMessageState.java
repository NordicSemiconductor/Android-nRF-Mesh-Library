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
 * ConfigMessageState class that handles configuration message state.
 */
class ConfigMessageState extends MeshMessageState {

    private final byte[] mDeviceKey;

    /**
     * Constructs the ConfigMessageState
     *
     * @param src           Source address
     * @param dst           Destination address
     * @param deviceKey     Device key
     * @param meshMessage   {@link MeshMessage} Mesh message to be sent
     * @param meshTransport {@link MeshTransport} Mesh transport
     * @param callbacks     {@link InternalMeshMsgHandlerCallbacks} callbacks
     */
    ConfigMessageState(final int src,
                       final int dst,
                       @NonNull final byte[] deviceKey,
                       @NonNull final MeshMessage meshMessage,
                       @NonNull final MeshTransport meshTransport,
                       @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        super(meshMessage, meshTransport, callbacks);
        this.mSrc = src;
        this.mDst = dst;
        this.mDeviceKey = deviceKey;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.CONFIG_MESSAGE_STATE;
    }

    private void createAccessMessage() throws IllegalArgumentException {
        final ConfigMessage configMessage = (ConfigMessage) mMeshMessage;
        final int akf = configMessage.getAkf();
        final int aid = configMessage.getAid();
        final int aszmic = configMessage.getAszmic();
        final int opCode = configMessage.getOpCode();
        final byte[] parameters = configMessage.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, configMessage.messageTtl,
                mDeviceKey, akf, aid, aszmic, opCode, parameters);
        configMessage.setMessage(message);
    }
}
