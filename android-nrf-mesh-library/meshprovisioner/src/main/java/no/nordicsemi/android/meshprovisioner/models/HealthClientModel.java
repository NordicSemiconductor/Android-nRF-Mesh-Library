package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;
import android.os.Parcelable;

public class HealthClientModel extends SigModel {

    public static final Parcelable.Creator<HealthClientModel> CREATOR = new Parcelable.Creator<HealthClientModel>() {
        @Override
        public HealthClientModel createFromParcel(final Parcel source) {
            return new HealthClientModel((short) source.readInt());
        }

        @Override
        public HealthClientModel[] newArray(final int size) {
            return new HealthClientModel[size];
        }
    };

    public HealthClientModel(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Health Client Model";
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
