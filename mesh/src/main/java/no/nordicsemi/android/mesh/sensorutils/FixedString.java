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
        //noinspection CharsetObjectCanBeUsed
        this.value = new String(data, offset, length, Charset.forName("UTF-8"));
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
    public String getValue() {
        return value;
    }
}
