package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;
import android.os.Parcelable;

public class GenericLevelServerModel extends SigModel {

    public static final Parcelable.Creator<GenericLevelServerModel> CREATOR = new Parcelable.Creator<GenericLevelServerModel>() {
        @Override
        public GenericLevelServerModel createFromParcel(final Parcel source) {
            return new GenericLevelServerModel(source);
        }

        @Override
        public GenericLevelServerModel[] newArray(final int size) {
            return new GenericLevelServerModel[size];
        }
    };

    public GenericLevelServerModel(final int modelId) {
        super(modelId);
    }

    private GenericLevelServerModel(final Parcel source) {
        super(source);
    }

    @Override
    public String getModelName() {
        return  "Generic Level Server";
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
