package no.nordicsemi.android.meshprovisioner.meshmessagestates;

import android.content.Context;

import no.nordicsemi.android.meshprovisioner.InternalMeshMsgHandlerCallbacks;

public abstract class GenericMessageState extends MeshMessageState {

    private static final String TAG = GenericMessageState.class.getSimpleName();

    public GenericMessageState(final Context context, final ProvisionedMeshNode provisionedMeshNode, final InternalMeshMsgHandlerCallbacks callbacks) {
        super(context, provisionedMeshNode, callbacks);
    }
}
