package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class LightCtlClient extends SigModel {

    public static final Creator<LightCtlClient> CREATOR = new Creator<LightCtlClient>() {
        @Override
        public LightCtlClient createFromParcel(final Parcel source) {
            return new LightCtlClient((short) source.readInt());
        }

        @Override
        public LightCtlClient[] newArray(final int size) {
            return new LightCtlClient[size];
        }
    };

    public LightCtlClient(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Light Ctl Client";
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
