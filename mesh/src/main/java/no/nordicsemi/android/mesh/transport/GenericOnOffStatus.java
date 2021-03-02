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

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * To be used as a wrapper class for when creating the GenericOnOffStatus Message.
 */
@SuppressWarnings({"WeakerAccess"})
public final class GenericOnOffStatus extends ApplicationStatusMessage implements Parcelable {

    private static final String TAG = GenericOnOffStatus.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.GENERIC_ON_OFF_STATUS;
    private static final int GENERIC_ON_OFF_STATE_ON = 0x01;
    private boolean mPresentOn;
    private Boolean mTargetOn;
    private int mRemainingTime;
    private int mTransitionSteps;
    private int mTransitionResolution;

    private static final Creator<GenericOnOffStatus> CREATOR = new Creator<GenericOnOffStatus>() {
        @Override
        public GenericOnOffStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            return new GenericOnOffStatus(message);
        }

        @Override
        public GenericOnOffStatus[] newArray(int size) {
            return new GenericOnOffStatus[size];
        }
    };

    /**
     * Constructs the GenericOnOffStatus mMessage.
     *
     * @param message Access Message
     */
    public GenericOnOffStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {
        Log.v(TAG, "Received generic on off status from: " + MeshAddress.formatAddress(mMessage.getSrc(), true));
        final ByteBuffer buffer = ByteBuffer.wrap(mParameters).order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(0);
        mPresentOn = buffer.get() == GENERIC_ON_OFF_STATE_ON;
        Log.v(TAG, "Present on: " + mPresentOn);
        if (buffer.limit() > 1) {
            mTargetOn = buffer.get() == GENERIC_ON_OFF_STATE_ON;
            mRemainingTime = buffer.get() & 0xFF;
            mTransitionSteps = (mRemainingTime & 0x3F);
            mTransitionResolution = (mRemainingTime >> 6);
            Log.v(TAG, "Target on: " + mTargetOn);
            Log.v(TAG, "Remaining time, transition number of steps: " + mTransitionSteps);
            Log.v(TAG, "Remaining time, transition number of step resolution: " + mTransitionResolution);
            Log.v(TAG, "Remaining time: " + MeshParserUtils.getRemainingTime(mRemainingTime));
        }
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    /**
     * Returns the present state of the GenericOnOffModel
     *
     * @return true if on and false other wise
     */
    public final boolean getPresentState() {
        return mPresentOn;
    }

    /**
     * Returns the target state of the GenericOnOffModel
     *
     * @return true if on and false other wise
     */
    public final Boolean getTargetState() {
        return mTargetOn;
    }

    /**
     * Returns the transition steps.
     *
     * @return transition steps
     */
    public int getTransitionSteps() {
        return mTransitionSteps;
    }

    /**
     * Returns the transition resolution.
     *
     * @return transition resolution
     */
    public int getTransitionResolution() {
        return mTransitionResolution;
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
