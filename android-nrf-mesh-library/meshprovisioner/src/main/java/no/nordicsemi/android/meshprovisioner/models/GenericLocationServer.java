package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class GenericLocationServer extends SigModel {

    public static final Creator<GenericLocationServer> CREATOR = new Creator<GenericLocationServer>() {
        @Override
        public GenericLocationServer createFromParcel(final Parcel source) {
            return new GenericLocationServer((short) source.readInt());
        }

        @Override
        public GenericLocationServer[] newArray(final int size) {
            return new GenericLocationServer[size];
        }
    };

    public GenericLocationServer(final int sigModelId) {
        super(sigModelId);
    }

    @Override
    public String getModelName() {
        return "Generic Location Server";
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
