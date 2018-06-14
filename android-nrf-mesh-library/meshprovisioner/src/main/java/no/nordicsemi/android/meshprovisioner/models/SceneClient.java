package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class SceneClient extends SigModel {

    public static final Creator<SceneClient> CREATOR = new Creator<SceneClient>() {
        @Override
        public SceneClient createFromParcel(final Parcel source) {
            return new SceneClient(source);
        }

        @Override
        public SceneClient[] newArray(final int size) {
            return new SceneClient[size];
        }
    };

    public SceneClient(final int modelId) {
        super(modelId);
    }

    private SceneClient(final Parcel source) {
        super(source);
    }

    @Override
    public String getModelName() {
        return "Scene Client";
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
