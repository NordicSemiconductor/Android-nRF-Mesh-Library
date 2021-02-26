package no.nordicsemi.android.mesh.sensorutils;

import java.util.Locale;

import androidx.annotation.NonNull;

/**
 * The Percentage 8 characteristic is used to represent a measure of percentage.
 */
public class Percentage8 extends DevicePropertyCharacteristic<Float> {

    public Percentage8(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        value = parse(data, offset, getLength(), 0,100, 0xFF);
    }

    public Percentage8(final float percentage){
        value = percentage;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "%.1f", value);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public byte[] getBytes() {
        return new byte[value.byteValue()];
    }
}
