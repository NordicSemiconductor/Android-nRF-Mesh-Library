package no.nordicsemi.android.mesh.sensorutils;

import static no.nordicsemi.android.mesh.utils.MeshParserUtils.unsignedBytesToInt;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * The Electric Current characteristic is used to represent a measure of current in units of ampere.
 */
public class ElectricCurrent extends DevicePropertyCharacteristic<Float> {

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public ElectricCurrent(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        int bits = unsignedBytesToInt(data[offset], data[offset + 1]);
        if (bits == 0xFFFF) this.value = null;
        else this.value = ((float) bits) / 100.0f;
    }

    /**
     * Electric current characteristic
     *
     * @param current Current in units of ampere.
     */
    public ElectricCurrent(final float current) {
        this.value = current;
    }

    @NonNull
    @Override
    public String toString() {
        return this.value + " A";
    }

    @Override
    public int getLength() {
        return 2;
    }

    @Override
    public byte[] getBytes() {
        byte[] bytes = {(byte) 0xFF, (byte) 0xFF};
        if (this.value != null) {
            int bits = (int) (this.value * 100.0f + 0.5f);
            for (int n = 0; n < 2; n++) {
                bytes[n] = (byte) ((bits >> (n * 8)) & 0xFF);
            }
        }
        return bytes;
    }
}