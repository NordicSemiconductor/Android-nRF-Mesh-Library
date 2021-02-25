package no.nordicsemi.android.mesh.sensorutils;

import androidx.annotation.NonNull;

/**
 * The Count 16,24 characteristic is used to represent a general count value.
 */
public class Count extends DevicePropertyCharacteristic<Integer> {
    private final int length;

    public Count(@NonNull final byte[] data, final int offset, final int length) {
        super(data, offset, length);
        this.length = length;
        switch (length) {
            case 2:
                value = (int) parse(data, offset, length, 0, 65534, 0xFFFF);
                break;
            case 3:
                value = (int) parse(data, offset, length, 0,16777214, 0xFFFFFF);
                break;
            default:
                throw new IllegalArgumentException("Invalid length");
        }
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
