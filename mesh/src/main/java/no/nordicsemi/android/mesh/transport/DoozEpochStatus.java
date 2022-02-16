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

import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.unsignedToSigned;

/**
 * To be used as a wrapper class to create DoozEpochStatus message.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class DoozEpochStatus extends ApplicationStatusMessage implements Parcelable {

    private static final String TAG = DoozEpochStatus.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.DOOZ_EPOCH_STATUS;

    private int tId;
    private int mPacked;
    private int mEpoch;
    private int mCorrelation;
    private Integer mExtra;

    private static final Creator<DoozEpochStatus> CREATOR = new Creator<DoozEpochStatus>() {
        @Override
        public DoozEpochStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            //noinspection ConstantConditions
            return new DoozEpochStatus(message);
        }

        @Override
        public DoozEpochStatus[] newArray(int size) {
            return new DoozEpochStatus[size];
        }
    };

    /**
     * Constructs DoozEpochStatus message
     * @param message access message
     */
    public DoozEpochStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mMessage = message;
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {
        Log.v(TAG, "Received DooZ Epoch Status from: " + MeshAddress.formatAddress(mMessage.getSrc(), true));
        final ByteBuffer buffer = ByteBuffer.wrap(mParameters).order(ByteOrder.LITTLE_ENDIAN);
        tId = (int) (buffer.get());
        mPacked = (int) (buffer.getShort());
        Log.v(TAG, "mPacked: " + mPacked + " (" + Integer.toString(mPacked, 2) + ")");
        mEpoch = buffer.getInt();
        Log.v(TAG, "mEpoch: " + mEpoch);
        mCorrelation = buffer.getInt();
        Log.v(TAG, "mCorrelation: " + mCorrelation);
        mExtra = 0;
        Log.v(TAG, "mExtra: " + mExtra);
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    /**
     * Returns the packed data
     *
     * @return mPacked
     */
    public final int getPacked() {
        return mPacked;
    }

    /**
     * Returns the epoch value
     *
     * @return mEpoch
     */
    public int getEpoch() {
        return mEpoch;
    }

    /**
     * Returns the correlation value.
     *
     * @return correlation
     */
    public int getCorrelation() {
        return mCorrelation;
    }

    /**
     * Returns the extra data.
     *
     * @return mExtra
     */
    public Integer getExtra() {
        return mExtra;
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
}