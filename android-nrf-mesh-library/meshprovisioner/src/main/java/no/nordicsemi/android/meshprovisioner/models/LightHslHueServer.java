package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class LightHslHueServer extends SigModel {

    public static final Creator<LightHslHueServer> CREATOR = new Creator<LightHslHueServer>() {
        @Override
        public LightHslHueServer createFromParcel(final Parcel source) {
            return new LightHslHueServer((short) source.readInt());
        }

        @Override
        public LightHslHueServer[] newArray(final int size) {
            return new LightHslHueServer[size];
        }
    };

    public LightHslHueServer(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Light HSL Hue Server";
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
