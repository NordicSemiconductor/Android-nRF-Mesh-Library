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
import no.nordicsemi.android.mesh.Features;
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.mesh.utils.DeviceFeatureUtils;
import no.nordicsemi.android.mesh.utils.HeartbeatPublication;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * ConfigHeartbeatPublicationStatus message.
 */
@SuppressWarnings({"WeakerAccess"})
public class ConfigHeartbeatPublicationStatus extends ConfigStatusMessage implements Parcelable {

    private static final String TAG = ConfigHeartbeatPublicationStatus.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_HEARTBEAT_PUBLICATION_STATUS;
    private HeartbeatPublication heartbeatPublication;

    /**
     * Constructs ConfigHeartbeatPublicationStatus message.
     *
     * @param message Message
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public ConfigHeartbeatPublicationStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    public static final Creator<ConfigHeartbeatPublicationStatus> CREATOR = new Creator<ConfigHeartbeatPublicationStatus>() {
        @Override
        public ConfigHeartbeatPublicationStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            return new ConfigHeartbeatPublicationStatus(message);
        }

        @Override
        public ConfigHeartbeatPublicationStatus[] newArray(int size) {
            return new ConfigHeartbeatPublicationStatus[size];
        }
    };

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void parseStatusParameters() {
        mStatusCode = mParameters[0];
        mStatusCodeName = getStatusCodeName(mStatusCode);
        final int dst = MeshParserUtils.unsignedBytesToInt(mParameters[1], mParameters[2]);
        final int countLog = MeshParserUtils.unsignedByteToInt(mParameters[3]);
        final int periodLog = MeshParserUtils.unsignedByteToInt(mParameters[4]);
        final int ttl = MeshParserUtils.unsignedByteToInt(mParameters[5]);

        final int featuresInt = MeshParserUtils.unsignedBytesToInt(mParameters[6], mParameters[7]);
        final Features features = new Features(DeviceFeatureUtils.getFriendFeature(featuresInt),
                DeviceFeatureUtils.getLowPowerFeature(featuresInt),
                DeviceFeatureUtils.getProxyFeature(featuresInt),
                DeviceFeatureUtils.getRelayFeature(featuresInt));
        final int netKeyIndex = MeshParserUtils.unsignedBytesToInt((mParameters[8]), mParameters[9]);
        heartbeatPublication = new HeartbeatPublication(dst, (byte) countLog, (byte) periodLog, ttl, features, netKeyIndex);
        Log.v(TAG, "Status code: " + mStatusCode);
        Log.v(TAG, "Status message: " + mStatusCodeName);
        Log.d(TAG, "Heartbeat publication: " + heartbeatPublication.toString());
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
     * Returns if the message was successful
     *
     * @return true if the message was successful or false otherwise
     */
    public final boolean isSuccessful() {
        return mStatusCode == 0x00;
    }

    public HeartbeatPublication getHeartbeatPublication() {
        return heartbeatPublication;
    }
}
