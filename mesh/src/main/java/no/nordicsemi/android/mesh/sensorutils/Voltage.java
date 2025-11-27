package no.nordicsemi.android.mesh.sensorutils;

import static no.nordicsemi.android.mesh.utils.MeshParserUtils.unsignedBytesToInt;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * The Voltage characteristic is used to represent a measure of positive electric potential difference.
 */
public class Voltage extends DevicePropertyCharacteristic<Float> {

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public Voltage(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        int bits = unsignedBytesToInt(data[offset], data[offset + 1]);
        if (bits == 0xFFFF) this.value = null;
        else this.value = ((float) bits) / 64.0f;
    }

    /**
     * Voltage characteristic
     *
     * @param current Voltage in units of volt.
     */
    public Voltage(final float voltage) {
        this.value = voltage;
    }

    @NonNull
    @Override
    public String toString() {
        return this.value + " V";
    }

    @Override
    public int getLength() {
        return 2;
    }

    @Override
    public byte[] getBytes() {
        byte[] bytes = {(byte) 0xFF, (byte) 0xFF};
        if (this.value != null) {
            int bits = (int) (this.value * 64.0f + 0.5f);
            for (int n = 0; n < 2; n++) {
                bytes[n] = (byte) ((bits >> (n * 8)) & 0xFF);
            }
        }
        return bytes;
    }
}