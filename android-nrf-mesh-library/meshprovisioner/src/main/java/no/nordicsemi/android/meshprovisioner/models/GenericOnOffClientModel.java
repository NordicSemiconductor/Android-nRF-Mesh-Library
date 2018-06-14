package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;
import android.os.Parcelable;

public class GenericOnOffClientModel extends SigModel {

    public static final Parcelable.Creator<GenericOnOffClientModel> CREATOR = new Parcelable.Creator<GenericOnOffClientModel>() {
        @Override
        public GenericOnOffClientModel createFromParcel(final Parcel source) {
            return new GenericOnOffClientModel(source);
        }

        @Override
        public GenericOnOffClientModel[] newArray(final int size) {
            return new GenericOnOffClientModel[size];
        }
    };

    private GenericOnOffClientModel(Parcel in){
        super(in);
    }

    public GenericOnOffClientModel(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Generic On Off Client";
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
