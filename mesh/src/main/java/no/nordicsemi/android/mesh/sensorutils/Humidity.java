package no.nordicsemi.android.mesh.sensorutils;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;

import static java.nio.ByteOrder.BIG_ENDIAN;

public class Humidity extends DevicePropertyCharacteristic<Float> {
    public Humidity(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        this.value = (float) ByteBuffer.wrap(this.data).order(BIG_ENDIAN).getShort();
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
    public Float getValue() {
        return value;
    }
}
