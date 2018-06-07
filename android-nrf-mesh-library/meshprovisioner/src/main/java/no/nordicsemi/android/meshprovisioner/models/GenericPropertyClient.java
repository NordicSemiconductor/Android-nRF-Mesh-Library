package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class GenericPropertyClient extends SigModel {

    public static final Creator<GenericPropertyClient> CREATOR = new Creator<GenericPropertyClient>() {
        @Override
        public GenericPropertyClient createFromParcel(final Parcel source) {
            return new GenericPropertyClient((short) source.readInt());
        }

        @Override
        public GenericPropertyClient[] newArray(final int size) {
            return new GenericPropertyClient[size];
        }
    };

    public GenericPropertyClient(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Generic User Property Client";
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
