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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.meshprovisioner.InternalMeshMsgHandlerCallbacks;
import no.nordicsemi.android.meshprovisioner.messages.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messages.ControlMessage;
import no.nordicsemi.android.meshprovisioner.messages.Message;
import no.nordicsemi.android.meshprovisioner.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * This class handles adding a application keys to a specific a mesh node.
 */
public class ConfigAppKeyAdd extends ConfigMessageState {

    private final String TAG = ConfigAppKeyAdd.class.getSimpleName();

    private final int mAszmic;
    private final String mAppKey;
    private final int mAppKeyIndex;

    public ConfigAppKeyAdd(final Context context, final ProvisionedMeshNode unprovisionedMeshNode,
                           final InternalMeshMsgHandlerCallbacks callbacks, final int aszmic, final String appKey, final int appKeyIndex) {
        super(context, unprovisionedMeshNode, callbacks);
        this.mAszmic = aszmic == 1 ? 1 : 0;
        this.mAppKey = appKey;
        this.mAppKeyIndex = appKeyIndex;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.APP_KEY_ADD_STATE;
    }

    @Override
    protected boolean parseMeshPdu(final byte[] pdu) {
        final Message message = mMeshTransport.parsePdu(mSrc, pdu);
        if (message != null) {
            if (message instanceof AccessMessage) {
                final byte[] accessPayload = ((AccessMessage) message).getAccessPdu();
                Log.v(TAG, "Unexpected access message received: " + MeshParserUtils.bytesToHex(accessPayload, false));
            } else {
                parseControlMessage((ControlMessage) message, mPayloads.size());
                return true;
            }
        } else {
            Log.v(TAG, "Message reassembly may not be complete yet");
        }
        return false;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final byte[] networkKeyIndex = mProvisionedMeshNode.getKeyIndex();
        final byte[] appKeyBytes = MeshParserUtils.toByteArray(mAppKey);
        final byte[] applicationKeyIndex = MeshParserUtils.addKeyIndexPadding(mAppKeyIndex);

        final ByteBuffer paramsBuffer = ByteBuffer.allocate(19).order(ByteOrder.BIG_ENDIAN);
        paramsBuffer.put(networkKeyIndex[1]);
        paramsBuffer.put((byte) ((applicationKeyIndex[1] << 4) | networkKeyIndex[0] & 0x0F));
        paramsBuffer.put((byte) ((applicationKeyIndex[0] << 4) | applicationKeyIndex[1] >> 4));
        paramsBuffer.put(appKeyBytes);

        final byte[] parameters = paramsBuffer.array();

        final byte[] key = mProvisionedMeshNode.getDeviceKey();
        final int akf = 0;
        final int aid = 0;
        message = mMeshTransport.createMeshMessage(mProvisionedMeshNode, mSrc, key, akf, aid, mAszmic, ConfigMessageOpCodes.CONFIG_APPKEY_ADD, parameters);
        mPayloads.putAll(message.getNetworkPdu());
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending config app key add");
        super.executeSend();
        if (!mPayloads.isEmpty()) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onAppKeyAddSent(mProvisionedMeshNode);
        }
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        final ControlMessage message = mMeshTransport.createSegmentBlockAcknowledgementMessage(controlMessage);
        Log.v(TAG, "Sending acknowledgement: " + MeshParserUtils.bytesToHex(message.getNetworkPdu().get(0), false));
        mInternalTransportCallbacks.sendPdu(mProvisionedMeshNode, message.getNetworkPdu().get(0));
        mMeshStatusCallbacks.onBlockAcknowledgementSent(mProvisionedMeshNode);
    }

    /**
     * Returns the application key that sent in the app key add message
     *
     * @return app key
     */
    public String getAppKey() {
        return mAppKey;
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
