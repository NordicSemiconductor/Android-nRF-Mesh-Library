package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class GenericBatteryClient extends SigModel {

    public static final Creator<GenericBatteryClient> CREATOR = new Creator<GenericBatteryClient>() {
        @Override
        public GenericBatteryClient createFromParcel(final Parcel source) {
            return new GenericBatteryClient(source);
        }

        @Override
        public GenericBatteryClient[] newArray(final int size) {
            return new GenericBatteryClient[size];
        }
    };

    public GenericBatteryClient(final int sigModelId) {
        super(sigModelId);
    }

    public GenericBatteryClient(final Parcel source) {
        super(source);
    }

    @Override
    public String getModelName() {
        return "Generic Battery Client";
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
