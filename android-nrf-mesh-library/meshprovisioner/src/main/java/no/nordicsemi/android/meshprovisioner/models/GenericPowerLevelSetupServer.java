package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class GenericPowerLevelSetupServer extends SigModel {

    public static final Creator<GenericPowerLevelSetupServer> CREATOR = new Creator<GenericPowerLevelSetupServer>() {
        @Override
        public GenericPowerLevelSetupServer createFromParcel(final Parcel source) {
            return new GenericPowerLevelSetupServer(source);
        }

        @Override
        public GenericPowerLevelSetupServer[] newArray(final int size) {
            return new GenericPowerLevelSetupServer[size];
        }
    };

    public GenericPowerLevelSetupServer(final int sigModelId) {
        super(sigModelId);
    }

    private GenericPowerLevelSetupServer(final Parcel source) {
        super(source);
    }

    @Override
    public String getModelName() {
        return "Generic Power Level Setup Server";
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
