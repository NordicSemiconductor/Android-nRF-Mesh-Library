package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class GenericPowerLevelClient extends SigModel {

    public static final Creator<GenericPowerLevelClient> CREATOR = new Creator<GenericPowerLevelClient>() {
        @Override
        public GenericPowerLevelClient createFromParcel(final Parcel source) {
            return new GenericPowerLevelClient((short) source.readInt());
        }

        @Override
        public GenericPowerLevelClient[] newArray(final int size) {
            return new GenericPowerLevelClient[size];
        }
    };

    public GenericPowerLevelClient(final int sigModelId) {
        super(sigModelId);
    }

    @Override
    public String getModelName() {
        return "Generic Power Level Client";
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
