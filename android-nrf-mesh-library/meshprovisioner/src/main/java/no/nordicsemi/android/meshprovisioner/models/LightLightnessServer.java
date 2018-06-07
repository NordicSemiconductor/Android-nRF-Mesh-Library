package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class LightLightnessServer extends SigModel {

    public static final Creator<LightLightnessServer> CREATOR = new Creator<LightLightnessServer>() {
        @Override
        public LightLightnessServer createFromParcel(final Parcel source) {
            return new LightLightnessServer((short) source.readInt());
        }

        @Override
        public LightLightnessServer[] newArray(final int size) {
            return new LightLightnessServer[size];
        }
    };

    public LightLightnessServer(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Light Lightness Server";
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
