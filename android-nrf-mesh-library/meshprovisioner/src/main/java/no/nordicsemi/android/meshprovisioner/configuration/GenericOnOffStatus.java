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
import no.nordicsemi.android.meshprovisioner.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

public final class GenericOnOffStatus extends GenericMessageState {

    private static final String TAG = GenericOnOffStatus.class.getSimpleName();
    private static final int GENERIC_ON_OFF_STATE_ON = 0x01;
    private boolean mPresentOn;
    private Boolean mTargetOn;
    private int mRemainingTime;

    public GenericOnOffStatus(Context context,
                              final ProvisionedMeshNode unprovisionedMeshNode,
                              final InternalMeshMsgHandlerCallbacks callbacks,
                              final MeshModel meshModel,
                              final int appKeyIndex) {
        super(context, unprovisionedMeshNode, callbacks);
        this.mMeshModel = meshModel;
        this.mAppKeyIndex = appKeyIndex;
    }

    GenericOnOffStatus(Context context,
                       final ProvisionedMeshNode unprovisionedMeshNode,
                       final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, unprovisionedMeshNode, callbacks);
    }

    @Override
    public MessageState getState() {
        return MessageState.GENERIC_ON_OFF_STATUS_STATE;
    }

    public final boolean parseMeshPdu(final byte[] pdu) {
        final Message message = mMeshTransport.parsePdu(mSrc, pdu);
        if (message != null) {
            if (message instanceof AccessMessage) {
                final AccessMessage accessMessage = (AccessMessage) message;
                final byte[] accessPayload = accessMessage.getAccessPdu();
                final int opCodeLength = ((accessPayload[0] >> 7) & 0x01) + 1;

                final short opcode;
                if(opCodeLength == 2) {
                    opcode = (short)accessMessage.getOpCode();
                } else {
                    opcode = (short) accessMessage.getOpCode();
                }

                if (opcode == ApplicationMessageOpCodes.GENERIC_ON_OFF_STATUS) {
                    parseGenericOnOffStatusMessage((AccessMessage)message);
                    return true;
                } else {
                    mMeshStatusCallbacks.onUnknownPduReceived(mProvisionedMeshNode);
                }
            } else {
                parseControlMessage((ControlMessage) message, mPayloads.size());
            }
        } else {
            Log.v(TAG, "Message reassembly may not be complete yet");
        }
        return false;
    }

    /**
     * Parses the contents of the Generic OnOff Status access message
     * @param message
     */
    protected final void parseGenericOnOffStatusMessage(final AccessMessage message) throws IllegalArgumentException{
        if(message == null)
            throw  new IllegalArgumentException("Access message cannot be null!");

        Log.v(TAG, "Received generic on off status");
        final ByteBuffer buffer = ByteBuffer.wrap(message.getParameters()).order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(0);
        mPresentOn = buffer.get() == GENERIC_ON_OFF_STATE_ON;
        Log.v(TAG, "Present on: " + mPresentOn);
        int transitionSteps = 0;
        int transitionResolution = 0;
        if(buffer.limit() > 1) {
            mTargetOn = buffer.get() == GENERIC_ON_OFF_STATE_ON;
            mRemainingTime = buffer.get() & 0xFF;
            Log.v(TAG, "Target on: " + mTargetOn);
            transitionSteps = (mRemainingTime & 0x3F);
            Log.v(TAG, "Remaining time, transition number of steps: " + transitionSteps);
            transitionResolution = (mRemainingTime >> 6);
            Log.v(TAG, "Remaining time, transition number of step resolution: " + transitionResolution);
            Log.v(TAG, "Remaining time: " + MeshParserUtils.getRemainingTime(mRemainingTime));
        }
        mInternalTransportCallbacks.updateMeshNode(mProvisionedMeshNode);
        mMeshStatusCallbacks.onGenericOnOffStatusReceived(mProvisionedMeshNode, mPresentOn, mTargetOn, transitionSteps, transitionResolution);
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        final ControlMessage message = mMeshTransport.createSegmentBlockAcknowledgementMessage(controlMessage);
        Log.v(TAG, "Sending acknowledgement: " + MeshParserUtils.bytesToHex(message.getNetworkPdu().get(0), false));
        mInternalTransportCallbacks.sendPdu(mProvisionedMeshNode, message.getNetworkPdu().get(0));
        mMeshStatusCallbacks.onBlockAcknowledgementSent(mProvisionedMeshNode);
    }

}
