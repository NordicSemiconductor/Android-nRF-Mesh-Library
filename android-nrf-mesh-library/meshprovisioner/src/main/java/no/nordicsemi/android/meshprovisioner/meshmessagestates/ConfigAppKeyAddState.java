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

package no.nordicsemi.android.meshprovisioner.meshmessagestates;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.InternalMeshMsgHandlerCallbacks;
import no.nordicsemi.android.meshprovisioner.messages.ConfigAppKeyAdd;
import no.nordicsemi.android.meshprovisioner.messages.ConfigAppKeyStatus;
import no.nordicsemi.android.meshprovisioner.messagetypes.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messagetypes.ControlMessage;
import no.nordicsemi.android.meshprovisioner.messagetypes.Message;
import no.nordicsemi.android.meshprovisioner.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * This class handles adding a application keys to a specific a mesh node.
 */
public class ConfigAppKeyAddState extends ConfigMessageState {

    private final String TAG = ConfigAppKeyAddState.class.getSimpleName();
    private final ConfigAppKeyAdd mConfigAppKeyAdd;

    public ConfigAppKeyAddState(@NonNull final Context context, @NonNull ConfigAppKeyAdd appKeyAdd, @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        super(context, appKeyAdd.getMeshNode(), callbacks);
        this.mConfigAppKeyAdd = appKeyAdd;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.APP_KEY_ADD_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final byte[] key = mNode.getDeviceKey();
        final int akf = mConfigAppKeyAdd.getAkf();
        final int aid = mConfigAppKeyAdd.getAid();
        final int opCode = mConfigAppKeyAdd.getOpCode();
        final byte[] parameters = mConfigAppKeyAdd.getParameters();
        message = mMeshTransport.createMeshMessage(mNode, mSrc, key, akf, aid, mConfigAppKeyAdd.getAszmic(), opCode, parameters);
        mPayloads.putAll(message.getNetworkPdu());
    }

    @Override
    public final boolean parseMeshPdu(final byte[] pdu) {
        final Message message = mMeshTransport.parsePdu(mSrc, pdu);
        if (message != null) {
            if (message instanceof AccessMessage) {
                final byte[] accessPayload = ((AccessMessage) message).getAccessPdu();

                //MSB of the first octet defines the length of opcodes.
                //if MSB = 0 length is 1 and so forth
                final int opCodeLength = ((accessPayload[0] >> 7) & 0x01) + 1;

                final int opcode = MeshParserUtils.getOpCode(accessPayload, opCodeLength);
                if (opcode == ConfigMessageOpCodes.CONFIG_APPKEY_STATUS) {
                    final ConfigAppKeyStatus appKeyStatus = new ConfigAppKeyStatus(mNode, (AccessMessage) message);

                    if(appKeyStatus.isSuccessful()) {
                        mNode.setAddedAppKey(appKeyStatus.getAppKeyIndex(), MeshParserUtils.bytesToHex(mConfigAppKeyAdd.getAppKey(), false));
                    }

                    mInternalTransportCallbacks.updateMeshNode(mNode);
                    mMeshStatusCallbacks.onAppKeyStatusReceived(appKeyStatus);
                    return true;
                } else {
                    mMeshStatusCallbacks.onUnknownPduReceived(mNode);
                }
            } else {
                parseControlMessage((ControlMessage) message, mPayloads.size());
            }
        } else {
            Log.v(TAG, "Message reassembly may not be complete yet");
        }
        return false;
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending config app key add");
        super.executeSend();
        if (!mPayloads.isEmpty()) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onAppKeyAddSent(mNode);
        }
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        final ControlMessage message = mMeshTransport.createSegmentBlockAcknowledgementMessage(controlMessage);
        Log.v(TAG, "Sending acknowledgement: " + MeshParserUtils.bytesToHex(message.getNetworkPdu().get(0), false));
        mInternalTransportCallbacks.sendPdu(mNode, message.getNetworkPdu().get(0));
        mMeshStatusCallbacks.onBlockAcknowledgementSent(mNode);
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
