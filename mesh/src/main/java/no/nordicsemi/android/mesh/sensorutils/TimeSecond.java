package no.nordicsemi.android.mesh.sensorutils;

import java.nio.ByteBuffer;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.bytesToInt;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.unsignedBytesToInt;

/**
 * The Time Second 8, 16, 24 characteristic is used to represent a general count value.
 */
public class TimeSecond extends DevicePropertyCharacteristic<Integer> {
    private final int length;

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public TimeSecond(@NonNull final byte[] data, final int offset, final int length) {
        super(data, offset, length);
        this.length = length;
        switch (length) {
            case 1:
                value = data[offset] & 0xFF;
                if (isNotValid(254, 0xFF))
                    value = null;
                break;
            case 2:
                value = unsignedBytesToInt(data[offset], data[offset + 1]);
                if (isNotValid(65534, 0xFFFF))
                    value = null;
                break;
            case 4:
                value = bytesToInt(Arrays.copyOfRange(data, offset, offset + length), LITTLE_ENDIAN);
                if (isNotValid(4294967294L, 0xFFFFFFFF))
                    value = null;
                break;
            default:
                throw new IllegalArgumentException("Invalid length");
        }
    }

    /**
     * TimeSecond 8, 16, 32 characteristic
     *
     * @param timeSecond time in seconds
     */
    public TimeSecond(final int timeSecond) {
        value = timeSecond;
        length = Integer.bitCount(timeSecond) / 8;
    }

    @NonNull
    @Override
    public String toString() {
        return value == null ? null : value + " seconds";
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public byte[] getBytes() {
        switch (length) {
            case 1:
                return new byte[]{value.byteValue()};
            case 2:
                return ByteBuffer.allocate(getLength()).order(LITTLE_ENDIAN).putShort(value.shortValue()).array();
            case 4:
                return ByteBuffer.allocate(getLength()).order(LITTLE_ENDIAN).putInt(value).array();
            default:
                throw new IllegalArgumentException("Invalid length");
        }
    }

    private boolean isNotValid(final long max, final int unknownValue) {
        return value == unknownValue || value < 0 || value > max;
    }
}
