package no.nordicsemi.android.mesh.utils;

import androidx.annotation.NonNull;

/**
 * A Marshalled Property ID (MPID) is a concatenation of a 1-bit Format field,
 * a 4-bit or 7-bit Length of the Property Value field, and an 11-bit or 16-bit Property ID.
 */
public class MarshalledPropertyId {
    private final Format format;
    private final int length;
    private final DeviceProperty propertyId;

    /**
     * Constructs a MarshalledPropertyID
     *
     * @param format {@link Format}
     * @param length      length of the property value
     * @param propertyId  property id
     */
    public MarshalledPropertyId(@NonNull final Format format, final int length, final DeviceProperty propertyId) {
        this.format = format;
        this.length = length;
        this.propertyId = propertyId;
    }

    /**
     * Returns the format for the Sensor Data
     *
     * @return {@link Format}
     */
    public Format getFormat() {
        return format;
    }

    /**
     * Returns the length, a 4-bit or 7-bit value defining the length of the property value.
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns the device property
     **/
    public DeviceProperty getPropertyId() {
        return propertyId;
    }
}
