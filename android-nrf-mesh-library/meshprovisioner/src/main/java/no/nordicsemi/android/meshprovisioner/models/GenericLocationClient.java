package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class GenericLocationClient extends SigModel {

    public static final Creator<GenericLocationClient> CREATOR = new Creator<GenericLocationClient>() {
        @Override
        public GenericLocationClient createFromParcel(final Parcel source) {
            return new GenericLocationClient((short) source.readInt());
        }

        @Override
        public GenericLocationClient[] newArray(final int size) {
            return new GenericLocationClient[size];
        }
    };

    public GenericLocationClient(final int sigModelId) {
        super(sigModelId);
    }

    @Override
    public String getModelName() {
        return "Generic Location Client";
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
