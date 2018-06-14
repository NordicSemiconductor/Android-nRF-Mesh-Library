package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ConfigurationServer extends SigModel {

    public static final Parcelable.Creator<ConfigurationServer> CREATOR = new Parcelable.Creator<ConfigurationServer>() {
        @Override
        public ConfigurationServer createFromParcel(final Parcel source) {
            return new ConfigurationServer(source);
        }

        @Override
        public ConfigurationServer[] newArray(final int size) {
            return new ConfigurationServer[size];
        }
    };

    public ConfigurationServer(final int modelId) {
        super(modelId);
    }

    private ConfigurationServer(Parcel in){
        super(in);
    }

    @Override
    public String getModelName() {
        return "Configuration Server";
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
