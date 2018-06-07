package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class GenericClientPropertyServer extends SigModel {

    public static final Creator<GenericClientPropertyServer> CREATOR = new Creator<GenericClientPropertyServer>() {
        @Override
        public GenericClientPropertyServer createFromParcel(final Parcel source) {
            return new GenericClientPropertyServer((short) source.readInt());
        }

        @Override
        public GenericClientPropertyServer[] newArray(final int size) {
            return new GenericClientPropertyServer[size];
        }
    };

    public GenericClientPropertyServer(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Generic Client Property Server";
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
