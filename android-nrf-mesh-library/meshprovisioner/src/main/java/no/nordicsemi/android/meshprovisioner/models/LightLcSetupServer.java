package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class LightLcSetupServer extends SigModel {

    public static final Creator<LightLcSetupServer> CREATOR = new Creator<LightLcSetupServer>() {
        @Override
        public LightLcSetupServer createFromParcel(final Parcel source) {
            return new LightLcSetupServer(source);
        }

        @Override
        public LightLcSetupServer[] newArray(final int size) {
            return new LightLcSetupServer[size];
        }
    };

    public LightLcSetupServer(final int modelId) {
        super(modelId);
    }

    private LightLcSetupServer(final Parcel source) {
        super(source);
    }

    @Override
    public String getModelName() {
        return "Light LC Setup Server";
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
