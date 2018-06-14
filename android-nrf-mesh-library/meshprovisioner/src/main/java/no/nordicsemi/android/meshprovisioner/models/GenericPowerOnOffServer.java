package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class GenericPowerOnOffServer extends SigModel {

    public static final Creator<GenericPowerOnOffServer> CREATOR = new Creator<GenericPowerOnOffServer>() {
        @Override
        public GenericPowerOnOffServer createFromParcel(final Parcel source) {
            return new GenericPowerOnOffServer(source);
        }

        @Override
        public GenericPowerOnOffServer[] newArray(final int size) {
            return new GenericPowerOnOffServer[size];
        }
    };

    public GenericPowerOnOffServer(final int sigModelId) {
        super(sigModelId);
    }

    private GenericPowerOnOffServer(final Parcel source) {
        super(source);
    }

    @Override
    public String getModelName() {
        return "Generic Power On Off Server";
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
