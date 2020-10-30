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
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.mesh.utils.HeartbeatSubscription;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * ConfigHeartbeatPublicationStatus message.
 */
@SuppressWarnings({"WeakerAccess"})
public class ConfigHeartbeatSubscriptionStatus extends ConfigStatusMessage implements Parcelable {

    private static final String TAG = ConfigHeartbeatSubscriptionStatus.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_HEARTBEAT_SUBSCRIPTION_STATUS;
    private HeartbeatSubscription heartbeatSubscription;

    /**
     * Constructs ConfigHeartbeatSubscriptionStatus message.
     *
     * @param message Message
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public ConfigHeartbeatSubscriptionStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    public static final Creator<ConfigHeartbeatSubscriptionStatus> CREATOR = new Creator<ConfigHeartbeatSubscriptionStatus>() {
        @Override
        public ConfigHeartbeatSubscriptionStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            return new ConfigHeartbeatSubscriptionStatus(message);
        }

        @Override
        public ConfigHeartbeatSubscriptionStatus[] newArray(int size) {
            return new ConfigHeartbeatSubscriptionStatus[size];
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
        final int srcAddress = MeshParserUtils.unsignedBytesToInt(mParameters[1], mParameters[2]);
        final int dstAddress = MeshParserUtils.unsignedBytesToInt(mParameters[3], mParameters[4]);
        final int periodLog = MeshParserUtils.unsignedByteToInt(mParameters[5]);
        final int countLog = MeshParserUtils.unsignedByteToInt(mParameters[6]);
        final int minHops = MeshParserUtils.unsignedByteToInt(mParameters[7]);
        final int maxHops = MeshParserUtils.unsignedByteToInt(mParameters[8]);

        heartbeatSubscription = new HeartbeatSubscription(srcAddress, dstAddress, (byte)periodLog, (byte)countLog, minHops, maxHops);
        Log.v(TAG, "Status code: " + mStatusCode);
        Log.v(TAG, "Status message: " + mStatusCodeName);
        Log.d(TAG, "Heartbeat subscription: " + heartbeatSubscription.toString());
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
     * Returns the Heartbeat subscription.
     */
    public HeartbeatSubscription getHeartbeatSubscription() {
        return heartbeatSubscription;
    }

    /**
     * Returns if the message was successful
     *
     * @return true if the message was successful or false otherwise
     */
    public final boolean isSuccessful() {
        return mStatusCode == 0x00;
    }
}
