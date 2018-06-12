package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ConfigurationServerModel extends SigModel {

    public static final Parcelable.Creator<ConfigurationServerModel> CREATOR = new Parcelable.Creator<ConfigurationServerModel>() {
        @Override
        public ConfigurationServerModel createFromParcel(final Parcel source) {
            return new ConfigurationServerModel((short) source.readInt());
        }

        @Override
        public ConfigurationServerModel[] newArray(final int size) {
            return new ConfigurationServerModel[size];
        }
    };

    public ConfigurationServerModel(final int modelId) {
        super(modelId);
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
        dest.writeInt(mModelId);
    }
}
