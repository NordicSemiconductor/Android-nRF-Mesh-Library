package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class GenericDefaultTransitionTimeClient extends SigModel {

    public static final Creator<GenericDefaultTransitionTimeClient> CREATOR = new Creator<GenericDefaultTransitionTimeClient>() {
        @Override
        public GenericDefaultTransitionTimeClient createFromParcel(final Parcel source) {
            return new GenericDefaultTransitionTimeClient(source);
        }

        @Override
        public GenericDefaultTransitionTimeClient[] newArray(final int size) {
            return new GenericDefaultTransitionTimeClient[size];
        }
    };

    public GenericDefaultTransitionTimeClient(final int sigModelId) {
        super(sigModelId);
    }

    public GenericDefaultTransitionTimeClient(final Parcel source) {
        super(source);
    }

    @Override
    public String getModelName() {
        return "Generic Default Transition Timer Client";
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
