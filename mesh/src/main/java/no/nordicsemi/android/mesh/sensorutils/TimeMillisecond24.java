package no.nordicsemi.android.mesh.sensorutils;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * The Time Hour 24 characteristic is used to represent a period of time in hours.
 */
public class TimeMillisecond24 extends DevicePropertyCharacteristic<Float> {

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public TimeMillisecond24(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        final int tempValue = (data[offset + 2] & 0xFF) << 16 | (data[offset + 1] & 0xFF) << 8 | data[offset] & 0xFF;
        if (tempValue == 0xFFFF)
            value = null;
        value = tempValue / 1000f;
        if (value < 0 || value > 16777.214)
            value = null;

    }

    /**
     * Time Milliseconds 24 characteristic
     *
     * @param seconds time in seconds
     */
    public TimeMillisecond24(final float seconds) {
        value = seconds;
    }

    @NonNull
    @Override
    public String toString() {
        return value == null ? null : value + " seconds";
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public byte[] getBytes() {
        return MeshParserUtils.convertIntTo24Bits((int) (value * 1000));
    }

}
