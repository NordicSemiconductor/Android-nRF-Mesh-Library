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
import no.nordicsemi.android.meshprovisioner.messages.ConfigCompositionDataGet;
import no.nordicsemi.android.meshprovisioner.messages.ConfigCompositionDataStatus;
import no.nordicsemi.android.meshprovisioner.messagetypes.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messagetypes.ControlMessage;
import no.nordicsemi.android.meshprovisioner.messagetypes.Message;
import no.nordicsemi.android.meshprovisioner.opcodes.ConfigMessageOpCodes;

public class ConfigCompositionDataGetState extends ConfigMessageState {

    private static final String TAG = ConfigCompositionDataGetState.class.getSimpleName();
    private final ConfigCompositionDataGet mConfigCompositionDataGet;

    public ConfigCompositionDataGetState(@NonNull final Context context,
                                         @NonNull final ConfigCompositionDataGet compositionDataGet,
                                         @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        super(context, compositionDataGet.getMeshNode(), callbacks);
        this.mConfigCompositionDataGet = compositionDataGet;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.COMPOSITION_DATA_GET_STATE;
    }

    @Override
    public boolean parseMeshPdu(final byte[] pdu) {
        final Message message = mMeshTransport.parsePdu(mSrc, pdu);
        if (message != null) {
            if (message instanceof AccessMessage) {
                final AccessMessage accessMessage = ((AccessMessage) message);
                final int opcode = accessMessage.getOpCode();
                if (opcode == ConfigMessageOpCodes.CONFIG_COMPOSITION_DATA_STATUS) {
                    Log.v(TAG, "Received composition data status");
                    final ConfigCompositionDataStatus compositionDataStatus = new ConfigCompositionDataStatus(mNode, (AccessMessage) message);
                    mNode.setCompositionData(compositionDataStatus);
                    //TODO composition data get state
                    mMeshStatusCallbacks.onCompositionDataStatusReceived(mNode);
                    mInternalTransportCallbacks.updateMeshNode(mNode);
                    return true;

                } else {
                    parseControlMessage((ControlMessage) message, mPayloads.size());
                    return true;
                }
            } else {
                Log.v(TAG, "Message reassembly may not be complete yet");
            }
            return false;
        }
        return false;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final int akf = mConfigCompositionDataGet.getAkf();
        final int aid = mConfigCompositionDataGet.getAid();
        final int aszmic = mConfigCompositionDataGet.getAszmic();
        final int opCode = mConfigCompositionDataGet.getOpCode();
        final Message message = mMeshTransport.createMeshMessage(mNode, mSrc, mNode.getDeviceKey(), akf, aid, aszmic, opCode, mConfigCompositionDataGet.getParameters());
        mPayloads.putAll(message.getNetworkPdu());
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending composition data get");
        super.executeSend();
        if (!mPayloads.isEmpty()) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onGetCompositionDataSent(mNode);
        }
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        //We don't send acks here
    }
}
