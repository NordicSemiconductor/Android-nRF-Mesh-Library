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
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * ConfigHeartbeatPublicationStatus message.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ConfigHeartbeatSubscriptionStatus extends ConfigStatusMessage implements Parcelable {

    private static final String TAG = ConfigHeartbeatSubscriptionStatus.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_HEARTBEAT_PUBLICATION_STATUS;
    private int srcAddress;
    private int dstAddress;
    private int periodLog;
    private int countLog;
    private int minHops;
    private int maxHops;

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
            //noinspection ConstantConditions
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
        srcAddress = MeshParserUtils.unsignedBytesToInt(mParameters[1], mParameters[2]);
        dstAddress = MeshParserUtils.unsignedBytesToInt(mParameters[3], mParameters[4]);
        periodLog = MeshParserUtils.unsignedByteToInt(mParameters[5]);
        countLog = MeshParserUtils.unsignedByteToInt(mParameters[6]);
        minHops = MeshParserUtils.unsignedByteToInt(mParameters[7]);
        maxHops = MeshParserUtils.unsignedByteToInt(mParameters[8]);

        Log.v(TAG, "Status code: " + mStatusCode);
        Log.v(TAG, "Status message: " + mStatusCodeName);
        Log.d(TAG, "Source address: " + Integer.toHexString(srcAddress));
        Log.d(TAG, "Destination address: " + Integer.toHexString(dstAddress));
        Log.d(TAG, "Period Log: " + Integer.toHexString(periodLog));
        Log.d(TAG, "Count Log: " + Integer.toHexString(countLog));
        Log.d(TAG, "Min Hops: " + minHops);
        Log.d(TAG, "Max Hops: " + maxHops);
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
     * Returns the source address of the Heartbeat publications.
     */
    public int getSrcAddress() {
        return srcAddress;
    }

    /**
     * Returns the destination address of the Heartbeat publications.
     */
    public int getDstAddress() {
        return dstAddress;
    }

    /**
     * Returns the period log.
     */
    public int getPeriodLog() {
        return periodLog;
    }

    /**
     * Returns the publication count.
     */
    public int getCountLog() {
        return countLog;
    }

    /**
     * Returns the minimum hops when receiving heartbeat messages.
     */
    public int getMinHops() {
        return minHops;
    }

    /**
     * Returns the maximum hops when receiving heartbeat messages.
     */
    public int getMaxHops() {
        return maxHops;
    }
}
