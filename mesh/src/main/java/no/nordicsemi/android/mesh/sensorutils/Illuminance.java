package no.nordicsemi.android.mesh.sensorutils;

import java.util.Locale;

import androidx.annotation.NonNull;

/**
 * The Illuminance characteristic is used to represent a measure of illuminance in units of lux.
 */
public class Illuminance extends DevicePropertyCharacteristic<Float> {
    private final int length;

    public Illuminance(@NonNull final byte[] data, final int offset, final int length) {
        super(data, offset, length);
        this.length = length;
        switch (length) {
            case 2:
                value = parse(data, offset, length, 0, 65534, 0xFFFF);
                break;
            case 3:
                value = parse(data, offset, length, 0, 16777214, 0xFFFFFF);
                break;
            default:
                throw new IllegalArgumentException("Invalid length");
        }
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "%.2f", value);
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
