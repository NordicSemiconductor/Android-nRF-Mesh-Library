package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class SceneServer extends SigModel {

    public static final Creator<SceneServer> CREATOR = new Creator<SceneServer>() {
        @Override
        public SceneServer createFromParcel(final Parcel source) {
            return new SceneServer((short) source.readInt());
        }

        @Override
        public SceneServer[] newArray(final int size) {
            return new SceneServer[size];
        }
    };

    public SceneServer(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Scene Server";
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
