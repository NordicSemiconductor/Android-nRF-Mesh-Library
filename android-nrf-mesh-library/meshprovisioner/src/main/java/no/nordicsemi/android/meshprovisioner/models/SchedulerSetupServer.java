package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class SchedulerSetupServer extends SigModel {

    public static final Creator<SchedulerSetupServer> CREATOR = new Creator<SchedulerSetupServer>() {
        @Override
        public SchedulerSetupServer createFromParcel(final Parcel source) {
            return new SchedulerSetupServer((short) source.readInt());
        }

        @Override
        public SchedulerSetupServer[] newArray(final int size) {
            return new SchedulerSetupServer[size];
        }
    };

    public SchedulerSetupServer(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Scheduler Setup Server";
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
