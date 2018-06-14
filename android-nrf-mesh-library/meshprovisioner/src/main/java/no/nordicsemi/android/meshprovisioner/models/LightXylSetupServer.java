package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class LightXylSetupServer extends SigModel {

    public static final Creator<LightXylSetupServer> CREATOR = new Creator<LightXylSetupServer>() {
        @Override
        public LightXylSetupServer createFromParcel(final Parcel source) {
            return new LightXylSetupServer(source);
        }

        @Override
        public LightXylSetupServer[] newArray(final int size) {
            return new LightXylSetupServer[size];
        }
    };

    public LightXylSetupServer(final int modelId) {
        super(modelId);
    }

    private LightXylSetupServer(final Parcel source) {
        super(source);
    }

    @Override
    public String getModelName() {
        return "Light XYL Setup Server";
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
