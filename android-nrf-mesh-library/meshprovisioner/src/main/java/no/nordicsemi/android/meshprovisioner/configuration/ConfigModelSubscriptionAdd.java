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
 * This class handles subscribing a model to subscription address.
 */
public final class ConfigModelSubscriptionAdd extends ConfigMessageState {

    private static final String TAG = ConfigModelSubscriptionAdd.class.getSimpleName();
    public static int SUBSCRIPTION_ADD = 0x00;
    private static final int SIG_MODEL_APP_KEY_BIND_PARAMS_LENGTH = 6;
    private static final int VENDOR_MODEL_APP_KEY_BIND_PARAMS_LENGTH = 8;

    private final int akf = 0;
    private final int aid = 0;

    private final int mAszmic;
    private final byte[] mElementAddress;
    private final int mModelIdentifier;
    private final byte[] mSubscriptionAddress;

    public ConfigModelSubscriptionAdd(final Context context,
                                      final ProvisionedMeshNode meshNode,
                                      final InternalMeshMsgHandlerCallbacks callbacks,
                                      final int aszmic,
                                      final byte[] elementAddress, final byte[] subscriptionAddress, final int modelIdentifier) {
        super(context, meshNode, callbacks);
        this.mAszmic = aszmic == 1 ? 1 : 0;
        this.mElementAddress = elementAddress;
        this.mModelIdentifier = modelIdentifier;
        this.mSubscriptionAddress = subscriptionAddress;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.CONFIG_MODEL_SUBSCRIPTION_ADD_STATE;
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
        ByteBuffer paramsBuffer;
        byte[] parameters;
        //We check if the model identifier value is within the range of a 16-bit value here. If it is then it is a sigmodel
        if (mModelIdentifier >= Short.MIN_VALUE && mModelIdentifier <= Short.MAX_VALUE) {
            paramsBuffer = ByteBuffer.allocate(SIG_MODEL_APP_KEY_BIND_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.put(mElementAddress[1]);
            paramsBuffer.put(mElementAddress[0]);
            paramsBuffer.put(mSubscriptionAddress[1]);
            paramsBuffer.put(mSubscriptionAddress[0]);
            paramsBuffer.putShort((short) mModelIdentifier);
            parameters = paramsBuffer.array();
        } else {
            paramsBuffer = ByteBuffer.allocate(VENDOR_MODEL_APP_KEY_BIND_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.put(mElementAddress[1]);
            paramsBuffer.put(mElementAddress[0]);
            paramsBuffer.put(mSubscriptionAddress[1]);
            paramsBuffer.put(mSubscriptionAddress[0]);
            final byte[] modelIdentifier = new byte[]{(byte) ((mModelIdentifier >> 24) & 0xFF), (byte) ((mModelIdentifier >> 16) & 0xFF), (byte) ((mModelIdentifier >> 8) & 0xFF), (byte) (mModelIdentifier & 0xFF)};
            paramsBuffer.put(modelIdentifier[1]);
            paramsBuffer.put(modelIdentifier[0]);
            paramsBuffer.put(modelIdentifier[3]);
            paramsBuffer.put(modelIdentifier[2]);
            parameters = paramsBuffer.array();
        }

        final byte[] key = mProvisionedMeshNode.getDeviceKey();
        message = mMeshTransport.createMeshMessage(mProvisionedMeshNode, mSrc, key, akf, aid, mAszmic, ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_ADD, parameters);
        mPayloads.putAll(message.getNetworkPdu());
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending config model subscription add");
        super.executeSend();

        if (!mPayloads.isEmpty()) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onSubscriptionAddSent(mProvisionedMeshNode);
        }
    }

    public void parseData(final byte[] pdu) {
        parseMeshPdu(pdu);
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        final ControlMessage message = mMeshTransport.createSegmentBlockAcknowledgementMessage(controlMessage);
        Log.v(TAG, "Sending acknowledgement: " + MeshParserUtils.bytesToHex(message.getNetworkPdu().get(0), false));
        mInternalTransportCallbacks.sendPdu(mProvisionedMeshNode, message.getNetworkPdu().get(0));
        mMeshStatusCallbacks.onBlockAcknowledgementSent(mProvisionedMeshNode);
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
