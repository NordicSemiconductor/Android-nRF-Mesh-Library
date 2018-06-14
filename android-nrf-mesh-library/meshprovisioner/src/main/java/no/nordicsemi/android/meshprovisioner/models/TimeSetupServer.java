package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class TimeSetupServer extends SigModel {

    public static final Creator<TimeSetupServer> CREATOR = new Creator<TimeSetupServer>() {
        @Override
        public TimeSetupServer createFromParcel(final Parcel source) {
            return new TimeSetupServer((short) source.readInt());
        }

        @Override
        public TimeSetupServer[] newArray(final int size) {
            return new TimeSetupServer[size];
        }
    };

    public TimeSetupServer(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Time Setup Server";
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
