package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class SensorServer extends SigModel {

    public static final Creator<SensorServer> CREATOR = new Creator<SensorServer>() {
        @Override
        public SensorServer createFromParcel(final Parcel source) {
            return new SensorServer((short) source.readInt());
        }

        @Override
        public SensorServer[] newArray(final int size) {
            return new SensorServer[size];
        }
    };

    public SensorServer(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Sensor Server";
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
