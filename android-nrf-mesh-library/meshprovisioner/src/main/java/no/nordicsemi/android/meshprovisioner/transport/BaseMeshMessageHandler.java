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

            switch (mMeshMessageState.getState()) {
                case COMPOSITION_DATA_GET_STATE:
                    final ConfigCompositionDataGetState compositionDataGet = (ConfigCompositionDataGetState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, compositionDataGet.getMeshMessage(), mMeshTransport, this));
                    break;
                case APP_KEY_ADD_STATE:
                    final ConfigAppKeyAddState appKeyAddState = (ConfigAppKeyAddState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, appKeyAddState.getMeshMessage(), mMeshTransport, this));
                    break;
                case CONFIG_MODEL_APP_BIND_STATE:
                    final ConfigModelAppBindState appBindState = (ConfigModelAppBindState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, appBindState.getMeshMessage(), mMeshTransport, this));
                    break;
                case CONFIG_MODEL_APP_UNBIND_STATE:
                    final ConfigModelAppUnbindState appUnbindState = (ConfigModelAppUnbindState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, appUnbindState.getMeshMessage(), mMeshTransport, this));
                    break;
                case CONFIG_MODEL_PUBLICATION_GET_STATE:
                    final ConfigModelPublicationGetState publicationGetState = (ConfigModelPublicationGetState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, publicationGetState.getMeshMessage(), mMeshTransport, this));
                    break;
                case CONFIG_MODEL_PUBLICATION_SET_STATE:
                    final ConfigModelPublicationSetState publicationSetState = (ConfigModelPublicationSetState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, publicationSetState.getMeshMessage(), mMeshTransport, this));
                    break;
                case CONFIG_MODEL_SUBSCRIPTION_ADD_STATE:
                    final ConfigModelSubscriptionAddState subscriptionAdd = (ConfigModelSubscriptionAddState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, subscriptionAdd.getMeshMessage(), mMeshTransport, this));
                    break;
                case CONFIG_MODEL_SUBSCRIPTION_DELETE_STATE:
                    final ConfigModelSubscriptionDeleteState subscriptionDelete = (ConfigModelSubscriptionDeleteState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, subscriptionDelete.getMeshMessage(), mMeshTransport, this));
                    break;
                case CONFIG_NODE_RESET_STATE:
                    final ConfigNodeResetState resetState = (ConfigNodeResetState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, resetState.getMeshMessage(), mMeshTransport, this));
                    break;
                case CONFIG_NETWORK_TRANSMIT_GET_STATE:
                    final ConfigNetworkTransmitGetState networkTransmitGet = (ConfigNetworkTransmitGetState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, networkTransmitGet.getMeshMessage(), mMeshTransport, this));
                    break;
                case CONFIG_NETWORK_TRANSMIT_SET_STATE:
                    final ConfigNetworkTransmitSetState networkTransmitSet = (ConfigNetworkTransmitSetState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, networkTransmitSet.getMeshMessage(), mMeshTransport, this));
                    break;
                case CONFIG_RELAY_GET_STATE:
                    final ConfigRelayGetState configRelayGetState = (ConfigRelayGetState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, configRelayGetState.getMeshMessage(), mMeshTransport, this));
                    break;
                case CONFIG_RELAY_SET_STATE:
                    final ConfigRelaySetState configRelaySetState = (ConfigRelaySetState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, configRelaySetState.getMeshMessage(), mMeshTransport, this));
                    break;
                case CONFIG_PROXY_GET_STATE:
                    final ConfigProxyGetState configProxyGetState = (ConfigProxyGetState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, configProxyGetState.getMeshMessage(), mMeshTransport, this));
                    break;
                case CONFIG_PROXY_SET_STATE:
                    final ConfigProxySetState configProxySetState = (ConfigProxySetState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, configProxySetState.getMeshMessage(), mMeshTransport, this));
                    break;
            }
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

        if (configurationMessage instanceof ConfigCompositionDataGet) {
            final ConfigCompositionDataGetState compositionDataGetState = new
                    ConfigCompositionDataGetState(mContext, src, dst, node.getDeviceKey(),
                    (ConfigCompositionDataGet) configurationMessage, mMeshTransport, this);
            compositionDataGetState.setTransportCallbacks(mInternalTransportCallbacks);
            compositionDataGetState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = compositionDataGetState;
            compositionDataGetState.executeSend();
        } else if (configurationMessage instanceof ConfigAppKeyAdd) {
            final ConfigAppKeyAddState configAppKeyAddState = new ConfigAppKeyAddState(mContext, src, dst, node.getDeviceKey(),
                    (ConfigAppKeyAdd) configurationMessage, mMeshTransport, this);
            configAppKeyAddState.setTransportCallbacks(mInternalTransportCallbacks);
            configAppKeyAddState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = configAppKeyAddState;
            configAppKeyAddState.executeSend();
        } else if (configurationMessage instanceof ConfigModelAppBind) {
            final ConfigModelAppBindState configModelAppBindState = new ConfigModelAppBindState(mContext, src, dst, node.getDeviceKey(),
                    (ConfigModelAppBind) configurationMessage, mMeshTransport, this);
            configModelAppBindState.setTransportCallbacks(mInternalTransportCallbacks);
            configModelAppBindState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = configModelAppBindState;
            configModelAppBindState.executeSend();
        } else if (configurationMessage instanceof ConfigModelAppUnbind) {
            final ConfigModelAppUnbindState modelAppUnbindState = new ConfigModelAppUnbindState(mContext, src, dst, node.getDeviceKey(),
                    (ConfigModelAppUnbind) configurationMessage, mMeshTransport, this);
            modelAppUnbindState.setTransportCallbacks(mInternalTransportCallbacks);
            modelAppUnbindState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = modelAppUnbindState;
            modelAppUnbindState.executeSend();
        } else if (configurationMessage instanceof ConfigModelPublicationGet) {
            final ConfigModelPublicationGetState configModelPublicationGetState = new ConfigModelPublicationGetState(mContext, src, dst, node.getDeviceKey(),
                    (ConfigModelPublicationGet) configurationMessage, mMeshTransport, this);
            configModelPublicationGetState.setTransportCallbacks(mInternalTransportCallbacks);
            configModelPublicationGetState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = configModelPublicationGetState;
            configModelPublicationGetState.executeSend();
        } else if (configurationMessage instanceof ConfigModelPublicationSet) {
            final ConfigModelPublicationSetState configModelPublicationSetState = new ConfigModelPublicationSetState(mContext, src, dst, node.getDeviceKey(),
                    (ConfigModelPublicationSet) configurationMessage, mMeshTransport, this);
            configModelPublicationSetState.setTransportCallbacks(mInternalTransportCallbacks);
            configModelPublicationSetState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = configModelPublicationSetState;
            configModelPublicationSetState.executeSend();
        } else if (configurationMessage instanceof ConfigModelSubscriptionAdd) {
            final ConfigModelSubscriptionAddState configModelSubscriptionAddState = new ConfigModelSubscriptionAddState(mContext, src, dst, node.getDeviceKey(),
                    (ConfigModelSubscriptionAdd) configurationMessage, mMeshTransport, this);
            configModelSubscriptionAddState.setTransportCallbacks(mInternalTransportCallbacks);
            configModelSubscriptionAddState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = configModelSubscriptionAddState;
            configModelSubscriptionAddState.executeSend();
        } else if (configurationMessage instanceof ConfigModelSubscriptionDelete) {
            final ConfigModelSubscriptionDeleteState configModelSubscriptionDeleteState = new ConfigModelSubscriptionDeleteState(mContext, src, dst, node.getDeviceKey(),
                    (ConfigModelSubscriptionDelete) configurationMessage, mMeshTransport, this);
            configModelSubscriptionDeleteState.setTransportCallbacks(mInternalTransportCallbacks);
            configModelSubscriptionDeleteState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = configModelSubscriptionDeleteState;
            configModelSubscriptionDeleteState.executeSend();
        } else if (configurationMessage instanceof ConfigNodeReset) {
            final ConfigNodeResetState configNodeResetState = new ConfigNodeResetState(mContext, src, dst, node.getDeviceKey(),
                    (ConfigNodeReset) configurationMessage, mMeshTransport, this);
            configNodeResetState.setTransportCallbacks(mInternalTransportCallbacks);
            configNodeResetState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = configNodeResetState;
            configNodeResetState.executeSend();
        } else if (configurationMessage instanceof ConfigNetworkTransmitGet) {
            final ConfigNetworkTransmitGetState configNetworkTransmitGetState = new ConfigNetworkTransmitGetState(mContext, src, dst, node.getDeviceKey(),
                    (ConfigNetworkTransmitGet) configurationMessage, mMeshTransport, this);
            configNetworkTransmitGetState.setTransportCallbacks(mInternalTransportCallbacks);
            configNetworkTransmitGetState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = configNetworkTransmitGetState;
            configNetworkTransmitGetState.executeSend();
        } else if (configurationMessage instanceof ConfigNetworkTransmitSet) {
            final ConfigNetworkTransmitSetState configNetworkTransmitSetState = new ConfigNetworkTransmitSetState(mContext, src, dst, node.getDeviceKey(),
                    (ConfigNetworkTransmitSet) configurationMessage, mMeshTransport, this);
            configNetworkTransmitSetState.setTransportCallbacks(mInternalTransportCallbacks);
            configNetworkTransmitSetState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = configNetworkTransmitSetState;
            configNetworkTransmitSetState.executeSend();
        } else if (configurationMessage instanceof ConfigRelayGet) {
            final ConfigRelayGetState configRelayGetState = new ConfigRelayGetState(mContext, src, dst, node.getDeviceKey(),
                    (ConfigRelayGet) configurationMessage, mMeshTransport, this);
            configRelayGetState.setTransportCallbacks(mInternalTransportCallbacks);
            configRelayGetState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = configRelayGetState;
            configRelayGetState.executeSend();
        } else if (configurationMessage instanceof ConfigRelaySet) {
            final ConfigRelaySetState configRelaySetState = new ConfigRelaySetState(mContext, src, dst, node.getDeviceKey(),
                    (ConfigRelaySet) configurationMessage, mMeshTransport, this);
            configRelaySetState.setTransportCallbacks(mInternalTransportCallbacks);
            configRelaySetState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = configRelaySetState;
            configRelaySetState.executeSend();
        } else if (configurationMessage instanceof ConfigProxyGet) {
            final ConfigProxyGetState configProxyGetState = new ConfigProxyGetState(mContext, src, dst, node.getDeviceKey(),
                    (ConfigProxyGet) configurationMessage, mMeshTransport, this);
            configProxyGetState.setTransportCallbacks(mInternalTransportCallbacks);
            configProxyGetState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = configProxyGetState;
            configProxyGetState.executeSend();
        } else if (configurationMessage instanceof ConfigProxySet) {
            final ConfigProxySetState configProxySetState = new ConfigProxySetState(mContext, src, dst, node.getDeviceKey(),
                    (ConfigProxySet) configurationMessage, mMeshTransport, this);
            configProxySetState.setTransportCallbacks(mInternalTransportCallbacks);
            configProxySetState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = configProxySetState;
            configProxySetState.executeSend();
        }
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
