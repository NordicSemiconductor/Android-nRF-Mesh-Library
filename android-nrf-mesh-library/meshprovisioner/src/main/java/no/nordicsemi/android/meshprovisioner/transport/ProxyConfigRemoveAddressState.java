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
 * This state class handles the message relating removing an address from an existing filter on a proxy server
 */
@SuppressWarnings({"unused", "WeakerAccess"})
class ProxyConfigRemoveAddressState extends ProxyConfigMessageState {

    private final String TAG = ProxyConfigRemoveAddressState.class.getSimpleName();

    /**
     * Constructs the state class for sending {@link ProxyConfigSetFilterType}
     *
     * @param context       Context
     * @param src           Source address
     * @param dst           Destination address
     * @param removeAddress {@link ProxyConfigAddAddressToFilter} mesh message
     * @param meshTransport MeshTransport
     * @param callbacks     Internal mesh message handler callbacks
     */
    ProxyConfigRemoveAddressState(@NonNull final Context context,
                                  @NonNull final byte[] src,
                                  @NonNull final byte[] dst,
                                  @NonNull ProxyConfigRemoveAddressFromFilter removeAddress,
                                  @NonNull final MeshTransport meshTransport,
                                  @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), removeAddress, meshTransport, callbacks);
    }

    /**
     * Constructs the state class for sending {@link ProxyConfigSetFilterType}
     *
     * @param context       Context
     * @param src           Source address
     * @param dst           Destination address
     * @param removeAddress {@link ProxyConfigAddAddressToFilter} mesh message
     * @param meshTransport MeshTransport
     * @param callbacks     Internal mesh message handler callbacks
     */
    ProxyConfigRemoveAddressState(@NonNull final Context context,
                                  final int src,
                                  final int dst,
                                  @NonNull ProxyConfigRemoveAddressFromFilter removeAddress,
                                  @NonNull final MeshTransport meshTransport,
                                  @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        super(context, removeAddress, meshTransport, callbacks);
        this.mSrc = src;
        this.mDst = dst;
        createControlMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.PROXY_CONFIG_REMOVE_ADDRESS_FROM_FILTER_STATE;
    }

    /**
     * Creates the control message to be sent to the node
     */
    private void createControlMessage() {
        final ProxyConfigRemoveAddressFromFilter removeAddressFromFilter = (ProxyConfigRemoveAddressFromFilter) mMeshMessage;
        final int opCode = removeAddressFromFilter.getOpCode();
        final byte[] parameters = removeAddressFromFilter.getParameters();
        message = mMeshTransport.createProxyConfigurationMessage(mSrc, mDst, opCode, parameters);
        removeAddressFromFilter.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending set filter type");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
        }
    }
}
