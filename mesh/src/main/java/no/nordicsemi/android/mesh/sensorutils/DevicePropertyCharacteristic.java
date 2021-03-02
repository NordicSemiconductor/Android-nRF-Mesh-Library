package no.nordicsemi.android.mesh.sensorutils;

import androidx.annotation.NonNull;

/**
 * Device Property Characteristic
 */
public abstract class DevicePropertyCharacteristic<T> {
    protected T value;
    private static final String TAG = DevicePropertyCharacteristic.class.getSimpleName();

    DevicePropertyCharacteristic() {

    }

    /**
     * Device Property Characteristic
     *
     * @param data   Byte array
     * @param offset Offset
     */
    protected DevicePropertyCharacteristic(@NonNull final byte[] data, final int offset) {
        if (data.length - offset < getLength())
            throw new IllegalArgumentException("Invalid data length!");
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
    }

    /**
     * Returns the length of the characteristic in bytes.
     */
    public abstract int getLength();

    /**
     * Returns the value of the charactelristic.
     */
    public final T getValue() {
        return value;
    }

    /**
     * Returns the byte array.
     */
    public abstract byte[] getBytes();
}
