package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class LightHslSetupServer extends SigModel {

    public static final Creator<LightHslSetupServer> CREATOR = new Creator<LightHslSetupServer>() {
        @Override
        public LightHslSetupServer createFromParcel(final Parcel source) {
            return new LightHslSetupServer(source);
        }

        @Override
        public LightHslSetupServer[] newArray(final int size) {
            return new LightHslSetupServer[size];
        }
    };

    public LightHslSetupServer(final int modelId) {
        super(modelId);
    }

    private LightHslSetupServer(final Parcel source) {
        super(source);
    }

    @Override
    public String getModelName() {
        return "Light HSL Setup Server";
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
