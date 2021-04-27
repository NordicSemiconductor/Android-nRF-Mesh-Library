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

import java.util.Arrays;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.sensorutils.DeviceProperty;

import static no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.SENSOR_SERIES_STATUS;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.unsignedBytesToInt;

/**
 * SensorColumnStatus Message.
 */
@SuppressWarnings({"WeakerAccess"})
public final class SensorSeriesStatus extends ApplicationStatusMessage implements Parcelable, SceneStatuses {
    private static final String TAG = SensorSeriesStatus.class.getSimpleName();
    private static final int OP_CODE = SENSOR_SERIES_STATUS;
    private DeviceProperty propertyId;
    private byte[] seriesRawX1X2;

    private static final Creator<SensorSeriesStatus> CREATOR = new Creator<SensorSeriesStatus>() {
        @Override
        public SensorSeriesStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            return new SensorSeriesStatus(message);
        }

        @Override
        public SensorSeriesStatus[] newArray(int size) {
            return new SensorSeriesStatus[size];
        }
    };

    /**
     * Constructs the SensorSettingsStatus mMessage.
     *
     * @param message Access Message
     */
    public SensorSeriesStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mMessage = message;
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {
        propertyId = DeviceProperty.from((short) unsignedBytesToInt(mParameters[0], mParameters[1]));
        if (mParameters.length > 2) {
            seriesRawX1X2 = Arrays.copyOfRange(mParameters, 2, mParameters.length);
        }
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

    /**
     * Returns the Property ID
     */
    public DeviceProperty getPropertyId() {
        return propertyId;
    }

    public byte[] getSeriesRawX1X2() {
        return seriesRawX1X2;
    }
}
