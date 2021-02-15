package no.nordicsemi.android.mesh.utils;

import androidx.annotation.NonNull;

/**
 * The Marshalled Sensor Data field represents the marshalled Sensor Data state.
 */
public class MarshalledSensorData {

    private final MarshalledPropertyId marshalledPropertyId;
    private final byte[] rawValues;

    /**
     * Constructs MarshalledSensorData.
     *
     * @param marshalledPropertyId {@link MarshalledPropertyId}
     * @param rawValues            Raw values.
     */
    public MarshalledSensorData(@NonNull final MarshalledPropertyId marshalledPropertyId, @NonNull final byte[] rawValues) {
        this.marshalledPropertyId = marshalledPropertyId;
        this.rawValues = rawValues;
    }

    /**
     * Returns the MarshalledPropertyId.
     */
    public MarshalledPropertyId getMarshalledPropertyId() {
        return marshalledPropertyId;
    }

    /**
     * Returns the raw values in the Marshalled Sensor Data.
     */
    public byte[] getRawValues() {
        return rawValues;
    }
}
