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

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * To be used as a wrapper class for when creating the VendorModelMessageStatus Message.
 */
@SuppressWarnings({"WeakerAccess"})
public final class VendorModelMessageStatus extends ApplicationStatusMessage implements Parcelable {

    private static final String TAG = VendorModelMessageStatus.class.getSimpleName();
    private final int mModelIdentifier;

    public static final Creator<VendorModelMessageStatus> CREATOR = new Creator<VendorModelMessageStatus>() {
        @Override
        public VendorModelMessageStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            final int modelIdentifier = in.readInt();
            return new VendorModelMessageStatus(message, modelIdentifier);
        }

        @Override
        public VendorModelMessageStatus[] newArray(int size) {
            return new VendorModelMessageStatus[size];
        }
    };

    /**
     * Constructs the VendorModelMessageStatus mMessage.
     *
     * @param message         Access Message
     * @param modelIdentifier model identifier
     */
    public VendorModelMessageStatus(@NonNull final AccessMessage message, final int modelIdentifier) {
        super(message);
        this.mParameters = message.getParameters();
        this.mModelIdentifier = modelIdentifier;
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {
        Log.v(TAG, "Received Vendor model status: " + MeshParserUtils.bytesToHex(mParameters, false));
    }

    @Override
    public int getOpCode() {
        return mMessage.getOpCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        final AccessMessage message = (AccessMessage) mMessage;
        dest.writeParcelable(message, flags);
        dest.writeInt(mModelIdentifier);
    }

    public final byte[] getAccessPayload() {
        return ((AccessMessage) mMessage).getAccessPdu();
    }

    /**
     * Returns the model identifier of model the for this message
     */
    public int getModelIdentifier() {
        return mModelIdentifier;
    }
}
