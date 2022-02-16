package no.nordicsemi.android.mesh.sensorutils;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * The Power characteristic is used to represent a measure of power in units of watts.
 */
public class Power extends DevicePropertyCharacteristic<Float> {

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public Power(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        int bits = (((data[offset + 2] & 0xFF) << 16) | ((data[offset + 1] & 0xFF) << 8) | (data[offset] & 0xFF));
        if (bits == 0xFFFFFF) this.value = null;
        else this.value = ((float) bits) / 10.0f;
    }

    /**
     * Power characteristic
     *
     * @param power Power in units of watts.
     */
    public Power(final float power) {
        this.value = power;
    }

    @NonNull
    @Override
    public String toString() {
        return this.value + " W";
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public byte[] getBytes() {
        byte[] bytes = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        if (this.value != null) {
            int bits = (int) (this.value * 10.0f + 0.5f);
            for (int n = 0; n < 3; n++) {
                bytes[n] = (byte) ((bits >> (n * 8)) & 0xFF);
            }
        }
        return bytes;
    }
}