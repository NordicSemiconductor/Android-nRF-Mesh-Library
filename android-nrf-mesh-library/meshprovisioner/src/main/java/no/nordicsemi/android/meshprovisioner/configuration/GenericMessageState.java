package no.nordicsemi.android.meshprovisioner.configuration;

import android.content.Context;

public abstract class GenericMessageState extends MeshMessageState {

    private static final String TAG = GenericMessageState.class.getSimpleName();

    public GenericMessageState(final Context context, final ProvisionedMeshNode provisionedMeshNode) {
        super(context, provisionedMeshNode);
    }
}
