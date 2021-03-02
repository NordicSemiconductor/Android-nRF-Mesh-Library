package no.nordicsemi.android.mesh.sensorutils;

import java.nio.ByteBuffer;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class Pressure extends DevicePropertyCharacteristic<Float> {

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public Pressure(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        value = ByteBuffer.wrap(Arrays.copyOfRange(data, offset, offset + getLength())).order(LITTLE_ENDIAN).getInt() / 10.0f;
    }

    /**
     * Pressure characteristic
     *
     * @param pressure Pressure
     */
    public Pressure(final Float pressure) {
        value = pressure;
    }

    @NonNull
    @Override
    public String toString() {
        return value + " Pa";
    }

    @Override
    public int getLength() {
        return 4;
    }

    @Override
    public byte[] getBytes() {
        return ByteBuffer.allocate(getLength()).order(LITTLE_ENDIAN).putInt(value.intValue()).array();
    }
}
