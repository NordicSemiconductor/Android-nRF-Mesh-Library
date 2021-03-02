package no.nordicsemi.android.mesh.sensorutils;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import static no.nordicsemi.android.mesh.utils.MeshParserUtils.convertIntTo24Bits;

/**
 * The Time Hour 24 characteristic is used to represent a period of time in hours.
 */
public class TimeHour24 extends DevicePropertyCharacteristic<Integer> {

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public TimeHour24(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        value = (data[offset + 2] & 0xFF) << 16 | (data[offset + 1] & 0xFF) << 8 | data[offset] & 0xFF;
        if (value == 0xFFFFFF || value < 0 || value > 16777214)
            value = null;
    }

    /**
     * TimeHour 24 characteristic
     * @param timeHour24 time in hours
     */
    public TimeHour24(final int timeHour24) {
        value = timeHour24;
    }

    @NonNull
    @Override
    public String toString() {
        return value == null ? null : value + " hour(s)";
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public byte[] getBytes() {
        return convertIntTo24Bits(value);
    }
}
