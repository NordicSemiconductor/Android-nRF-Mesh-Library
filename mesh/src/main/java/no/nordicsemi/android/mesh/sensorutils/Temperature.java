package no.nordicsemi.android.mesh.sensorutils;

import java.util.Locale;

import androidx.annotation.NonNull;

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
    public Float getValue() {
        return value;
    }
}
