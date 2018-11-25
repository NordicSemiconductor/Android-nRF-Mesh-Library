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
import android.support.annotation.NonNull;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;

import no.nordicsemi.android.meshprovisioner.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * To be used as a wrapper class for when creating the GenericOnOffStatus Message.
 */
@SuppressWarnings("unused")
public final class SceneRegisterStatus extends GenericStatusMessage implements Parcelable {

    private static final String TAG = SceneRegisterStatus.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.SCENE_REGISTER_STATUS;
    private int mStatus;
    private int mCurrentScene;
    private int[] mSceneList;


    /**
     * Constructs the GenericOnOffStatus mMessage.
     *
     * @param node    Node from which the mMessage originated from
     * @param message Access Message
     */
    public SceneRegisterStatus(@NonNull final ProvisionedMeshNode node,
                               @NonNull final AccessMessage message) {
        super(node, message);
        this.mMessage = message;
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    private static final Creator<SceneRegisterStatus> CREATOR = new Creator<SceneRegisterStatus>() {
        @Override
        public SceneRegisterStatus createFromParcel(Parcel in) {
            final ProvisionedMeshNode meshNode = (ProvisionedMeshNode) in.readValue(ProvisionedMeshNode.class.getClassLoader());
            final AccessMessage message = (AccessMessage) in.readValue(AccessMessage.class.getClassLoader());
            return new SceneRegisterStatus(meshNode, message);
        }

        @Override
        public SceneRegisterStatus[] newArray(int size) {
            return new SceneRegisterStatus[size];
        }
    };

    @Override
    void parseStatusParameters() {
        Log.v(TAG, "Received scene register status from: " + MeshParserUtils.bytesToHex(mMessage.getSrc(), true));
        final ByteBuffer buffer = ByteBuffer.wrap(mParameters).order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(0);
        mStatus = buffer.get() & 0xFF;
        mCurrentScene = buffer.getShort() & 0xFFFF;
        Log.v(TAG, "Status: " + mStatus);
        Log.v(TAG, "Current Scene: " + mCurrentScene);
        if (buffer.limit() > 1) {
            short[] scenes = buffer.asShortBuffer().array();
            mSceneList = new int[scenes.length];
            for (int i = 0; i < scenes.length; i++) {
                mSceneList[i] = (int) scenes[i] & 0xFFFF;
            }
            Log.v(TAG, "Scenees stored: " + scenes.length);
        }
    }

    @Override
    int getOpCode() {
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
    public int[] getSceneList() {
        return mSceneList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeValue(mNode);
        dest.writeValue(mMessage);
    }
}
