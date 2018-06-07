package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ConfigurationClientModel extends SigModel {

    public static final Parcelable.Creator<ConfigurationClientModel> CREATOR = new Parcelable.Creator<ConfigurationClientModel>() {
        @Override
        public ConfigurationClientModel createFromParcel(final Parcel source) {
            return new ConfigurationClientModel((short) source.readInt());
        }

        @Override
        public ConfigurationClientModel[] newArray(final int size) {
            return new ConfigurationClientModel[size];
        }
    };

    public ConfigurationClientModel(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Configuration Client Model";
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
