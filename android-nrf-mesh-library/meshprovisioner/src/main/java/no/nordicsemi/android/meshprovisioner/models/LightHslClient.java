package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class LightHslClient extends SigModel {

    public static final Creator<LightHslClient> CREATOR = new Creator<LightHslClient>() {
        @Override
        public LightHslClient createFromParcel(final Parcel source) {
            return new LightHslClient((short) source.readInt());
        }

        @Override
        public LightHslClient[] newArray(final int size) {
            return new LightHslClient[size];
        }
    };

    public LightHslClient(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Light HSL Client";
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
