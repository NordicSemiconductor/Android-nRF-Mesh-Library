package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class TimeClient extends SigModel {

    public static final Creator<TimeClient> CREATOR = new Creator<TimeClient>() {
        @Override
        public TimeClient createFromParcel(final Parcel source) {
            return new TimeClient((short) source.readInt());
        }

        @Override
        public TimeClient[] newArray(final int size) {
            return new TimeClient[size];
        }
    };

    public TimeClient(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Time Client";
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
