package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class LightCtlSetupServer extends SigModel {

    public static final Creator<LightCtlSetupServer> CREATOR = new Creator<LightCtlSetupServer>() {
        @Override
        public LightCtlSetupServer createFromParcel(final Parcel source) {
            return new LightCtlSetupServer(source);
        }

        @Override
        public LightCtlSetupServer[] newArray(final int size) {
            return new LightCtlSetupServer[size];
        }
    };

    public LightCtlSetupServer(final int modelId) {
        super(modelId);
    }

    private LightCtlSetupServer(final Parcel source) {
        super(source);
    }

    @Override
    public String getModelName() {
        return "Light Ctl Setup Server";
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
