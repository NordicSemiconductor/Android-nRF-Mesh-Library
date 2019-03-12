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

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.utils.AddressUtils;

public abstract class BaseMeshMessageHandler implements MeshMessageHandlerApi, InternalMeshMsgHandlerCallbacks {

    private static final String TAG = BaseMeshMessageHandler.class.getSimpleName();

    protected final Context mContext;
    protected final MeshTransport mMeshTransport;
    protected final InternalTransportCallbacks mInternalTransportCallbacks;
    protected MeshStatusCallbacks mStatusCallbacks;
    private MeshMessageState mMeshMessageState;

    protected BaseMeshMessageHandler(final Context context, final InternalTransportCallbacks internalTransportCallbacks) {
        this.mContext = context;
        this.mMeshTransport = new MeshTransport(context);
        this.mInternalTransportCallbacks = internalTransportCallbacks;
    }

    protected abstract MeshTransport getMeshTransport();

    /**
     * Handle mesh message States on write callback complete
     * <p>
     * This method will jump to the current state and switch the current state according to the message that has been sent.
     * </p>
     *
     * @param pdu mesh pdu that was sent
     */
    public final void handleMeshMsgWriteCallbacks(final byte[] pdu) {
        if (mMeshMessageState instanceof ProxyConfigMessageState) {
            switch (mMeshMessageState.getState()) {
                case PROXY_CONFIG_SET_FILTER_TYPE_STATE:
                    final ProxyConfigSetFilterTypeState setFilterTypeState = (ProxyConfigSetFilterTypeState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, setFilterTypeState.getMeshMessage(), mMeshTransport, this));
                    break;
                case PROXY_CONFIG_ADD_ADDRESS_TO_FILTER_STATE:
                    final ProxyConfigAddAddressState addAddressState = (ProxyConfigAddAddressState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, addAddressState.getMeshMessage(), mMeshTransport, this));
                    break;
                case PROXY_CONFIG_REMOVE_ADDRESS_FROM_FILTER_STATE:
                    final ProxyConfigRemoveAddressState removeAddressState = (ProxyConfigRemoveAddressState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, removeAddressState.getMeshMessage(), mMeshTransport, this));
                    break;
            }
        } else if (mMeshMessageState instanceof ConfigMessageState) {
            if (mMeshMessageState.getState() == null)
                return;

            final ConfigMessageState configMessageState = (ConfigMessageState) mMeshMessageState;
            switchToNoOperationState(new DefaultNoOperationMessageState(mContext, configMessageState.getMeshMessage(), mMeshTransport, this));
        } else if (mMeshMessageState instanceof GenericMessageState) {
            switchToNoOperationState(new DefaultNoOperationMessageState(mContext, mMeshMessageState.getMeshMessage(), mMeshTransport, this));
        }
    }

    /**
     * Handle mesh States on receiving mesh message notifications
     * <p>
     * This method will jump to the current state and switch the state depending on the expected and the next message received.
     * </p>
     *
     * @param pdu mesh pdu that was sent
     */
    public final void parseMeshMsgNotifications(final byte[] pdu) {
        if (mMeshMessageState instanceof DefaultNoOperationMessageState) {
            ((DefaultNoOperationMessageState) mMeshMessageState).parseMeshPdu(pdu);
        }
    }

    @Override
    public final void onIncompleteTimerExpired(final boolean incompleteTimerExpired) {
        //We switch no operation state if the incomplete timer has expired so that we don't wait on the same state if a particular message fails.
        final MeshMessage meshMessage = mMeshMessageState.getMeshMessage();
        switchToNoOperationState(new DefaultNoOperationMessageState(mContext, meshMessage, mMeshTransport, this));
    }

    /**
     * Switch the current state of the mesh message handler
     * <p>
     * This method will switch the current state of the mesh message handler
     * </p>
     *
     * @param newState new state that is to be switched to
     */
    private void switchToNoOperationState(final MeshMessageState newState) {
        //Switching to unknown message state here for messages that are not
        if (mMeshMessageState.getState() != null && newState.getState() != null) {
            Log.v(TAG, "Switching current state " + mMeshMessageState.getState().name() + " to No operation state");
        } else {
            Log.v(TAG, "Switched to No operation state");
        }
        newState.setTransportCallbacks(mInternalTransportCallbacks);
        newState.setStatusCallbacks(mStatusCallbacks);
        mMeshMessageState = newState;
    }

    @Override
    public void sendMeshMessage(@NonNull final byte[] src, @NonNull final byte[] dst, @NonNull final MeshMessage meshMessage) {
        final int srcAddress = AddressUtils.getUnicastAddressInt(src);
        final int dstAddress = AddressUtils.getUnicastAddressInt(dst);
        if (meshMessage instanceof ProxyConfigMessage) {
            sendProxyConfigMeshMessage(srcAddress, dstAddress, (ProxyConfigMessage) meshMessage);
        } else if (meshMessage instanceof ConfigMessage) {
            sendConfigMeshMessage(srcAddress, dstAddress, (ConfigMessage) meshMessage);
        } else if (meshMessage instanceof GenericMessage) {
            sendAppMeshMessage(srcAddress, dstAddress, (GenericMessage) meshMessage);
        }
    }

    @Override
    public void sendMeshMessage(final int src, final int dst, @NonNull final MeshMessage meshMessage) {
        if (meshMessage instanceof ProxyConfigMessage) {
            sendProxyConfigMeshMessage(src, dst, (ProxyConfigMessage) meshMessage);
        } else if (meshMessage instanceof ConfigMessage) {
            sendConfigMeshMessage(src, dst, (ConfigMessage) meshMessage);
        } else if (meshMessage instanceof GenericMessage) {
            sendAppMeshMessage(src, dst, (GenericMessage) meshMessage);
        }
    }

    /**
     * Sends a mesh message specified within the {@link MeshMessage} object
     *
     * @param configurationMessage {@link ProxyConfigMessage} Mesh message containing the message opcode and message parameters
     */
    private void sendProxyConfigMeshMessage(final int src, final int dst, @NonNull final ProxyConfigMessage configurationMessage) {

        if (configurationMessage instanceof ProxyConfigSetFilterType) {
            final ProxyConfigSetFilterTypeState proxyConfigSetFilterTypeState = new ProxyConfigSetFilterTypeState(mContext, src, dst,
                    (ProxyConfigSetFilterType) configurationMessage, mMeshTransport, this);
            proxyConfigSetFilterTypeState.setTransportCallbacks(mInternalTransportCallbacks);
            proxyConfigSetFilterTypeState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = proxyConfigSetFilterTypeState;
            proxyConfigSetFilterTypeState.executeSend();
        } else if (configurationMessage instanceof ProxyConfigAddAddressToFilter) {
            final ProxyConfigAddAddressState proxyConfigAddAddressState = new ProxyConfigAddAddressState(mContext, src, dst,
                    (ProxyConfigAddAddressToFilter) configurationMessage, mMeshTransport, this);
            proxyConfigAddAddressState.setTransportCallbacks(mInternalTransportCallbacks);
            proxyConfigAddAddressState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = proxyConfigAddAddressState;
            proxyConfigAddAddressState.executeSend();
        } else if (configurationMessage instanceof ProxyConfigRemoveAddressFromFilter) {
            final ProxyConfigRemoveAddressState proxyConfigRemoveAddressState = new ProxyConfigRemoveAddressState(mContext, src, dst,
                    (ProxyConfigRemoveAddressFromFilter) configurationMessage, mMeshTransport, this);
            proxyConfigRemoveAddressState.setTransportCallbacks(mInternalTransportCallbacks);
            proxyConfigRemoveAddressState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = proxyConfigRemoveAddressState;
            proxyConfigRemoveAddressState.executeSend();
        }
    }

    /**
     * Sends a mesh message specified within the {@link MeshMessage} object
     *
     * @param configurationMessage {@link ConfigMessage} Mesh message containing the message opcode and message parameters
     */
    private void sendConfigMeshMessage(final int src, final int dst, @NonNull final ConfigMessage configurationMessage) {
        final ProvisionedMeshNode node = mInternalTransportCallbacks.getProvisionedNode(dst);
        if (node == null) {
            return;
        }

        final ConfigMessageState state = new ConfigMessageState(mContext, src, dst, node.getDeviceKey(), configurationMessage, mMeshTransport, this);
        state.setTransportCallbacks(mInternalTransportCallbacks);
        state.setStatusCallbacks(mStatusCallbacks);
        mMeshMessageState = state;
        state.executeSend();
    }


    /**
     * Sends a mesh message specified within the {@link GenericMessage} object
     * <p> This method can be used specifically when sending an application message with a unicast address or a group address.
     * Application messages currently supported in the library are {@link GenericOnOffGet},{@link GenericOnOffSet}, {@link GenericOnOffSetUnacknowledged},
     * {@link GenericLevelGet},  {@link GenericLevelSet},  {@link GenericLevelSetUnacknowledged},
     * {@link VendorModelMessageAcked} and {@link VendorModelMessageUnacked}</p>
     *
     * @param src            source address where the message is originating from
     * @param dst            Destination to which the message must be sent to, this could be a unicast address or a group address.
     * @param genericMessage Mesh message containing the message opcode and message parameters.
     */
    private void sendAppMeshMessage(final int src, final int dst, @NonNull final GenericMessage genericMessage) {
        final GenericMessageState genericMessageState = new GenericMessageState(mContext, src, dst, genericMessage, mMeshTransport, this);
        genericMessageState.setTransportCallbacks(mInternalTransportCallbacks);
        genericMessageState.setStatusCallbacks(mStatusCallbacks);
        mMeshMessageState = genericMessageState;
        genericMessageState.executeSend();
    }
}
