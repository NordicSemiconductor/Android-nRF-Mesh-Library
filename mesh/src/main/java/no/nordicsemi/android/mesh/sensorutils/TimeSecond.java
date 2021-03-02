package no.nordicsemi.android.mesh.sensorutils;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.unsignedBytesToInt;

/**
 * The Time Second 8, 16, 24 characteristic is used to represent a general count value.
 */
public class TimeSecond extends DevicePropertyCharacteristic<Integer> {
    private final int length;

    public TimeSecond(@NonNull final byte[] data, final int offset, final int length) {
        super(data, offset, length);
        this.length = length;
        switch (length) {
            case 1:
                value = data[offset] & 0xFF;
                break;
            case 2:
                value = unsignedBytesToInt(data[offset], data[offset + 1]);
                break;
            case 4:
                value = ((((data[offset + 2] & 0xFF) << 16) | ((data[offset + 1] & 0xFF) << 8) | data[offset] & 0xFF));
                break;
            default:
                throw new IllegalArgumentException("Invalid length");
        }
    }

    @NonNull
    @Override
    public String toString() {
        return value + " seconds";
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
            default:
            case 4:
                return ByteBuffer.allocate(getLength()).order(LITTLE_ENDIAN).putInt(value).array();
        }
    }
}
