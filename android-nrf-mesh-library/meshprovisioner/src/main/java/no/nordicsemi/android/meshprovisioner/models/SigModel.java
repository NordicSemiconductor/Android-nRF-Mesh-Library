package no.nordicsemi.android.meshprovisioner.models;

import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;

public abstract class SigModel extends MeshModel {

    public static final int MODEL_ID_LENGTH = 2;

    public SigModel(final int sigModelId) {
        super(sigModelId);
    }

    @Override
    public int getModelId() {
        return mModelId;
    }

}
