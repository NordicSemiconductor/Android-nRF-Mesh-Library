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

package no.nordicsemi.android.mesh.transport;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * To be used as a wrapper class for when creating the ConfigModelAppStatus Message.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ConfigModelAppStatus extends ConfigStatusMessage implements Parcelable {

    private static final String TAG = ConfigModelAppStatus.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_MODEL_APP_STATUS;
    private static final int CONFIG_MODEL_APP_BIND_STATUS_SIG_MODEL = 7;
    private static final int CONFIG_MODEL_APP_BIND_STATUS_VENDOR_MODEL = 9;
    private int mElementAddress;
    private int mAppKeyIndex;
    private int mModelIdentifier;

    private static final Creator<ConfigModelAppStatus> CREATOR = new Creator<ConfigModelAppStatus>() {
        @Override
        public ConfigModelAppStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            //noinspection ConstantConditions
            return new ConfigModelAppStatus(message);
        }

        @Override
        public ConfigModelAppStatus[] newArray(int size) {
            return new ConfigModelAppStatus[size];
        }
    };

    /**
     * Constructs the ConfigModelAppStatus mMessage.
     *
     * @param message Access Message
     */
    public ConfigModelAppStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    final void parseStatusParameters() {
        final AccessMessage message = (AccessMessage) mMessage;
        final ByteBuffer buffer = ByteBuffer.wrap(message.getParameters()).order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(0);
        mStatusCode = buffer.get();
        mStatusCodeName = getStatusCodeName(mStatusCode);
        mElementAddress = MeshParserUtils.unsignedBytesToInt(mParameters[1], mParameters[2]);
        final byte[] appKeyIndex = new byte[]{(byte) (mParameters[4] & 0x0F), mParameters[3]};
        mAppKeyIndex = ByteBuffer.wrap(appKeyIndex).order(ByteOrder.BIG_ENDIAN).getShort();

        final byte[] modelIdentifier;
        if (mParameters.length == CONFIG_MODEL_APP_BIND_STATUS_SIG_MODEL) {
            mModelIdentifier = MeshParserUtils.unsignedBytesToInt(mParameters[5], mParameters[6]);
        } else {
            modelIdentifier = new byte[]{mParameters[6], mParameters[5], mParameters[8], mParameters[7]};
            mModelIdentifier = ByteBuffer.wrap(modelIdentifier).order(ByteOrder.BIG_ENDIAN).getInt();
        }

        Log.v(TAG, "Status code: " + mStatusCode);
        Log.v(TAG, "Status message: " + mStatusCodeName);
        Log.v(TAG, "Element address: " + MeshAddress.formatAddress(mElementAddress, false));
        Log.v(TAG, "App key index: " + MeshParserUtils.bytesToHex(appKeyIndex, false));
        Log.v(TAG, "Model identifier: " + Integer.toHexString(mModelIdentifier));
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
     * Returns the global app key index.
     *
     * @return appkey index
     */
    public final int getAppKeyIndex() {
        return mAppKeyIndex;
    }

    /**
     * Returns the model identifier
     *
     * @return 16-bit sig model identifier or 32-bit vendor model identifier
     */
    public final int getModelIdentifier() {
        return mModelIdentifier;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        final AccessMessage message = (AccessMessage) mMessage;
        dest.writeParcelable(message, flags);
    }

    /**
     * Returns if the message was successful or not.
     *
     * @return true if succesful or false otherwise
     */
    public boolean isSuccessful() {
        return mStatusCode == 0x00;
    }
}
