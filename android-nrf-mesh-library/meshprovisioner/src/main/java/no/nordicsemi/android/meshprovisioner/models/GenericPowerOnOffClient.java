package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class GenericPowerOnOffClient extends SigModel {

    public static final Creator<GenericPowerOnOffClient> CREATOR = new Creator<GenericPowerOnOffClient>() {
        @Override
        public GenericPowerOnOffClient createFromParcel(final Parcel source) {
            return new GenericPowerOnOffClient(source);
        }

        @Override
        public GenericPowerOnOffClient[] newArray(final int size) {
            return new GenericPowerOnOffClient[size];
        }
    };

    public GenericPowerOnOffClient(final int sigModelId) {
        super(sigModelId);
    }

    private GenericPowerOnOffClient(final Parcel source) {
        super(source);
    }

    @Override
    public String getModelName() {
        return "Generic Power On Off Client";
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
