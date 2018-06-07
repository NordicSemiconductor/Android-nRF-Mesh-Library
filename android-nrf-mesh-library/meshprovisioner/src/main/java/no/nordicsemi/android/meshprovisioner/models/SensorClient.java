package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class SensorClient extends SigModel {

    public static final Creator<SensorClient> CREATOR = new Creator<SensorClient>() {
        @Override
        public SensorClient createFromParcel(final Parcel source) {
            return new SensorClient((short) source.readInt());
        }

        @Override
        public SensorClient[] newArray(final int size) {
            return new SensorClient[size];
        }
    };

    public SensorClient(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Sensor Client";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(mModelId);
    }
}
