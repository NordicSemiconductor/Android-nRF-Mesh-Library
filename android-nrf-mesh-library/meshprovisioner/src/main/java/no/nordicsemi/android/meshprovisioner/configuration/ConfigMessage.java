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
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshConfigurationStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.control.BlockAcknowledgementMessage;
import no.nordicsemi.android.meshprovisioner.control.TransportControlMessage;
import no.nordicsemi.android.meshprovisioner.messages.ControlMessage;
import no.nordicsemi.android.meshprovisioner.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.transport.LowerTransportLayerCallbacks;
import no.nordicsemi.android.meshprovisioner.utils.InterfaceAdapter;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

public abstract class ConfigMessage implements LowerTransportLayerCallbacks {

    private static final String TAG = ConfigCompositionDataStatus.class.getSimpleName();
    private static final String PROVISIONED_NODES_FILE = "PROVISIONED_FILES";
    protected final Context mContext;
    protected final ProvisionedMeshNode mProvisionedMeshNode;
    final MeshTransport mMeshTransport;
    final Map<Integer, byte[]> mPayloads = new HashMap<>();
    final byte[] mSrc;
    protected InternalTransportCallbacks mInternalTransportCallbacks;
    MeshConfigurationStatusCallbacks mConfigStatusCallbacks;
    private Gson mGson;

    public ConfigMessage(final Context context, final ProvisionedMeshNode unprovisionedMeshNode) {
        this.mContext = context;
        this.mProvisionedMeshNode = unprovisionedMeshNode;
        this.mSrc = mProvisionedMeshNode.getConfigurationSrc();
        this.mMeshTransport = new MeshTransport(context, unprovisionedMeshNode);
        this.mMeshTransport.setCallbacks(this);
        initGson();
    }


    private void initGson() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.enableComplexMapKeySerialization();
        gsonBuilder.registerTypeAdapter(MeshModel.class, new InterfaceAdapter<MeshModel>());
        gsonBuilder.setPrettyPrinting();
        mGson = gsonBuilder.create();
    }

    public abstract ConfigMessageState getState();

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

    /**
     * Serialize and save provisioned nodes
     */
    final void updateSavedProvisionedNode(final Context context, final ProvisionedMeshNode node) {
        SharedPreferences preferences = context.getSharedPreferences(PROVISIONED_NODES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        final String unicastAddress = MeshParserUtils.bytesToHex(node.getUnicastAddress(), true);
        final String provisionedNode = mGson.toJson(node);
        editor.putString(unicastAddress, provisionedNode);
        editor.commit();
    }

    public enum ConfigMessageState {
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
        CONFIG_MODEL_SUBSCRIPTION_STATUS(ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_STATUS);

        private int state;

        ConfigMessageState(final int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }
    }
}
