package no.nordicsemi.android.mesh.sensorutils;

import androidx.annotation.NonNull;

/**
 * The Time Hour 24 characteristic is used to represent a period of time in hours.
 */
public class TimeHour24 extends DevicePropertyCharacteristic<Integer> {

    public TimeHour24(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        value = (int) parse(data, offset, 3, 0, 16777214, 0xFFFFFF);
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
