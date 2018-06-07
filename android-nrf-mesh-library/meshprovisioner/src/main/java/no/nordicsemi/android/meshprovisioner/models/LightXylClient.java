package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class LightXylClient extends SigModel {

    public static final Creator<LightXylClient> CREATOR = new Creator<LightXylClient>() {
        @Override
        public LightXylClient createFromParcel(final Parcel source) {
            return new LightXylClient((short) source.readInt());
        }

        @Override
        public LightXylClient[] newArray(final int size) {
            return new LightXylClient[size];
        }
    };

    public LightXylClient(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Light XYL Client";
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
