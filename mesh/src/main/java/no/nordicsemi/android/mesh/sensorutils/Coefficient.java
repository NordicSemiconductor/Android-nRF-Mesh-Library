package no.nordicsemi.android.mesh.sensorutils;

import java.nio.ByteBuffer;
import java.util.Locale;

import androidx.annotation.NonNull;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * The Coefficient characteristic is used to represent a general coefficient value.
 */
public class Coefficient extends DevicePropertyCharacteristic<Float> {

    public Coefficient(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        value = ByteBuffer.wrap(data).order(LITTLE_ENDIAN).getFloat(offset);
    }

    public Coefficient(final float coefficient) {
        value = coefficient;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "%.2f", value);
    }

    @Override
    public int getLength() {
        return 4;
    }

    @Override
    public byte[] getBytes() {
        return ByteBuffer.allocate(4).order(LITTLE_ENDIAN).putFloat(value).array();
    }
}
