package no.nordicsemi.android.mesh.sensorutils;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.unsignedBytesToInt;

public class Humidity extends DevicePropertyCharacteristic<Float> {

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public Humidity(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        value = unsignedBytesToInt(data[offset], data[offset + 1]) / 100f;
        if (value == 0xFFFF || value < 0.0f || value > 100.0f)
            value = null;
    }

    /**
     * Humidity characteristic.
     *
     * @param humidity Humidity
     */
    public Humidity(final float humidity) {
        value = humidity;
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public int getLength() {
        return 2;
    }

    @Override
    public byte[] getBytes() {
        return ByteBuffer.allocate(getLength()).order(LITTLE_ENDIAN).putShort(value.shortValue()).array();
    }
}
