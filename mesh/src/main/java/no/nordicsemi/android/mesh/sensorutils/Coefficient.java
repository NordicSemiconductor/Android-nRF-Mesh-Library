package no.nordicsemi.android.mesh.sensorutils;

import java.nio.ByteBuffer;
import java.util.Locale;

import androidx.annotation.NonNull;

import static java.nio.ByteOrder.BIG_ENDIAN;

/**
 * The Coefficient characteristic is used to represent a general coefficient value.
 */
public class Coefficient extends DevicePropertyCharacteristic<Float> {

    public Coefficient(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        this.value = ByteBuffer.wrap(data).order(BIG_ENDIAN).getFloat(offset);
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
    public Float getValue() {
        return value;
    }
}
