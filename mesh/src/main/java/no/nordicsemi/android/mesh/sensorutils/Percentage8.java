package no.nordicsemi.android.mesh.sensorutils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import static no.nordicsemi.android.mesh.utils.MeshParserUtils.unsignedToSigned;

/**
 * The Percentage 8 characteristic is used to represent a measure of percentage.
 */
public class Percentage8 extends DevicePropertyCharacteristic<Float> {

    private static final String TAG = Percentage8.class.getSimpleName();

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public Percentage8(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        value = unsignedToSigned(data[offset] & 0xFF, 8) / 2.0f;
        if (value < 0.0f || value > 100.0f) {
            this.value = null;
            Log.e(TAG, "Value " + value + " is Prohibited!");
        }
    }

    /**
     * Percentage8 characteristic.
     *
     * @param percentage percentage
     */
    public Percentage8(final float percentage) {
        value = percentage;
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(value);
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
