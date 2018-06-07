package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class LightLightnessSetupServer extends SigModel {

    public static final Creator<LightLightnessSetupServer> CREATOR = new Creator<LightLightnessSetupServer>() {
        @Override
        public LightLightnessSetupServer createFromParcel(final Parcel source) {
            return new LightLightnessSetupServer((short) source.readInt());
        }

        @Override
        public LightLightnessSetupServer[] newArray(final int size) {
            return new LightLightnessSetupServer[size];
        }
    };

    public LightLightnessSetupServer(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Light Lightness Setup Server";
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
