package no.nordicsemi.android.mesh.sensorutils;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

import androidx.annotation.NonNull;

/**
 * The Marshalled Sensor Data field represents the marshalled Sensor Data state.
 */
public class MarshalledSensorData implements Parcelable {

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

    protected MarshalledSensorData(Parcel in) {
        marshalledPropertyId = in.readParcelable(MarshalledPropertyId.class.getClassLoader());
        rawValues = in.createByteArray();
    }

    public static final Creator<MarshalledSensorData> CREATOR = new Creator<MarshalledSensorData>() {
        @Override
        public MarshalledSensorData createFromParcel(Parcel in) {
            return new MarshalledSensorData(in);
        }

        @Override
        public MarshalledSensorData[] newArray(int size) {
            return new MarshalledSensorData[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeParcelable(marshalledPropertyId, flags);
        dest.writeByteArray(rawValues);
    }

    @Override
    public String toString() {
        return "MarshalledSensorData{" +
                "marshalledPropertyId=" + marshalledPropertyId +
                ", rawValues=" + Arrays.toString(rawValues) +
                '}';
    }
}
