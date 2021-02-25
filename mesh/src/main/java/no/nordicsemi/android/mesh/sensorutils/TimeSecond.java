package no.nordicsemi.android.mesh.sensorutils;

import androidx.annotation.NonNull;

/**
 * The Time Second 8, 16, 24 characteristic is used to represent a general count value.
 */
public class TimeSecond extends DevicePropertyCharacteristic<Integer> {
    private final int length;

    public TimeSecond(@NonNull final byte[] data, final int offset, final int length) {
        super(data, offset, length);
        this.length = length;
        switch (length) {
            case 1:
                value = (int) parse(data, offset, length, 0, 254, 0xFF);
                break;
            case 2:
                value = (int) parse(data, offset, length, 0, 65534, 0xFFFF);
                break;
            case 4:
                value = (int) parse(data, offset, length, 0, 4294967294F, 0xFFFFFFFF);
                break;
            default:
                throw new IllegalArgumentException();
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
