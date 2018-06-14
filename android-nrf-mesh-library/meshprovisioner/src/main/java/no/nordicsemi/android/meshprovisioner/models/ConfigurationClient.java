package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ConfigurationClient extends SigModel {

    public static final Parcelable.Creator<ConfigurationClient> CREATOR = new Parcelable.Creator<ConfigurationClient>() {
        @Override
        public ConfigurationClient createFromParcel(final Parcel source) {
            return new ConfigurationClient(source);
        }

        @Override
        public ConfigurationClient[] newArray(final int size) {
            return new ConfigurationClient[size];
        }
    };

    public ConfigurationClient(final int modelId) {
        super(modelId);
    }

    private ConfigurationClient(Parcel in){
        super(in);
    }

    @Override
    public String getModelName() {
        return "Configuration Client";
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
