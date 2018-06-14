package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class LightLcServer extends SigModel {

    public static final Creator<LightLcServer> CREATOR = new Creator<LightLcServer>() {
        @Override
        public LightLcServer createFromParcel(final Parcel source) {
            return new LightLcServer((short) source.readInt());
        }

        @Override
        public LightLcServer[] newArray(final int size) {
            return new LightLcServer[size];
        }
    };

    public LightLcServer(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Light LC Server";
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
