package no.nordicsemi.android.mesh.sensorutils;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.convert24BitsToInt;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.convertIntTo24Bits;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.unsignedBytesToInt;

/**
 * The Count 16,24 characteristic is used to represent a general count value.
 */
public class Count extends DevicePropertyCharacteristic<Integer> {

    private static final String TAG = Count.class.getSimpleName();

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public Count(@NonNull final byte[] data, final int offset, final int length) {
        super(data, offset, length);
        switch (length) {
            case 2:
                value = unsignedBytesToInt(data[offset], data[offset + 1]);
                if (isNotValid(65534, 0xFFFF))
                    value = null;
                break;
            case 3:
                value = convert24BitsToInt(data, offset);
                if (isNotValid(16777214, 0xFFFFFF))
                    value = null;
                break;
            default:
                throw new IllegalArgumentException("Invalid length");
        }
    }

    /**
     * Count16 and 24 characteristic
     *
     * @param count Count
     */
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
        if (getLength() == 2) {
            return ByteBuffer.allocate(getLength()).order(LITTLE_ENDIAN).putShort(value.shortValue()).array();
        } else {
            return convertIntTo24Bits(value);
        }
    }

    private boolean isNotValid(final int max, final int unknownValue) {
        return value == unknownValue || value < 0 || value > max;
    }
}
