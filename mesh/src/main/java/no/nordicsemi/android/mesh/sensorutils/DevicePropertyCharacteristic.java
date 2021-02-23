package no.nordicsemi.android.mesh.sensorutils;

import java.io.Serializable;
import java.util.Arrays;

import androidx.annotation.NonNull;

/**
 * Device Property Characteristic
 */
public abstract class DevicePropertyCharacteristic<T extends Serializable> {
    protected T value;
    protected byte[] data; //raw data

    public DevicePropertyCharacteristic(@NonNull final byte[] data, final int offset) {
        this.data = Arrays.copyOfRange(data, offset, offset + getLength());
    }

    public DevicePropertyCharacteristic(@NonNull final byte[] data, final int offset, final int length) {
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

    public byte[] parse(@NonNull final byte[] data, final int offset, final int length) {
        this.data = Arrays.copyOfRange(data, offset, offset + getLength());
        return this.data;
    }

}
