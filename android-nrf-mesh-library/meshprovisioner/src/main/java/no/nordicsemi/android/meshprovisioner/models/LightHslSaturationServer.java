package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class LightHslSaturationServer extends SigModel {

    public static final Creator<LightHslSaturationServer> CREATOR = new Creator<LightHslSaturationServer>() {
        @Override
        public LightHslSaturationServer createFromParcel(final Parcel source) {
            return new LightHslSaturationServer(source);
        }

        @Override
        public LightHslSaturationServer[] newArray(final int size) {
            return new LightHslSaturationServer[size];
        }
    };

    public LightHslSaturationServer(final int modelId) {
        super(modelId);
    }

    private LightHslSaturationServer(final Parcel source) {
        super(source);
    }

    @Override
    public String getModelName() {
        return "Light HSL Saturation Server";
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
