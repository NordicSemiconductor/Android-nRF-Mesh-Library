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

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshAddress;

/**
 * To be used as a wrapper class for when creating the GenericOnOffStatus Message.
 */
@SuppressWarnings({"WeakerAccess"})
public final class SceneRegisterStatus extends ApplicationStatusMessage implements Parcelable, SceneStatuses {
    private static final int SCENE_REGISTER_STATUS_MANDATORY_LENGTH = 3;
    private static final String TAG = SceneRegisterStatus.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.SCENE_REGISTER_STATUS;
    private int mStatus;
    private int mCurrentScene;
    private final ArrayList<Integer> mSceneList = new ArrayList<>();

    private static final Creator<SceneRegisterStatus> CREATOR = new Creator<SceneRegisterStatus>() {
        @Override
        public SceneRegisterStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            return new SceneRegisterStatus(message);
        }

        @Override
        public SceneRegisterStatus[] newArray(int size) {
            return new SceneRegisterStatus[size];
        }
    };

    /**
     * Constructs the GenericOnOffStatus mMessage.
     *
     * @param message Access Message
     */
    public SceneRegisterStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mMessage = message;
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {
        Log.v(TAG, "Received scene register status from: " + MeshAddress.formatAddress(mMessage.getSrc(), true));
        final ByteBuffer buffer = ByteBuffer.wrap(mParameters).order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(0);
        mStatus = buffer.get() & 0xFF;
        mCurrentScene = buffer.getShort() & 0xFFFF;
        Log.v(TAG, "Status: " + mStatus);
        Log.v(TAG, "Current Scene: " + mCurrentScene);
        if (buffer.limit() > SCENE_REGISTER_STATUS_MANDATORY_LENGTH) {
            int sceneCount = (buffer.limit() - SCENE_REGISTER_STATUS_MANDATORY_LENGTH) / 2;
            for (int i = 0; i < sceneCount; i++) {
                mSceneList.add(buffer.getShort() & 0xFFFF);
            }
            Log.v(TAG, "Scenes stored: " + sceneCount);
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
        return mStatus;
    }

    public boolean isSuccessful() {
        return mStatus == 0x00;
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
     * Returns the scene list.
     *
     * @return scene list
     */
    public ArrayList<Integer> getSceneList() {
        return mSceneList;
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
