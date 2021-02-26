package no.nordicsemi.android.mesh.sensorutils;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class Humidity extends DevicePropertyCharacteristic<Float> {
    public Humidity(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        value = (float) ByteBuffer.wrap(data).order(BIG_ENDIAN).getShort();
    }

    public Humidity(final float humidity) {
        value = humidity;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public int getLength() {
        return 2;
    }

    @Override
    public byte[] getBytes() {
        return ByteBuffer.allocate(getLength()).order(LITTLE_ENDIAN).putShort(value.shortValue()).array();
    }
}
