package no.nordicsemi.android.mesh.sensorutils;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.unsignedBytesToInt;

/**
 * The Perceived Lightness characteristic is used to represent the perceived lightness of a light.
 */
public class PerceivedLightness extends DevicePropertyCharacteristic<Integer> {

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public PerceivedLightness(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        final int perceivedLightness = unsignedBytesToInt(data[offset], data[offset + 1]);
        if (perceivedLightness < 0 && perceivedLightness > 65535) {
            throw new IllegalArgumentException("Value " + perceivedLightness + " is Prohibited!");
        }
        value = perceivedLightness;
    }

    /**
     * PerceivedLightness characteristic.
     *
     * @param perceivedLightness Perceived lightness
     */
    public PerceivedLightness(final int perceivedLightness) {
        value = perceivedLightness;
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
