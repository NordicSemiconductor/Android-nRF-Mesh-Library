package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class LightXylServer extends SigModel {

    public static final Creator<LightXylServer> CREATOR = new Creator<LightXylServer>() {
        @Override
        public LightXylServer createFromParcel(final Parcel source) {
            return new LightXylServer((short) source.readInt());
        }

        @Override
        public LightXylServer[] newArray(final int size) {
            return new LightXylServer[size];
        }
    };

    public LightXylServer(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Light XYL Server";
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
