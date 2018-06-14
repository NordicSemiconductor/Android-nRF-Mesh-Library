package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class LightClient extends SigModel {

    public static final Creator<LightClient> CREATOR = new Creator<LightClient>() {
        @Override
        public LightClient createFromParcel(final Parcel source) {
            return new LightClient((short) source.readInt());
        }

        @Override
        public LightClient[] newArray(final int size) {
            return new LightClient[size];
        }
    };

    public LightClient(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Light Client";
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
