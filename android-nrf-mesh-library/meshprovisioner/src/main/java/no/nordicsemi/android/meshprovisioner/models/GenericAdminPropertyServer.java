package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class GenericAdminPropertyServer extends SigModel {

    public static final Creator<GenericAdminPropertyServer> CREATOR = new Creator<GenericAdminPropertyServer>() {
        @Override
        public GenericAdminPropertyServer createFromParcel(final Parcel source) {
            return new GenericAdminPropertyServer(source);
        }

        @Override
        public GenericAdminPropertyServer[] newArray(final int size) {
            return new GenericAdminPropertyServer[size];
        }
    };

    public GenericAdminPropertyServer(final int modelId) {
        super(modelId);
    }

    private GenericAdminPropertyServer(Parcel in){
        super(in);
    }

    @Override
    public String getModelName() {
        return "Generic Admin Property Server";
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
