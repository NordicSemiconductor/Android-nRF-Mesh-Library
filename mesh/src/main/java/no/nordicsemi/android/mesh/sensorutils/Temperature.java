package no.nordicsemi.android.mesh.sensorutils;

import java.nio.ByteBuffer;
import java.util.Locale;

import androidx.annotation.NonNull;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class Temperature extends DevicePropertyCharacteristic<Float> {
    private final int length;

    public Temperature(@NonNull final byte[] data, final int offset, final int length) {
        super(data, offset, length);
        this.length = length;
        switch (length) {
            case 1:
                this.value = parse(data, offset, length, -64.0, 63.5, 0xFF);
                break;
            case 2:
                this.value = parse(data, offset, length, -273.15, 327.67, 0x8000);
                break;
            default:
                throw new IllegalArgumentException("Invalid length");
        }
    }

    public Temperature(@NonNull final Float temperature) {
        final int length = Float.floatToIntBits(temperature) / 8;
        if (length != 1 && length != 2) {
            throw new IllegalArgumentException("Illegal length");
        }
        this.length = length;
        value = temperature;
    }

    @NonNull
    @Override
    public String toString() {
        return length == 1 ? String.format(Locale.US, "%.1f", value) : String.format(Locale.US, "%.2f", value);
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public byte[] getBytes() {
        if (getLength() == 1) {
            return new byte[]{value.byteValue()};
        }
        return ByteBuffer.allocate(getLength()).order(LITTLE_ENDIAN).putShort(value.shortValue()).array();
    }

}
