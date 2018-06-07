package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class GenericPowerOnOffSetupServer extends SigModel {

    public static final Creator<GenericPowerOnOffSetupServer> CREATOR = new Creator<GenericPowerOnOffSetupServer>() {
        @Override
        public GenericPowerOnOffSetupServer createFromParcel(final Parcel source) {
            return new GenericPowerOnOffSetupServer((short) source.readInt());
        }

        @Override
        public GenericPowerOnOffSetupServer[] newArray(final int size) {
            return new GenericPowerOnOffSetupServer[size];
        }
    };

    public GenericPowerOnOffSetupServer(final int sigModelId) {
        super(sigModelId);
    }

    @Override
    public String getModelName() {
        return "Generic Power On Off Setup Server";
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
