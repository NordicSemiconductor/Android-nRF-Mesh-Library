package no.nordicsemi.android.mesh.sensorutils;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * The Count 16,24 characteristic is used to represent a general count value.
 */
public class Count extends DevicePropertyCharacteristic<Integer> {

    public Count(@NonNull final byte[] data, final int offset, final int length) {
        super(data, offset, length);
        switch (length) {
            case 2:
                value = (int) parse(data, offset, length, 0, 65534, 0xFFFF);
                break;
            case 3:
                value = (int) parse(data, offset, length, 0, 16777214, 0xFFFFFF);
                break;
            default:
                throw new IllegalArgumentException("Invalid length");
        }
    }

    public Count(final int count) {
        value = count;
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public int getLength() {
        return Integer.bitCount(value) / 8;
    }

    @Override
    public byte[] getBytes() {
        if(getLength() == 2){
            return ByteBuffer.allocate(getLength()).order(LITTLE_ENDIAN).putShort(value.shortValue()).array();
        } else {
            return MeshParserUtils.convertIntTo24Bits(value);
        }
    }
}
