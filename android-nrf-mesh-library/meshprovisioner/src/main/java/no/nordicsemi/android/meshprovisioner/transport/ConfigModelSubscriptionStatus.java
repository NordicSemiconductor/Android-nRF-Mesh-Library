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

package no.nordicsemi.android.meshprovisioner.transport;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.meshprovisioner.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * To be used as a wrapper class for when creating the ConfigModelSubscriptionStatus Message.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ConfigModelSubscriptionStatus extends ConfigStatusMessage implements Parcelable {

    private static final String TAG = ConfigModelSubscriptionStatus.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_STATUS;
    private static final int CONFIG_MODEL_PUBLICATION_STATUS_SIG_MODEL_PDU_LENGTH = 7;
    private static final int CONFIG_MODEL_APP_BIND_STATUS_VENDOR_MODEL_PDU_LENGTH = 9;
    private static final Creator<ConfigModelSubscriptionStatus> CREATOR = new Creator<ConfigModelSubscriptionStatus>() {
        @Override
        public ConfigModelSubscriptionStatus createFromParcel(Parcel in) {
            final AccessMessage message = (AccessMessage) in.readValue(AccessMessage.class.getClassLoader());
            return new ConfigModelSubscriptionStatus(message);
        }

        @Override
        public ConfigModelSubscriptionStatus[] newArray(int size) {
            return new ConfigModelSubscriptionStatus[size];
        }
    };
    private int mElementAddress;
    private int mModelIdentifier;
    private byte[] mSubscriptionAddress;

    /**
     * Constructs the ConfigModelSubscriptionStatus mMessage.
     *
     * @param message Access Message
     */
    public ConfigModelSubscriptionStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    final void parseStatusParameters() {
        final AccessMessage message = (AccessMessage) mMessage;
        mStatusCode = mParameters[0];
        mStatusCodeName = getStatusCodeName(mStatusCode);
        final byte[] elementAddress = new byte[]{mParameters[2], mParameters[1]};
        mElementAddress = ByteBuffer.wrap(elementAddress).order(ByteOrder.BIG_ENDIAN).getShort();

        mSubscriptionAddress = new byte[]{mParameters[4], mParameters[3]};

        final byte[] modelIdentifier;
        if (mParameters.length == CONFIG_MODEL_PUBLICATION_STATUS_SIG_MODEL_PDU_LENGTH) {
            modelIdentifier = new byte[]{mParameters[6], mParameters[5]};
            mModelIdentifier = ByteBuffer.wrap(modelIdentifier).order(ByteOrder.BIG_ENDIAN).getShort();
        } else {
            modelIdentifier = new byte[]{mParameters[6], mParameters[5], mParameters[8], mParameters[7]};
            mModelIdentifier = ByteBuffer.wrap(modelIdentifier).order(ByteOrder.BIG_ENDIAN).getInt();
        }

        Log.v(TAG, "Status code: " + mStatusCode);
        Log.v(TAG, "Status message: " + mStatusCodeName);
        Log.v(TAG, "Element Address: " + MeshParserUtils.bytesToHex(elementAddress, false));
        Log.v(TAG, "Subscription Address: " + MeshParserUtils.bytesToHex(mSubscriptionAddress, false));
        Log.v(TAG, "Model Identifier: " + MeshParserUtils.bytesToHex(modelIdentifier, false));
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    /**
     * Returns the element address that the key was bound to
     *
     * @return element address
     */
    public int getElementAddress() {
        return mElementAddress;
    }

    /**
     * Returns the subscription address.
     *
     * @return subscription address
     */
    public byte[] getSubscriptionAddress() {
        return mSubscriptionAddress;
    }

    /**
     * Returns the model identifier
     *
     * @return 16-bit sig model identifier or 32-bit vendor model identifier
     */
    public final int getModelIdentifier() {
        return mModelIdentifier;
    }

    /**
     * Returns if the message was successful
     *
     * @return true if the message was successful or false otherwise
     */
    public final boolean isSuccessful() {
        return mStatusCode == 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeValue(mMessage);
    }
}
