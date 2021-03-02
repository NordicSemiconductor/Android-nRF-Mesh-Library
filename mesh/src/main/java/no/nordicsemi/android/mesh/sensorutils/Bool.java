package no.nordicsemi.android.mesh.sensorutils;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * The Boolean characteristic defines the predefined Boolean values as an enumeration.
 */
public class Bool extends DevicePropertyCharacteristic<Boolean> {

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public Bool(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        final int bool = data[offset] & 0xFF;
        if (bool < 0 && bool > 1) {
            throw new IllegalArgumentException("Value " + bool + " is Prohibited!");
        }
        value = bool == 1;
    }

    /**
     * Boolean characteristic
     *
     * @param flag true of false
     */
    public Bool(final boolean flag) {
        value = flag;
    }

    @NonNull
    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public byte[] getBytes() {
        return new byte[]{(byte) (value ? 0x01 : 0x00)};
    }
}
