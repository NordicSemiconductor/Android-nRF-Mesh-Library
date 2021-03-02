package no.nordicsemi.android.mesh.sensorutils;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * The Illuminance characteristic is used to represent a measure of illuminance in units of lux.
 */
public class Illuminance extends DevicePropertyCharacteristic<Float> {

    public Illuminance(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        value = ((((data[offset + 2] & 0xFF) << 16) | ((data[offset + 1] & 0xFF) << 8) | data[offset] & 0xFF)) / 100f;
        if (value == 0xFFFFFF || value < 0.0f || value > 167772.14f)
            value = null;
    }

    /**
     * Illuminance characteristic.
     *
     * @param illuminance Illuminance
     */
    public Illuminance(final float illuminance) {
        value = illuminance;
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public byte[] getBytes() {
        return MeshParserUtils.convertIntTo24Bits(value.intValue());
    }

}
