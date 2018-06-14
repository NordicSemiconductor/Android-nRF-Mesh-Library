package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class GenericLocationSetupServer extends SigModel {

    public static final Creator<GenericLocationSetupServer> CREATOR = new Creator<GenericLocationSetupServer>() {
        @Override
        public GenericLocationSetupServer createFromParcel(final Parcel source) {
            return new GenericLocationSetupServer((short) source.readInt());
        }

        @Override
        public GenericLocationSetupServer[] newArray(final int size) {
            return new GenericLocationSetupServer[size];
        }
    };

    public GenericLocationSetupServer(final int sigModelId) {
        super(sigModelId);
    }

    @Override
    public String getModelName() {
        return "Generic Location Setup Server";
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
