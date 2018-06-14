package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class SensorSetupServer extends SigModel {

    public static final Creator<SensorSetupServer> CREATOR = new Creator<SensorSetupServer>() {
        @Override
        public SensorSetupServer createFromParcel(final Parcel source) {
            return new SensorSetupServer((short) source.readInt());
        }

        @Override
        public SensorSetupServer[] newArray(final int size) {
            return new SensorSetupServer[size];
        }
    };

    public SensorSetupServer(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Sensor Setup Server";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        super.parcelMeshModel(dest, flags);
    }
}
