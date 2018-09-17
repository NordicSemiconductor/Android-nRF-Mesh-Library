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
import no.nordicsemi.android.meshprovisioner.R;
import no.nordicsemi.android.meshprovisioner.messages.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messages.ControlMessage;
import no.nordicsemi.android.meshprovisioner.messages.Message;
import no.nordicsemi.android.meshprovisioner.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

import static no.nordicsemi.android.meshprovisioner.configuration.ConfigAppKeyStatus.AppKeyStatuses.fromStatusCode;

public class ConfigAppKeyStatus extends ConfigMessageState {

    private static final String TAG = ConfigAppKeyStatus.class.getSimpleName();
    private String appKey;
    private int status;
    private boolean isSuccessful;
    private String statusMessage;
    private byte[] netKeyIndex;
    private byte[] appKeyIndex;

    public ConfigAppKeyStatus(final Context context, final ProvisionedMeshNode meshNode, final byte[] src, final String appKey, final InternalMeshMsgHandlerCallbacks callbacks) {
        super(context, meshNode, callbacks);
        this.appKey = appKey;
    }

    public static String parseStatusMessage(final Context context, final int status) {
        switch (fromStatusCode(status)) {
            case SUCCESS:
                return context.getString(R.string.status_success);
            case INVALID_ADDRESS:
                return context.getString(R.string.status_invalid_address);
            case INVALID_MODEL:
                return context.getString(R.string.status_invalid_model);
            case INVALID_APPKEY_INDEX:
                return context.getString(R.string.status_invalid_appkey_index);
            case INVALID_NETKEY_INDEX:
                return context.getString(R.string.status_invalid_netkey_index);
            case INSUFFICIENT_RESOURCES:
                return context.getString(R.string.status_insufficient_resources);
            case KEY_INDEX_ALREADY_STORED:
                return context.getString(R.string.status_key_index_already_stored);
            case INVALID_PUBLISH_PARAMETERS:
                return context.getString(R.string.status_invalid_publish_parameters);
            case NOT_A_SUBSCRIBE_MODEL:
                return context.getString(R.string.status_not_a_subscribe_model);
            case STORAGE_FAILURE:
                return context.getString(R.string.status_storage_failure);
            case FEATURE_NOT_SUPPORTED:
                return context.getString(R.string.status_feature_not_supported);
            case CANNOT_UPDATE:
                return context.getString(R.string.status_cannot_update);
            case CANNOT_REMOVE:
                return context.getString(R.string.status_cannot_remove);
            case CANNOT_BIND:
                return context.getString(R.string.status_cannot_bind);
            case TEMPORARILY_UNABLE_TO_CHANGE_STATE:
                return context.getString(R.string.status_temporarily_unable_to_change_state);
            case CANNOT_SET:
                return context.getString(R.string.status_cannot_set);
            case UNSPECIFIED_ERROR:
                return context.getString(R.string.status_unspecified_error);
            case INVALID_BINDING:
                return context.getString(R.string.status_success_invalid_binding);
            case RFU:
            default:
                return context.getString(R.string.app_key_status_rfu);
        }

    }

    @Override
    public MessageState getState() {
        return MessageState.APP_KEY_STATUS_STATE;
    }


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
                    Log.v(TAG, "Received config app key status");
                    final int offset = +2; //Ignoring the opcode and the parameter received
                    parseConfigAppKeyStatus(accessPayload, offset);
                    if (isSuccessful) {
                        mProvisionedMeshNode.setAddedAppKey(ByteBuffer.wrap(appKeyIndex).order(ByteOrder.BIG_ENDIAN).getShort(), appKey);
                    }
                    mMeshStatusCallbacks.onAppKeyStatusReceived(mProvisionedMeshNode, isSuccessful, status,
                            ByteBuffer.wrap(netKeyIndex).order(ByteOrder.BIG_ENDIAN).getShort(),
                            ByteBuffer.wrap(appKeyIndex).order(ByteOrder.BIG_ENDIAN).getShort());
                    mInternalTransportCallbacks.updateMeshNode(mProvisionedMeshNode);
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

    private void parseConfigAppKeyStatus(final byte[] accessPayload, final int offset) {
        status = accessPayload[offset];
        parseStatus(status);
        statusMessage = parseStatusMessage(mContext, status);
        netKeyIndex = new byte[]{(byte) (accessPayload[4] & 0x0F), accessPayload[3]};
        appKeyIndex = new byte[]{(byte) ((accessPayload[5] & 0xF0) >> 4), (byte) (accessPayload[5] << 4 | ((accessPayload[4] & 0xF0) >> 4))};
        Log.v(TAG, "Status: " + status);
        Log.v(TAG, "Status message: " + statusMessage);
        Log.v(TAG, "Net key index: " + MeshParserUtils.bytesToHex(netKeyIndex, false));
        Log.v(TAG, "App key index: " + MeshParserUtils.bytesToHex(appKeyIndex, false));
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        final ControlMessage message = mMeshTransport.createSegmentBlockAcknowledgementMessage(controlMessage);
        mInternalTransportCallbacks.sendPdu(mProvisionedMeshNode, message.getNetworkPdu().get(0));
        Log.v(TAG, "Sending acknowledgement: " + MeshParserUtils.bytesToHex(message.getNetworkPdu().get(0), false));
        mMeshStatusCallbacks.onBlockAcknowledgementSent(mProvisionedMeshNode);
    }

    private void parseStatus(final int status) {
        switch (fromStatusCode(status)) {
            case SUCCESS:
                isSuccessful = true;
                break;
        }
    }

    public int getStatus() {
        return status;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public enum AppKeyStatuses {
        SUCCESS(0x00),
        INVALID_ADDRESS(0x01),
        INVALID_MODEL(0x02),
        INVALID_APPKEY_INDEX(0x03),
        INVALID_NETKEY_INDEX(0x04),
        INSUFFICIENT_RESOURCES(0x05),
        KEY_INDEX_ALREADY_STORED(0x06),
        INVALID_PUBLISH_PARAMETERS(0x07),
        NOT_A_SUBSCRIBE_MODEL(0x08),
        STORAGE_FAILURE(0x09),
        FEATURE_NOT_SUPPORTED(0x0A),
        CANNOT_UPDATE(0x0B),
        CANNOT_REMOVE(0x0C),
        CANNOT_BIND(0x0D),
        TEMPORARILY_UNABLE_TO_CHANGE_STATE(0x0E),
        CANNOT_SET(0x0F),
        UNSPECIFIED_ERROR(0x10),
        INVALID_BINDING(0x11),
        RFU(0x12);

        private final int statusCode;

        AppKeyStatuses(final int statusCode) {
            this.statusCode = statusCode;
        }

        public static AppKeyStatuses fromStatusCode(final int statusCode) {
            for (AppKeyStatuses failureCode : values()) {
                if (failureCode.getStatusCode() == statusCode) {
                    return failureCode;
                }
            }
            throw new IllegalArgumentException("Enum not found in AppKeyStatus");
        }

        public final int getStatusCode() {
            return statusCode;
        }
    }
}
