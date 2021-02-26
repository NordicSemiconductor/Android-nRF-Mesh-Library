package no.nordicsemi.android.mesh.sensorutils;

import java.util.Locale;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * The Illuminance characteristic is used to represent a measure of illuminance in units of lux.
 */
public class Illuminance extends DevicePropertyCharacteristic<Float> {

    public Illuminance(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        value = parse(data, offset, 3, 0, 16777214, 0xFFFFFF);
    }

    public Illuminance(final float illuminance) {
        value = illuminance;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "%.2f", value);
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
