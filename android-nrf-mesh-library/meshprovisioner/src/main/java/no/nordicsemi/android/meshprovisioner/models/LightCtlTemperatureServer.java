package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class LightCtlTemperatureServer extends SigModel {

    public static final Creator<LightCtlTemperatureServer> CREATOR = new Creator<LightCtlTemperatureServer>() {
        @Override
        public LightCtlTemperatureServer createFromParcel(final Parcel source) {
            return new LightCtlTemperatureServer(source);
        }

        @Override
        public LightCtlTemperatureServer[] newArray(final int size) {
            return new LightCtlTemperatureServer[size];
        }
    };

    public LightCtlTemperatureServer(final int modelId) {
        super(modelId);
    }

    private LightCtlTemperatureServer(final Parcel source) {
        super(source);
    }

    @Override
    public String getModelName() {
        return "Light Ctl Temperature Server";
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
