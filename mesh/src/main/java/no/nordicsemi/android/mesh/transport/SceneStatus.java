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
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

import static no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.SCENE_STATUS;

/**
 * To be used as a wrapper class for when creating the GenericOnOffStatus Message.
 */
@SuppressWarnings({"WeakerAccess"})
public final class SceneStatus extends ApplicationStatusMessage implements Parcelable, SceneStatuses {
    private static final int SCENE_STATUS_MANDATORY_LENGTH = 3;
    private static final String TAG = SceneStatus.class.getSimpleName();
    private static final int OP_CODE = SCENE_STATUS;
    private int mStatusCode;
    private int mCurrentScene;
    private int mTargetScene;
    private int mRemainingTime;
    private int mTransitionSteps;
    private int mTransitionResolution;

    private static final Creator<SceneStatus> CREATOR = new Creator<SceneStatus>() {
        @Override
        public SceneStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            return new SceneStatus(message);
        }

        @Override
        public SceneStatus[] newArray(int size) {
            return new SceneStatus[size];
        }
    };

    /**
     * Constructs the GenericOnOffStatus mMessage.
     *
     * @param message Access Message
     */
    public SceneStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mMessage = message;
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {
        Log.v(TAG, "Received scene status from: " + MeshAddress.formatAddress(mMessage.getSrc(), true));
        final ByteBuffer buffer = ByteBuffer.wrap(mParameters).order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(0);
        mStatusCode = buffer.get() & 0xFF;
        mCurrentScene = buffer.getShort() & 0xFFFF;
        Log.d(TAG, "Status: " + mStatusCode);
        Log.d(TAG, "Current Scene : " + mCurrentScene);
        if (buffer.limit() > SCENE_STATUS_MANDATORY_LENGTH) {
            mTargetScene = buffer.getShort() & 0xFFFF;
            mRemainingTime = buffer.get() & 0xFF;
            mTransitionSteps = (mRemainingTime & 0x3F);
            mTransitionResolution = (mRemainingTime >> 6);
            Log.d(TAG, "Target scene: " + mTargetScene);
            Log.d(TAG, "Remaining time, transition number of steps: " + mTransitionSteps);
            Log.d(TAG, "Remaining time, transition number of step resolution: " + mTransitionResolution);
            Log.d(TAG, "Remaining time: " + MeshParserUtils.getRemainingTime(mRemainingTime));
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
    public final int getStatus() {
        return mStatusCode;
    }

    /**
     * Returns true if the message was successful.
     */
    public final boolean isSuccessful() {
        return mStatusCode == 0;
    }

    /**
     * Returns the target state of the GenericOnOffModel
     *
     * @return true if on and false other wise
     */
    public final int getCurrentScene() {
        return mCurrentScene;
    }

    /**
     * Returns the target state of the GenericOnOffModel
     *
     * @return true if on and false other wise
     */
    public final Integer getTargetScene() {
        return mTargetScene;
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
