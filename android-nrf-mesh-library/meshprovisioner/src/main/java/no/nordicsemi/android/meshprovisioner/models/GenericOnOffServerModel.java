package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;
import android.os.Parcelable;

public class GenericOnOffServerModel extends SigModel {

    public static final Parcelable.Creator<GenericOnOffServerModel> CREATOR = new Parcelable.Creator<GenericOnOffServerModel>() {
        @Override
        public GenericOnOffServerModel createFromParcel(final Parcel source) {
            return new GenericOnOffServerModel(source);
        }

        @Override
        public GenericOnOffServerModel[] newArray(final int size) {
            return new GenericOnOffServerModel[size];
        }
    };

    public GenericOnOffServerModel(final int modelId) {
        super(modelId);
    }

    private GenericOnOffServerModel(Parcel in){
        super(in);
    }

    @Override
    public String getModelName() {
        return "Generic On Off Server";
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
