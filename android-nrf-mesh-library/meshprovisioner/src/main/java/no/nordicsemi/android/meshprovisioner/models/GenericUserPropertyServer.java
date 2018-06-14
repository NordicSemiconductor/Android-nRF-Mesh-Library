package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class GenericUserPropertyServer extends SigModel {

    public static final Creator<GenericUserPropertyServer> CREATOR = new Creator<GenericUserPropertyServer>() {
        @Override
        public GenericUserPropertyServer createFromParcel(final Parcel source) {
            return new GenericUserPropertyServer((short) source.readInt());
        }

        @Override
        public GenericUserPropertyServer[] newArray(final int size) {
            return new GenericUserPropertyServer[size];
        }
    };

    public GenericUserPropertyServer(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Generic User Property Server";
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
