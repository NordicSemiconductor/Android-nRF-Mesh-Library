package no.nordicsemi.android.mesh.sensorutils;

import androidx.annotation.NonNull;

public class Temperature8 extends DevicePropertyCharacteristic<Float> {
    public Temperature8(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        this.value = parse(data, offset, 1, -64.0, 63.5, 0xFF);
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public Float getValue() {
        return value;
    }
}
