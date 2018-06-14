package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;
import android.os.Parcelable;

public class GenericDefaultTransitionTimeServer extends SigModel {

    public static final Parcelable.Creator<GenericDefaultTransitionTimeServer> CREATOR = new Parcelable.Creator<GenericDefaultTransitionTimeServer>() {
        @Override
        public GenericDefaultTransitionTimeServer createFromParcel(final Parcel source) {
            return new GenericDefaultTransitionTimeServer((short) source.readInt());
        }

        @Override
        public GenericDefaultTransitionTimeServer[] newArray(final int size) {
            return new GenericDefaultTransitionTimeServer[size];
        }
    };

    public GenericDefaultTransitionTimeServer(final int sigModelId) {
        super(sigModelId);
    }

    @Override
    public String getModelName() {
        return "Generic Default Transition Timer Server";
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
