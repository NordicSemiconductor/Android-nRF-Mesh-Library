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

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshConfigurationStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.messages.ControlMessage;
import no.nordicsemi.android.meshprovisioner.opcodes.ConfigMessageOpCodes;

public class ConfigCompositionDataGet extends ConfigMessage {

    private static final String TAG = ConfigCompositionDataGet.class.getSimpleName();
    private int mAszmic;
    private int akf = 0;
    private int aid = 0;

    public ConfigCompositionDataGet(final Context context, final ProvisionedMeshNode provisionedMeshNode, final int aszmic, final InternalTransportCallbacks internalTransportCallbacks,
                                    final MeshConfigurationStatusCallbacks meshConfigurationStatusCallbacks) {
        super(context, provisionedMeshNode);
        this.mAszmic = aszmic == 1 ? 1 : 0;
        this.mInternalTransportCallbacks = internalTransportCallbacks;
        this.mConfigStatusCallbacks = meshConfigurationStatusCallbacks;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.COMPOSITION_DATA_GET;
    }

    @Override
    protected void parseMessage(final byte[] pdu) {
        //Do nothing here
    }


    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        accessMessage = mMeshTransport.createMeshMessage(mProvisionedMeshNode, mSrc, mProvisionedMeshNode.getDeviceKey(),
                akf, aid, mAszmic, ConfigMessageOpCodes.CONFIG_COMPOSITION_DATA_GET,
                new byte[]{(byte) 0xFF});
        mPayloads.putAll(accessMessage.getNetworkPdu());

    }

    /**
     * Starts sending the mesh pdu
     */
    public void executeSend() {
        Log.v(TAG, "Sending composition data get");
        if (!mPayloads.isEmpty()) {
            for (int i = 0; i < mPayloads.size(); i++) {
                mInternalTransportCallbacks.sendPdu(mProvisionedMeshNode, mPayloads.get(i));
            }

            if (mConfigStatusCallbacks != null)
                mConfigStatusCallbacks.onGetCompositionDataSent(mProvisionedMeshNode);
        }
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        //We don't send acks here
    }

    /**
     * Returns the source address of the message i.e. where it originated from
     *
     * @return source address
     */
    public byte[] getSrc() {
        return mSrc;
    }
}
