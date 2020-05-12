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

import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;

/**
 * To be used as a wrapper class for when creating the ConfigNetworkTransmitStatus message.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class ConfigNetworkTransmitStatus extends ConfigStatusMessage implements Parcelable {

    private static final String TAG = ConfigNetworkTransmitStatus.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_NETWORK_TRANSMIT_STATUS;
    private int mNetworkTransmitCount;
    private int mNetworkTransmitIntervalSteps;

    private static final Creator<ConfigNetworkTransmitStatus> CREATOR = new Creator<ConfigNetworkTransmitStatus>() {
        @Override
        public ConfigNetworkTransmitStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            //noinspection ConstantConditions
            return new ConfigNetworkTransmitStatus(message);
        }

        @Override
        public ConfigNetworkTransmitStatus[] newArray(int size) {
            return new ConfigNetworkTransmitStatus[size];
        }
    };

    /**
     * Constructs a ConfigNetworkTransmitStatus message.
     *
     * @param message Access message received
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public ConfigNetworkTransmitStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    final void parseStatusParameters() {
        final byte[] payload = ((AccessMessage) mMessage).getAccessPdu();

        mNetworkTransmitCount = payload[2] & 0b111;
        mNetworkTransmitIntervalSteps = (payload[2] >> 3) & 0b11111;
    }

    /**
     * Returns the Network Transmit Count set in this message
     *
     * @return Network Transmit Count
     */
    public int getNetworkTransmitCount() {
        return mNetworkTransmitCount;
    }

    /**
     * Returns the Network Transmit Interval Steps set in this message
     *
     * @return Network Transmit Interval Steps
     */
    public int getNetworkTransmitIntervalSteps() {
        return mNetworkTransmitIntervalSteps;
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
