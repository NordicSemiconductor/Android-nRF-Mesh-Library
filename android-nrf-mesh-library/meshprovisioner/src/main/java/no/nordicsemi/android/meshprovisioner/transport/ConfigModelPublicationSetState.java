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

package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class handling the publication set messages and its status message.
 */
@SuppressWarnings("WeakerAccess")
class ConfigModelPublicationSetState extends ConfigMessageState {

    private static final String TAG = ConfigModelPublicationSetState.class.getSimpleName();
    private final byte[] mDeviceKey;

    /**
     * Constructs the state for creating ConfigModelPublicationSet message
     *
     * @param context                   context
     * @param src                       source address
     * @param dst                       destination address
     * @param deviceKey                 device key
     * @param configModelPublicationSet {@link ConfigModelPublicationSet}
     * @param meshTransport             {@link MeshTransport}
     * @param callbacks                 {@link InternalMeshMsgHandlerCallbacks}
     */
    @Deprecated
    ConfigModelPublicationSetState(@NonNull final Context context,
                                   @NonNull final byte[] src,
                                   @NonNull final byte[] dst,
                                   @NonNull final byte[] deviceKey,
                                   @NonNull final ConfigModelPublicationSet configModelPublicationSet,
                                   @NonNull final MeshTransport meshTransport,
                                   @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), deviceKey, configModelPublicationSet, meshTransport, callbacks);
    }

    /**
     * Constructs the state for creating ConfigModelPublicationSet message
     *
     * @param context                   context
     * @param src                       source address
     * @param dst                       destination address
     * @param deviceKey                 device key
     * @param configModelPublicationSet {@link ConfigModelPublicationSet}
     * @param meshTransport             {@link MeshTransport}
     * @param callbacks                 {@link InternalMeshMsgHandlerCallbacks}
     */
    ConfigModelPublicationSetState(@NonNull final Context context,
                                   final int src,
                                   final int dst,
                                   @NonNull final byte[] deviceKey,
                                   @NonNull final ConfigModelPublicationSet configModelPublicationSet,
                                   @NonNull final MeshTransport meshTransport,
                                   @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        super(context, configModelPublicationSet, meshTransport, callbacks);
        this.mSrc = src;
        this.mDst = dst;
        this.mDeviceKey = deviceKey;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.CONFIG_MODEL_PUBLICATION_SET_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() throws IllegalArgumentException {
        final ConfigModelPublicationSet configModelPublicationSet = (ConfigModelPublicationSet) mMeshMessage;
        final int akf = configModelPublicationSet.getAkf();
        final int aid = configModelPublicationSet.getAid();
        final int aszmic = configModelPublicationSet.getAszmic();
        final int opCode = configModelPublicationSet.getOpCode();
        final byte[] parameters = configModelPublicationSet.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, mDeviceKey, akf, aid, aszmic, opCode, parameters);
        configModelPublicationSet.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending config model publication set");
        super.executeSend();

        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
        }
    }
}
