package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;

public abstract class SigModel extends MeshModel {

    public static final int MODEL_ID_LENGTH = 2;

    SigModel(final int sigModelId) {
        super(sigModelId);
    }

    SigModel(final Parcel in) {
        super(in);
    }

    @Override
    public int getModelId() {
        return mModelId;
    }

}
