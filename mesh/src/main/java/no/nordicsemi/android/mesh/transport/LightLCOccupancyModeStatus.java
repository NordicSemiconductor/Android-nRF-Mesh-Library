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
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshAddress;

/**
 * LightLCOccupancyModeStatus
 */
@SuppressWarnings({"WeakerAccess"})
public final class LightLCOccupancyModeStatus extends ApplicationStatusMessage implements Parcelable {

    private static final String TAG = LightLCOccupancyModeStatus.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.LIGHT_LC_MODE_STATUS;
    private boolean status;

    private static final Creator<LightLCOccupancyModeStatus> CREATOR = new Creator<LightLCOccupancyModeStatus>() {
        @Override
        public LightLCOccupancyModeStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            return new LightLCOccupancyModeStatus(message);
        }

        @Override
        public LightLCOccupancyModeStatus[] newArray(int size) {
            return new LightLCOccupancyModeStatus[size];
        }
    };

    /**
     * Constructs the LightLCOccupancyModeStatus mMessage.
     *
     * @param message Access Message
     */
    public LightLCOccupancyModeStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {
        Log.v(TAG, "Received light lc occupancy mode status from: " + MeshAddress.formatAddress(mMessage.getSrc(), true));
        status = (mParameters[0] & 0xFF) == 0x01;
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
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

    public boolean getStatus() {
        return status;
    }
}
