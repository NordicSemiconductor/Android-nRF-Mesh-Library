package no.nordicsemi.android.mesh.sensorutils;

import java.io.Serializable;
import java.util.Arrays;

import androidx.annotation.NonNull;

import static no.nordicsemi.android.mesh.utils.MeshParserUtils.bytesToInt;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.convert24BitsToInt;

/**
 * Device Property Characteristic
 */
public abstract class DevicePropertyCharacteristic<T extends Serializable> {
    protected T value;
    protected byte[] data; //raw data

    /**
     * Device Property Characteristic
     *
     * @param data   Byte array
     * @param offset Offset
     */
    public DevicePropertyCharacteristic(@NonNull final byte[] data, final int offset) {
        if (data.length - offset < getLength())
            throw new IllegalArgumentException("Invalid data length!");
        this.data = Arrays.copyOfRange(data, offset, offset + getLength());
    }

    /**
     * Device Property Characteristic
     *
     * @param data   Byte array
     * @param offset Offset
     * @param length Length
     */
    protected DevicePropertyCharacteristic(@NonNull final byte[] data, final int offset, final int length) {
        if (data.length - offset < length)
            throw new IllegalArgumentException("Invalid data length!");
        this.data = Arrays.copyOfRange(data, offset, offset + length);
    }

    /**
     * Returns the length of the characteristic in bytes.
     */
    public abstract int getLength();

    /**
     * Returns the value of the characteristic.
     */
    public abstract T getValue();

    /**
     * Parses the value
     *
     * @param data         Byte array
     * @param offset       Offset
     * @param length       Length
     * @param min          Lower bound
     * @param max          Upper bound
     * @param unknownValue Unsupported or unknown value
     */
    protected float parse(@NonNull final byte[] data, final int offset, final int length, final double min, final double max, final int unknownValue) {
        final int value;
        switch (length) {
            case 1:
                value = data[offset] & 0xFF;
                break;
            case 2:
            case 4:
                value = bytesToInt(Arrays.copyOfRange(data, offset, offset + length));
                break;
            case 3:
                value = convert24BitsToInt(Arrays.copyOfRange(data, offset, offset + length));
                break;
            default:
                throw new IllegalArgumentException("Invalid length");
        }
        if (value == unknownValue) {
            this.value = null;
        } else if (value < min && value > max) {
            throw new IllegalArgumentException("Value " + value + " is Prohibited!");
        }
        return value;
    }

}
