package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;
import android.os.Parcelable;

public class GenericLevelClientModel extends SigModel {

    public static final Parcelable.Creator<GenericLevelClientModel> CREATOR = new Parcelable.Creator<GenericLevelClientModel>() {
        @Override
        public GenericLevelClientModel createFromParcel(final Parcel source) {
            return new GenericLevelClientModel((short) source.readInt());
        }

        @Override
        public GenericLevelClientModel[] newArray(final int size) {
            return new GenericLevelClientModel[size];
        }
    };

    public GenericLevelClientModel(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Generic Level Client";
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
