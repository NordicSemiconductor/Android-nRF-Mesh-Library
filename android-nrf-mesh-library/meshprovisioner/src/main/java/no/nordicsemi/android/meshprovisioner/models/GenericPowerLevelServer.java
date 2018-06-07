package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class GenericPowerLevelServer extends SigModel {

    public static final Creator<GenericPowerLevelServer> CREATOR = new Creator<GenericPowerLevelServer>() {
        @Override
        public GenericPowerLevelServer createFromParcel(final Parcel source) {
            return new GenericPowerLevelServer((short) source.readInt());
        }

        @Override
        public GenericPowerLevelServer[] newArray(final int size) {
            return new GenericPowerLevelServer[size];
        }
    };

    public GenericPowerLevelServer(final int sigModelId) {
        super(sigModelId);
    }

    @Override
    public String getModelName() {
        return "Generic Power Level Server";
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
