package no.nordicsemi.android.mesh.sensorutils;

import java.nio.charset.Charset;

import androidx.annotation.NonNull;

/**
 * The Fixed String characteristic represents an 8, 16, 24, 36 or a 64-octet UTF-8 string.
 */
public class FixedString extends DevicePropertyCharacteristic<String> {
    final int length;

    public FixedString(@NonNull final byte[] data, final int offset, final int length) {
        super(data, offset, length);
        this.length = length;
        value = new String(data, offset, length, Charset.forName("UTF-8"));
    }

    public FixedString(@NonNull final String fixedString) {
        switch (fixedString.getBytes().length) {
            case 8:
                length = 8;
                break;
            case 16:
                length = 16;
                break;
            case 24:
                length = 24;
                break;
            case 36:
                length = 36;
                break;
            case 64:
                length = 64;
                break;
            default:
                throw new IllegalArgumentException("Invalid length");
        }
        value = fixedString;
    }

    @NonNull
    @Override
    public String toString() {
        return value;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public byte[] getBytes() {
        return value.getBytes();
    }

}
