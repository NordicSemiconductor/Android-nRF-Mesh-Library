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
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.mesh.utils.CompositionDataParser;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * Creates the ConfigAppKeyList Message.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ConfigVendorModelAppList extends ConfigStatusMessage implements Parcelable {

    private static final String TAG = ConfigVendorModelAppList.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_VENDOR_MODEL_APP_LIST;
    private int mElementAddress;
    private int mModelIdentifier;
    private final List<Integer> mKeyIndexes;

    public static final Creator<ConfigVendorModelAppList> CREATOR = new Creator<ConfigVendorModelAppList>() {
        @Override
        public ConfigVendorModelAppList createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            //noinspection ConstantConditions
            return new ConfigVendorModelAppList(message);
        }

        @Override
        public ConfigVendorModelAppList[] newArray(int size) {
            return new ConfigVendorModelAppList[size];
        }
    };

    /**
     * Constructs the ConfigNetKeyList mMessage.
     *
     * @param message Access Message
     */
    public ConfigVendorModelAppList(@NonNull final AccessMessage message) {
        super(message);
        mKeyIndexes = new ArrayList<>();
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    final void parseStatusParameters() {
        mStatusCode = mParameters[0];
        mStatusCodeName = getStatusCodeName(mStatusCode);
        mElementAddress = MeshParserUtils.unsignedBytesToInt(mParameters[1], mParameters[2]);
        final byte[] modelIdentifier = new byte[]{mParameters[4], mParameters[3], mParameters[6], mParameters[5]};
        mModelIdentifier = ByteBuffer.wrap(modelIdentifier).order(ByteOrder.BIG_ENDIAN).getInt();

        Log.v(TAG, "Status code: " + mStatusCode);
        Log.v(TAG, "Status message: " + mStatusCodeName);
        Log.v(TAG, "Element address: " + MeshAddress.formatAddress(mElementAddress, false));
        Log.v(TAG, "Model identifier: " + CompositionDataParser.formatModelIdentifier(mModelIdentifier, false));

        mKeyIndexes.addAll(decode(mParameters.length, 7));
        for (Integer keyIndex : mKeyIndexes) {
            Log.v(TAG, "AppKey Index: " + Integer.toHexString(keyIndex));
        }
    }

    @Override
    public final int getOpCode() {
        return OP_CODE;
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
        final AccessMessage message = (AccessMessage) mMessage;
        dest.writeParcelable(message, flags);
    }

    /**
     * Returns the element address
     */
    public int getElementAddress() {
        return mElementAddress;
    }

    /**
     * Returns the model identifier
     */
    public int getModelIdentifier() {
        return mModelIdentifier;
    }

    /**
     * Returns the bound app key indexes
     */
    public List<Integer> getKeyIndexes() {
        return mKeyIndexes;
    }
}
