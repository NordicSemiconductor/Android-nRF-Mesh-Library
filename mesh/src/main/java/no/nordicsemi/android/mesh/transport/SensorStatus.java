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

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.sensorutils.DeviceProperty;
import no.nordicsemi.android.mesh.sensorutils.MarshalledPropertyId;
import no.nordicsemi.android.mesh.sensorutils.MarshalledSensorData;
import no.nordicsemi.android.mesh.utils.SensorFormat;

import static no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.SENSOR_STATUS;

/**
 * SensorStatus Message.
 */
@SuppressWarnings({"WeakerAccess"})
public final class SensorStatus extends ApplicationStatusMessage implements Parcelable, SceneStatuses {
    private static final String TAG = SensorStatus.class.getSimpleName();
    private static final int OP_CODE = SENSOR_STATUS;
    private final ArrayList<MarshalledSensorData> marshalledSensorDataList = new ArrayList<>();

    private static final Creator<SensorStatus> CREATOR = new Creator<SensorStatus>() {
        @Override
        public SensorStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            return new SensorStatus(message);
        }

        @Override
        public SensorStatus[] newArray(int size) {
            return new SensorStatus[size];
        }
    };

    /**
     * Constructs the GenericOnOffStatus mMessage.
     *
     * @param message Access Message
     */
    public SensorStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mMessage = message;
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {
        int offset = 0;
        SensorFormat sensorFormat;
        int length;
        short propertyId;
        while (offset < mParameters.length) {
            final int octet0 = mParameters[offset++] & 0xFF;
            final int octet1 = mParameters[offset++] & 0xFF;
            sensorFormat = SensorFormat.from((byte) ((octet0) & 0x01));
            switch (sensorFormat) {
                case FORMAT_A:
                    length = ((octet0 & 0x1E) >> 1) + 1; // zero based
                    propertyId = (short) ((octet1 << 3) | ((octet0) >> 5));
                    break;
                case FORMAT_B:
                    final int octet2 = mParameters[offset++] & 0xFF;
                    final int tempLength = ((octet0 & 0xFE) >> 1);
                    length = tempLength == 0x7F ? 0 : tempLength;
                    propertyId = (short) (octet2 | octet1);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid data");
            }
            final MarshalledPropertyId marshalledPropertyId = new MarshalledPropertyId(sensorFormat, length, DeviceProperty.from(sensorFormat, propertyId));
            final byte[] raw = Arrays.copyOfRange(mParameters, offset, offset + length);
            final MarshalledSensorData marshalledSensorData = new MarshalledSensorData(marshalledPropertyId, raw);
            Log.d(TAG, "Result: " + marshalledSensorData.toString());
            marshalledSensorDataList.add(marshalledSensorData);
            offset += length;
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

    public ArrayList<MarshalledSensorData> getMarshalledSensorData() {
        return marshalledSensorDataList;
    }
}
