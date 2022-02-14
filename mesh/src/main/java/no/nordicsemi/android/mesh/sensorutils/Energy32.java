package no.nordicsemi.android.mesh.sensorutils;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import java.util.Arrays;

/**
 * The Energy 32 characteristic is used to represent a measure of energy in units of kilowatt-hours.
 */
public class Energy32 extends DevicePropertyCharacteristic<Double> {

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public Energy32(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        long bits = 0;
        byte[] bytes = Arrays.copyOfRange(data, offset, offset + getLength());
        for (byte b : bytes) bits = (bits >> 8) | (((long) b & 0xFF) << 24);
        if (bits == 0xFFFFFFFFL || bits == 0xFFFFFFFEL) this.value = null;
        else this.value = ((double) bits) / 1000.0d;
    }

    /**
     * Energy 32 characteristic
     *
     * @param energy Energy in units of kilowatt-hours.
     */
    public Energy32(final double energy) {
        this.value = energy;
    }

    @NonNull
    @Override
    public String toString() {
        return this.value + " kWh";
    }

    @Override
    public int getLength() {
        return 4;
    }

    @Override
    public byte[] getBytes() {
        byte[] bytes = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        if (this.value != null) {
            long value = (long) (this.value * 1000.0d + 0.5d);
            for (int n = 0; n < 4; n++) {
                bytes[n] = (byte) ((value >> (n * 8)) & 0xFF);
            }
        }
        return bytes;
    }
}
