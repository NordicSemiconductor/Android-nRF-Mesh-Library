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

/**
 * To be used as a wrapper class to create DoozScenarioStatus message.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class DoozScenarioStatus extends ApplicationStatusMessage implements Parcelable {

    private static final String TAG = DoozScenarioStatus.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.DOOZ_SCENARIO_STATUS;

    private int tId;
    private int mScenarioId;
    private int mCommand;
    private int mIO;
    private boolean mIsActive;
    private int mUnused;
    private int mValue;
    private int mTransition;
    private int mStartAt;
    private int mDuration;
    private int mDaysInWeek;
    private int mCorrelation;
    private Integer mExtra;

    private static final Creator<DoozScenarioStatus> CREATOR = new Creator<DoozScenarioStatus>() {
        @Override
        public DoozScenarioStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            //noinspection ConstantConditions
            return new DoozScenarioStatus(message);
        }

        @Override
        public DoozScenarioStatus[] newArray(int size) {
            return new DoozScenarioStatus[size];
        }
    };

    /**
     * Constructs DoozScenarioStatus message
     * @param message access message
     */
    public DoozScenarioStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mMessage = message;
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {
        Log.v(TAG, "Received DooZ Scenario Status from: " + MeshAddress.formatAddress(mMessage.getSrc(), true));
        final ByteBuffer buffer = ByteBuffer.wrap(mParameters).order(ByteOrder.LITTLE_ENDIAN);

        tId = (int) (buffer.get());
        mScenarioId = (int) (buffer.getShort());
        mCommand = (int) (buffer.getShort());
        mIO = (int) (buffer.getShort());
        final int isActive = (int) (buffer.getShort());
        mIsActive = isActive == 0x01;
        mUnused = (int) (buffer.getShort());
        mValue = (int) (buffer.get());
        mTransition = (int) (buffer.get());
        mStartAt = (int) (buffer.get());
        mDuration = (int) (buffer.get());
        mDaysInWeek = (int) (buffer.get());
        mCorrelation = buffer.getInt();
        // mExtra = (int) (buffer.getShort());
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    /**
     * Returns the scenarioId
     *
     * @return mScenarioId
     */
    public final int getScenarioId() {
        return mScenarioId;
    }

    /**
     * Returns the command
     *
     * @return mCommand
     */
    public final int getCommand() {
        return mCommand;
    }

    /**
     * Returns the target io of the magic level server model.
     *
     * @return target io
     */
    public final int getIO() {
        return mIO;
    }

    /**
     * Returns the is active flag
     *
     * @return mIsActive
     */
    public final boolean getIsActive() {
        return mIsActive;
    }

    /**
     * Returns the data in the unused field
     *
     * @return mUnused
     */
    public final int getUnused() {
        return mUnused;
    }

    /**
     * Returns the value used for the scenario
     *
     * @return value
     */
    public int getValue() {
        return mValue;
    }

    /**
     * Returns the transition
     *
     * @return mTransition
     */
    public int getTransition() {
        return mTransition;
    }

    /**
     * Returns the start at
     *
     * @return mStartAt
     */
    public int getStartAt() {
        return mStartAt;
    }

    /**
     * Returns the duration
     *
     * @return mDuration
     */
    public int getDuration() {
        return mDuration;
    }

    /**
     * Returns the days in week
     *
     * @return mDaysInWeek
     */
    public int getDaysInWeek() {
        return mDaysInWeek;
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
     * @return correlation
     */
    public int getExtra() {
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