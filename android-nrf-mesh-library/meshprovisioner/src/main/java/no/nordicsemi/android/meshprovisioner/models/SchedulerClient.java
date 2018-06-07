package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class SchedulerClient extends SigModel {

    public static final Creator<SchedulerClient> CREATOR = new Creator<SchedulerClient>() {
        @Override
        public SchedulerClient createFromParcel(final Parcel source) {
            return new SchedulerClient((short) source.readInt());
        }

        @Override
        public SchedulerClient[] newArray(final int size) {
            return new SchedulerClient[size];
        }
    };

    public SchedulerClient(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Scheduler Client";
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
