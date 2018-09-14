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
import no.nordicsemi.android.meshprovisioner.utils.ConfigModelPublicationSetParams;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

public class ConfigModelPublicationSet extends ConfigMessageState {

    private static final String TAG = ConfigModelPublicationSet.class.getSimpleName();

    private static final int SIG_MODEL_PUBLISH_SET_PARAMS_LENGTH = 11;
    private static final int VENDOR_MODEL_PUBLISH_SET_PARAMS_LENGTH = 13;

    private final int aszmic;
    private final byte[] elementAddress;
    private final byte[] publishAddress;
    private final int appKeyIndex;
    private final int credentialFlag;
    private final int publishTtl;
    private final int publicationSteps;
    private int publicationResolution;
    private final int publishRetransmitCount;
    private final int publishRetransmitIntervalSteps;
    private final int mModelIdentifier;

    public ConfigModelPublicationSet(final Context context, final ConfigModelPublicationSetParams configModelPublicationParams,
                              final InternalMeshMsgHandlerCallbacks callbacks) {
        super(context, configModelPublicationParams.getMeshNode(), callbacks);
        this.aszmic = configModelPublicationParams.getAszmic();
        this.elementAddress = configModelPublicationParams.getElementAddress();
        this.publishAddress = configModelPublicationParams.getPublishAddress();
        this.mModelIdentifier = configModelPublicationParams.getModelIdentifier();
        this.appKeyIndex = configModelPublicationParams.getAppKeyIndex();
        this.credentialFlag = configModelPublicationParams.getCredentialFlag() ? 1 : 0;
        this.publishTtl = configModelPublicationParams.getPublishTtl();
        this.publicationSteps = configModelPublicationParams.getPublicationSteps();
        this.publicationResolution = configModelPublicationParams.getPublicationResolution();
        this.publishRetransmitCount = configModelPublicationParams.getPublishRetransmitCount();
        this.publishRetransmitIntervalSteps = configModelPublicationParams.getPublishRetransmitIntervalSteps();
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.CONFIG_MODEL_PUBLICATION_SET_STATE;
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
    private void createAccessMessage() throws IllegalArgumentException {
        ByteBuffer paramsBuffer;
        byte[] parameters;
        final byte[] applicationKeyIndex = MeshParserUtils.addKeyIndexPadding(appKeyIndex);

        final int rfu = 0; // We ignore the rfu here
        final int octet5 = ((applicationKeyIndex[0] << 4)) | (credentialFlag);
        final int octet8 = (publishRetransmitCount << 5) | (publishRetransmitIntervalSteps & 0x1F);
        //We check if the model identifier value is within the range of a 16-bit value here. If it is then it is a sigmodel
        if (mModelIdentifier >= Short.MIN_VALUE && mModelIdentifier <= Short.MAX_VALUE) {
            paramsBuffer = ByteBuffer.allocate(SIG_MODEL_PUBLISH_SET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.put(elementAddress[1]);
            paramsBuffer.put(elementAddress[0]);
            paramsBuffer.put(publishAddress[1]);
            paramsBuffer.put(publishAddress[0]);
            paramsBuffer.put(applicationKeyIndex[1]);
            paramsBuffer.put((byte) octet5);
            paramsBuffer.put((byte) publishTtl);
            paramsBuffer.put((byte) (publicationSteps | publicationResolution));
            paramsBuffer.put((byte) octet8);
            paramsBuffer.putShort((short) mModelIdentifier);
            parameters = paramsBuffer.array();
        } else {
            paramsBuffer = ByteBuffer.allocate(VENDOR_MODEL_PUBLISH_SET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.put(elementAddress[1]);
            paramsBuffer.put(elementAddress[0]);
            paramsBuffer.put(publishAddress[1]);
            paramsBuffer.put(publishAddress[0]);
            paramsBuffer.put(applicationKeyIndex[1]);
            paramsBuffer.put((byte) octet5);
            paramsBuffer.put((byte) publishTtl);
            paramsBuffer.put((byte) (publicationSteps | publicationResolution));
            paramsBuffer.put((byte) octet8);
            final byte[] modelIdentifier = new byte[]{(byte) ((mModelIdentifier >> 24) & 0xFF), (byte) ((mModelIdentifier >> 16) & 0xFF), (byte) ((mModelIdentifier >> 8) & 0xFF), (byte) (mModelIdentifier & 0xFF)};
            paramsBuffer.put(modelIdentifier[1]);
            paramsBuffer.put(modelIdentifier[0]);
            paramsBuffer.put(modelIdentifier[3]);
            paramsBuffer.put(modelIdentifier[2]);
            parameters = paramsBuffer.array();
        }

        final byte[] key = mProvisionedMeshNode.getDeviceKey();
        final int akf = 0;
        final int aid = 0b000;
        final int aszmic = 0;
        message = mMeshTransport.createMeshMessage(mProvisionedMeshNode, mSrc, key, akf, aid, aszmic, ConfigMessageOpCodes.CONFIG_MODEL_PUBLICATION_SET, parameters);
        mPayloads.putAll(message.getNetworkPdu());
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending config model publication set");
        super.executeSend();

        if (!mPayloads.isEmpty()) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onPublicationSetSent(mProvisionedMeshNode);
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
