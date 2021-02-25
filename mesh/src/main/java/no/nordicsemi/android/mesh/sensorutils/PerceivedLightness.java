package no.nordicsemi.android.mesh.sensorutils;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * The Perceived Lightness characteristic is used to represent the perceived lightness of a light.
 */
public class PerceivedLightness extends DevicePropertyCharacteristic<Float> {

    public PerceivedLightness(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        final int perceivedLightness = (short) MeshParserUtils.unsignedBytesToInt(data[offset], data[offset + 1]);
        if (perceivedLightness < 0 && perceivedLightness > 65535) {
            throw new IllegalArgumentException("Value " + perceivedLightness + " is Prohibited!");
        }
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public int getLength() {
        return 16;
    }

    @Override
    public Float getValue() {
        return value;
    }
}
