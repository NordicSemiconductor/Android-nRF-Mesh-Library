package no.nordicsemi.android.mesh.sensorutils;

import java.util.Arrays;

import androidx.annotation.NonNull;

public class UnknownCharacteristic extends DevicePropertyCharacteristic<byte[]> {

    public UnknownCharacteristic(@NonNull final byte[] data, final int offset, final int length) {
        super(data, offset, length);
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public int getLength() {
        return data.length;
    }

    @Override
    public byte[] getValue() {
        return data;
    }

    public byte[] parse(@NonNull final byte[] data, final int offset, final int length) {
        this.data = Arrays.copyOfRange(data, offset, offset + getLength());
        return this.data;
    }
}
