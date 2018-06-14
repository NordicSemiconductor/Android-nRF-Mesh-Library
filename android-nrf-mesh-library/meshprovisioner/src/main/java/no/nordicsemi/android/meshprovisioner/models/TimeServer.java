package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class TimeServer extends SigModel {

    public static final Creator<TimeServer> CREATOR = new Creator<TimeServer>() {
        @Override
        public TimeServer createFromParcel(final Parcel source) {
            return new TimeServer(source);
        }

        @Override
        public TimeServer[] newArray(final int size) {
            return new TimeServer[size];
        }
    };

    public TimeServer(final int modelId) {
        super(modelId);
    }

    private TimeServer(final Parcel source) {
        super(source);
    }

    @Override
    public String getModelName() {
        return "Time Server";
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
