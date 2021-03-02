package no.nordicsemi.android.mesh.transport;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.mesh.sensorutils.DeviceProperty;
import no.nordicsemi.android.mesh.sensorutils.DevicePropertyCharacteristic;
import no.nordicsemi.android.mesh.sensorutils.StatusTriggerDelta;
import no.nordicsemi.android.mesh.sensorutils.StatusTriggerType;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static no.nordicsemi.android.mesh.sensorutils.StatusTriggerDelta.Characteristic;
import static no.nordicsemi.android.mesh.sensorutils.StatusTriggerDelta.Percent;
import static no.nordicsemi.android.mesh.sensorutils.StatusTriggerType.from;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.unsignedBytesToInt;

public abstract class SensorMessage extends ApplicationStatusMessage {
    SensorMessage(@NonNull final AccessMessage message) {
        super(message);
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class SensorCadence {
        private final DeviceProperty deviceProperty;
        private int periodDivisor;
        private StatusTriggerType triggerType;
        private int statusMinInterval;
        private StatusTriggerDelta<?> delta;
        private DevicePropertyCharacteristic<?> fastCadenceLow;
        private DevicePropertyCharacteristic<?> fastCadenceHigh;

        /**
         * Constructs SensorCadenceSet message.
         *
         * @param property          {@link DeviceProperty} device property.
         * @param periodDivisor     Period divisor.
         * @param triggerType       The Status Trigger Delta Down field identifies a Status Trigger Delta Down state of an element.
         * @param delta             The Status Trigger Delta identifies a Status Trigger Delta Up and Down state of an element.
         * @param statusMinInterval Minimum interval
         * @param fastCadenceLow    The Fast Cadence Low field identifies a Fast Cadence Low state of an element
         * @param fastCadenceHigh   Fast Cadence High field identifies a Fast Cadence High state of an element.
         * @throws IllegalArgumentException if any illegal arguments are passed
         */
        public SensorCadence(@NonNull final DeviceProperty property,
                             final int periodDivisor,
                             @NonNull final StatusTriggerType triggerType,
                             @NonNull final StatusTriggerDelta<?> delta,
                             @NonNull final DevicePropertyCharacteristic<?> fastCadenceLow,
                             @NonNull final DevicePropertyCharacteristic<?> fastCadenceHigh,
                             final int statusMinInterval) {
            this.deviceProperty = property;
            this.periodDivisor = periodDivisor;
            this.triggerType = triggerType;
            this.delta = delta;
            this.statusMinInterval = statusMinInterval;
            this.fastCadenceLow = fastCadenceLow;
            this.fastCadenceHigh = fastCadenceHigh;
        }

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
                this.periodDivisor = data[offset++] & 0xFF;
                this.triggerType = from(data[offset++] & 0xFF);
                // When Trigger type is 0x00, status Trigger Delta Down, Up and Fast Cadence Low and High have the same length.
                final int length = (triggerType.ordinal() == 0b00) ? (data.length - offset - 4) / 4 : (data.length - offset - 8) / 2;
                switch (triggerType) {
                    default:
                    case SENSOR_PROPERTY_ID_FORMAT_TYPE:
                        final DevicePropertyCharacteristic<?> down = DeviceProperty.getCharacteristic(deviceProperty, data, offset, length);
                        final DevicePropertyCharacteristic<?> up = DeviceProperty.getCharacteristic(deviceProperty, data, offset + length, length);
                        this.delta = new Characteristic(down, up);
                        offset += length * 2;
                        break;
                    case UNIT_LESS:
                        this.delta = new Percent((float)unsignedBytesToInt(data[offset + 2], data[offset + 3]) / 100,
                                (float)unsignedBytesToInt(data[offset], data[offset + 1]) / 100f);
                        offset += 4;
                        break;
                }
                this.statusMinInterval = data[offset++] & 0xFF;
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
        public int getPeriodDivisor() {
            return periodDivisor;
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
         * Returns status min interval.
         */
        public int getStatusMinInterval() {
            return statusMinInterval;
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

        protected byte[] toBytes() {
            final byte[] data = new byte[2 + 1 + delta.getLength() + 1 + fastCadenceLow.getLength() + fastCadenceHigh.getLength()];
            final ByteBuffer buffer = ByteBuffer.allocate(data.length).order(LITTLE_ENDIAN);
            buffer.putShort(deviceProperty.getPropertyId());
            buffer.put((byte) ((periodDivisor << 7) | triggerType.ordinal()));
            if (triggerType == StatusTriggerType.SENSOR_PROPERTY_ID_FORMAT_TYPE) {
                buffer.put(((DevicePropertyCharacteristic<?>) delta.getDown()).getBytes());
                buffer.put(((DevicePropertyCharacteristic<?>) delta.getUp()).getBytes());
            } else {
                buffer.putShort(((Percent) delta).getDown().shortValue());
                buffer.putShort(((Percent) delta).getUp().shortValue());
            }
            return data;
        }
    }
}
