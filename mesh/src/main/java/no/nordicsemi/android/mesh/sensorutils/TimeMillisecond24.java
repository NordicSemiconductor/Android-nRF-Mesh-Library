package no.nordicsemi.android.mesh.sensorutils;

import androidx.annotation.NonNull;

/**
 * The Time Hour 24 characteristic is used to represent a period of time in hours.
 */
public class TimeMillisecond24 extends DevicePropertyCharacteristic<Integer> {
    private final int length;

    public TimeMillisecond24(@NonNull final byte[] data, final int offset, final int length) {
        super(data, offset, length);
        this.length = length;
        if (length == 3) {
            value = (int) parse(data, offset, length, 0, 16777214, 0xFFFFFF);
        } else {
            throw new IllegalArgumentException("Invalid length");
        }
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
