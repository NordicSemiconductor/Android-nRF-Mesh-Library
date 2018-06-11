package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;
import android.os.Parcelable;

public class HealthServerModel extends SigModel {

    public static final Parcelable.Creator<HealthServerModel> CREATOR = new Parcelable.Creator<HealthServerModel>() {
        @Override
        public HealthServerModel createFromParcel(final Parcel source) {
            return new HealthServerModel((short) source.readInt());
        }

        @Override
        public HealthServerModel[] newArray(final int size) {
            return new HealthServerModel[size];
        }
    };

    public HealthServerModel(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Health Server";
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
