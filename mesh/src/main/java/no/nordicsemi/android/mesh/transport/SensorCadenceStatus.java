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
import androidx.annotation.Nullable;
import no.nordicsemi.android.mesh.sensorutils.DeviceProperty;
import no.nordicsemi.android.mesh.sensorutils.DevicePropertyCharacteristic;
import no.nordicsemi.android.mesh.sensorutils.StatusTriggerDelta;
import no.nordicsemi.android.mesh.sensorutils.StatusTriggerType;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

import static no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.SENSOR_CADENCE_STATUS;
import static no.nordicsemi.android.mesh.sensorutils.StatusTriggerType.from;

/**
 * SensorCadenceStatus Message.
 */
public final class SensorCadenceStatus extends SensorStatusMessage implements Parcelable, SceneStatuses {
    private static final String TAG = SensorCadenceStatus.class.getSimpleName();
    private static final int OP_CODE = SENSOR_CADENCE_STATUS;
    private SensorCadence cadence;

    private static final Creator<SensorCadenceStatus> CREATOR = new Creator<SensorCadenceStatus>() {
        @Override
        public SensorCadenceStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            return new SensorCadenceStatus(message);
        }

        @Override
        public SensorCadenceStatus[] newArray(int size) {
            return new SensorCadenceStatus[size];
        }
    };

    /**
     * Constructs the SensorCadenceStatus mMessage.
     *
     * @param message Access Message
     */
    public SensorCadenceStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mMessage = message;
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {
        final DeviceProperty deviceProperty = DeviceProperty.from((short) MeshParserUtils.unsignedBytesToInt(mParameters[0], mParameters[1]));
        cadence = new SensorCadence(deviceProperty, mParameters);
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

    public SensorCadence getCadence() {
        return cadence;
    }


    @SuppressWarnings("InnerClassMayBeStatic")
    public class SensorCadence {
        private final DeviceProperty deviceProperty;
        private Integer fastCadencePeriodDivisor;
        private StatusTriggerType triggerType;
        private StatusTriggerDelta<?> delta;
        private Integer minInterval;
        private DevicePropertyCharacteristic<?> fastCadenceLow;
        private DevicePropertyCharacteristic<?> fastCadenceHigh;

        /**
         * Constructs SensorCadence
         *
         * @param deviceProperty Device Property
         * @param data           Byte-array
         */
        SensorCadence(@NonNull final DeviceProperty deviceProperty,
                      @NonNull final byte[] data) {
            int offset = 2;
            this.deviceProperty = deviceProperty;
            if (data.length > 2) {
                this.fastCadencePeriodDivisor = data[offset++] & 0xFF;
                this.triggerType = from(data[offset++] & 0xFF);
                // When Trigger type is 0x00, status Trigger Delta Down, Up and Fast Cadence Low and High have the same length.
                final int length = (triggerType.ordinal() == 0b00) ? (data.length - offset - 4) / 4 : (data.length - offset - 8) / 2;
                switch (triggerType) {
                    default:
                    case SENSOR_PROPERTY_ID_FORMAT_TYPE:
                        final DevicePropertyCharacteristic<?> down = DeviceProperty.getCharacteristic(deviceProperty, data, offset, length);
                        final DevicePropertyCharacteristic<?> up = DeviceProperty.getCharacteristic(deviceProperty, data, offset + length, length);
                        this.delta = new StatusTriggerDelta<>(down, up);
                        offset += length * 2;
                        break;
                    case UNIT_LESS:
                        this.delta = new StatusTriggerDelta<>(((short) MeshParserUtils.unsignedBytesToInt(data[offset + 2], data[offset + 3]) / 100), ((short) MeshParserUtils.unsignedBytesToInt(data[offset], data[offset + 1])) / 100
                        );
                        offset += 4;
                        break;
                }
                this.minInterval = data[offset++] & 0xFF;
                this.fastCadenceLow = DeviceProperty.getCharacteristic(deviceProperty, data, offset, length);
                this.fastCadenceHigh = DeviceProperty.getCharacteristic(deviceProperty, data, offset + length, length);
            }
        }

        /**
         * Returns the Sensor property.
         */
        public DeviceProperty getDeviceProperty() {
            return deviceProperty;
        }

        /**
         * Returns the fast cadence period divisor.
         * The Fast Cadence Period Divisor field is a 7-bit value that shall control the increased cadence of publishing Sensor Status messages.
         */
        @Nullable
        public Integer getFastCadencePeriodDivisor() {
            return fastCadencePeriodDivisor;
        }

        /**
         * Returns the Status Trigger Type field that define the unit and format of the Status Trigger Delta Down and the Status Trigger Delta Up fields.
         */
        @Nullable
        public StatusTriggerType getTriggerType() {
            return triggerType;
        }

        /**
         * Returns the delta of cadence.
         */
        @Nullable
        public StatusTriggerDelta<?> getDelta() {
            return delta;
        }

        /**
         * Returns the Fast cadence low
         */
        @Nullable
        public Integer getMinInterval() {
            return minInterval;
        }

        /**
         * Returns the Fast cadence low
         */
        @Nullable
        public DevicePropertyCharacteristic<?> getFastCadenceLow() {
            return fastCadenceLow;
        }

        /**
         * Returns the Fast cadence high
         */
        @Nullable
        public DevicePropertyCharacteristic<?> getFastCadenceHigh() {
            return fastCadenceHigh;
        }
    }
}
