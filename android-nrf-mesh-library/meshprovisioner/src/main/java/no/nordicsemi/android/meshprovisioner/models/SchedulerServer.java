package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class SchedulerServer extends SigModel {

    public static final Creator<SchedulerServer> CREATOR = new Creator<SchedulerServer>() {
        @Override
        public SchedulerServer createFromParcel(final Parcel source) {
            return new SchedulerServer(source);
        }

        @Override
        public SchedulerServer[] newArray(final int size) {
            return new SchedulerServer[size];
        }
    };

    public SchedulerServer(final int modelId) {
        super(modelId);
    }

    private SchedulerServer(final Parcel source) {
        super(source);
    }

    @Override
    public String getModelName() {
        return "Scheduler Server";
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
