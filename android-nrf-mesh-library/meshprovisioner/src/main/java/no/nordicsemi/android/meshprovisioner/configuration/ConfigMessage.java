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

package no.nordicsemi.android.meshprovisioner.configuration;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshConfigurationStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.control.BlockAcknowledgementMessage;
import no.nordicsemi.android.meshprovisioner.control.TransportControlMessage;
import no.nordicsemi.android.meshprovisioner.messages.ControlMessage;
import no.nordicsemi.android.meshprovisioner.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.transport.LowerTransportLayerCallbacks;

public abstract class ConfigMessage implements LowerTransportLayerCallbacks {

    private static final String TAG = ConfigCompositionDataStatus.class.getSimpleName();
    protected final Context mContext;
    protected final ProvisionedMeshNode mProvisionedMeshNode;
    final MeshTransport mMeshTransport;
    final Map<Integer, byte[]> mPayloads = new HashMap<>();
    final byte[] mSrc;
    protected InternalTransportCallbacks mInternalTransportCallbacks;
    MeshConfigurationStatusCallbacks mConfigStatusCallbacks;
    protected MeshModel mMeshModel;
    protected int mAppKeyIndex;

    public ConfigMessage(final Context context, final ProvisionedMeshNode provisionedMeshNode) {
        this.mContext = context;
        this.mProvisionedMeshNode = provisionedMeshNode;
        this.mSrc = mProvisionedMeshNode.getConfigurationSrc();
        this.mMeshTransport = new MeshTransport(context, provisionedMeshNode);
        this.mMeshTransport.setLowerTransportLayerCallbacks(this);
    }
    public abstract MessageState getState();

    /**
     * Parses control message and returns the underlying configuration message
     *
     * @param controlMessage control message to be passed
     */
    final void parseControlMessage(final ControlMessage controlMessage) {
        final TransportControlMessage transportControlMessage = controlMessage.getTransportControlMessage();
        switch (transportControlMessage.getState()) {
            case LOWER_TRANSPORT_BLOCK_ACKNOWLEDGEMENT:
                if (controlMessage.getTransportControlMessage().getState() == TransportControlMessage.TransportControlMessageState.LOWER_TRANSPORT_BLOCK_ACKNOWLEDGEMENT) {
                    final BlockAcknowledgementMessage blockAcknowledgementMessage = (BlockAcknowledgementMessage) controlMessage.getTransportControlMessage();
                    mConfigStatusCallbacks.onBlockAcknowledgementReceived(mProvisionedMeshNode);
                }
                break;
            default:
                Log.v(TAG, "Unexpected control message received, ignoring message");
                mConfigStatusCallbacks.onUnknownPduReceived(mProvisionedMeshNode);
                break;
        }
    }

    public ProvisionedMeshNode getMeshNode() {
        return mProvisionedMeshNode;
    }

    public MeshModel getMeshModel() {
        return mMeshModel;
    }

    public int getAppKeyIndex() {
        return mAppKeyIndex;
    }

    public enum MessageState {
        //Configuration message states
        COMPOSITION_DATA_GET(ConfigMessageOpCodes.CONFIG_COMPOSITION_DATA_GET),
        COMPOSITION_DATA_STATUS(ConfigMessageOpCodes.CONFIG_COMPOSITION_DATA_STATUS),
        APP_KEY_ADD(ConfigMessageOpCodes.CONFIG_APPKEY_ADD),
        APP_KEY_STATUS(ConfigMessageOpCodes.CONFIG_APPKEY_STATUS),
        CONFIG_MODEL_APP_BIND(ConfigMessageOpCodes.CONFIG_MODEL_APP_BIND),
        CONFIG_MODEL_APP_STATUS(ConfigMessageOpCodes.CONFIG_MODEL_APP_STATUS),
        CONFIG_MODEL_PUBLICATION_SET(ConfigMessageOpCodes.CONFIG_MODEL_PUBLICATION_SET),
        CONFIG_MODEL_PUBLICATION_STATUS(ConfigMessageOpCodes.CONFIG_MODEL_PUBLICATION_STATUS),
        CONFIG_MODEL_SUBSCRIPTION_ADD(ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_ADD),
        CONFIG_MODEL_SUBSCRIPTION_DELETE(ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_DELETE),
        CONFIG_MODEL_SUBSCRIPTION_STATUS(ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_STATUS),
        CONFIG_NODE_RESET(ConfigMessageOpCodes.CONFIG_NODE_RESET),
        CONFIG_NODE_RESET_STATUS(ConfigMessageOpCodes.CONFIG_NODE_RESET_STATUS),

        //Application message states
        GENERIC_ON_OFF_GET(ApplicationMessageOpCodes.GENERIC_ON_OFF_GET),
        GENERIC_ON_OFF_SET(ApplicationMessageOpCodes.GENERIC_ON_OFF_SET),
        GENERIC_ON_OFF_SET_UNACKNOWLEDGED(ApplicationMessageOpCodes.GENERIC_ON_OFF_SET_UNACKNOWLEDGED),
        GENERIC_ON_OFF_STATUS(ApplicationMessageOpCodes.GENERIC_ON_OFF_STATUS),
        //config relay
        CONFIG_RELAY_GET(ApplicationMessageOpCodes.CONFIG_RELAY_GET),
        CONFIG_RELAY_SET(ApplicationMessageOpCodes.CONFIG_RELAY_SET),
        CONFIG_RELAY_STATUS(ApplicationMessageOpCodes.CONFIG_RELAY_STATUS),

        //config network transmit
        CONFIG_NETWORK_TRANSMIT_GET(ApplicationMessageOpCodes.CONFIG_NETWORK_TRANSMIT_GET),
        CONFIG_NETWORK_TRANSMIT_SET(ApplicationMessageOpCodes.CONFIG_NETWORK_TRANSMIT_SET),
        CONFIG_NETWORK_TRANSMIT_STATUS(ApplicationMessageOpCodes.CONFIG_NETWORK_TRANSMIT_STATUS),

        //config default ttl
        CONFIG_DEFAULT_TTL_GET(ApplicationMessageOpCodes.CONFIG_DEFAULT_TTL_GET),
        CONFIG_DEFAULT_TTL_SET(ApplicationMessageOpCodes.CONFIG_DEFAULT_TTL_SET),
        CONFIG_DEFAULT_TTL_STATUS(ApplicationMessageOpCodes.CONFIG_DEFAULT_TTL_STATUS),

        //config gatt proxy
        CONFIG_GATT_PROXY_GET(ApplicationMessageOpCodes.CONFIG_GATT_PROXY_GET),
        CONFIG_GATT_PROXY_SET(ApplicationMessageOpCodes.CONFIG_GATT_PROXY_SET),
        CONFIG_GATT_PROXY_STATUS(ApplicationMessageOpCodes.CONFIG_GATT_PROXY_STATUS);

        private int state;

        MessageState(final int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }
    }
}
