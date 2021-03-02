package no.nordicsemi.android.mesh.sensorutils;

import android.util.Log;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.unsignedToSigned;

public class Temperature extends DevicePropertyCharacteristic<Float> {
    private static final String TAG = Temperature.class.getSimpleName();
    private final int length;

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public Temperature(@NonNull final byte[] data, final int offset, final int length) {
        super(data, offset, length);
        this.length = length;
        int tempValue;
        switch (length) {
            case 1:
                tempValue = unsignedToSigned(data[offset] & 0xFF, 8);
                if (tempValue == 0x8000) {
                    value = null;
                }
                value = tempValue / 2.0f;
                if (value < -64.0f || value > 63.5f) {
                    this.value = null;
                    Log.e(TAG, "Value " + tempValue + " is Prohibited!");
                }
                break;
            case 2:
                tempValue = unsignedToSigned(data[offset] & 0xFF | (data[offset + 1] & 0xFF) << 8, 16);
                if (tempValue == 0x8000) {
                    value = null;
                }
                value = (tempValue / 100.0f);
                if (value < -273.15f || value > 327.67f) {
                    this.value = null;
                    Log.e(TAG, "Value " + tempValue + " is Prohibited!");
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid length");
        }
    }

    /**
     * Temperature characteristic for Temperature and Temperature 8
     * @param temperature temperature
     */
    public Temperature(final float temperature) {
        final int length = Float.floatToIntBits(temperature) / 8;
        if (length != 1 && length != 2) {
            throw new IllegalArgumentException("Illegal length");
        }
        this.length = length;
        value = temperature;
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public byte[] getBytes() {
        final float val;
        if (getLength() == 1) {
            val = value * 10;
            return new byte[]{(byte) val};
        }
        val = value * 100;
        return ByteBuffer.allocate(getLength()).order(LITTLE_ENDIAN).putShort((short) val).array();
    }

}
