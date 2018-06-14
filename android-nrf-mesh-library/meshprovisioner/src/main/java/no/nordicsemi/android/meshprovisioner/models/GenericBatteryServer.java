package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class GenericBatteryServer extends SigModel {

    public static final Creator<GenericBatteryServer> CREATOR = new Creator<GenericBatteryServer>() {
        @Override
        public GenericBatteryServer createFromParcel(final Parcel source) {
            return new GenericBatteryServer(source);
        }

        @Override
        public GenericBatteryServer[] newArray(final int size) {
            return new GenericBatteryServer[size];
        }
    };

    public GenericBatteryServer(final int sigModelId) {
        super(sigModelId);
    }

    private GenericBatteryServer(final Parcel source) {
        super(source);
    }

    @Override
    public String getModelName() {
        return "Generic Battery Server";
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
