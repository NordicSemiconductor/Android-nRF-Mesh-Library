package no.nordicsemi.android.mesh.sensorutils;

import androidx.annotation.NonNull;

/**
 * The Boolean characteristic defines the predefined Boolean values as an enumeration.
 */
public class Bool extends DevicePropertyCharacteristic<Boolean> {

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
