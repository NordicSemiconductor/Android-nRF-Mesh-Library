package no.nordicsemi.android.meshprovisioner.meshmessagestates;

import android.content.Context;
import android.support.annotation.NonNull;

import no.nordicsemi.android.meshprovisioner.InternalMeshMsgHandlerCallbacks;

/**
 * Abstract state class that handles Generic Message States
 */
public abstract class GenericMessageState extends MeshMessageState {

    private static final String TAG = GenericMessageState.class.getSimpleName();
    final byte[] mDstAddress;

    public GenericMessageState(@NonNull final Context context,
                               @NonNull final byte[] dstAddress,
                               @NonNull final ProvisionedMeshNode provisionedMeshNode,
                               @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, provisionedMeshNode, callbacks);
        if (dstAddress.length != 2)
            throw new IllegalArgumentException("Destination address must be 2 bytes!");
        this.mDstAddress = dstAddress;
    }
}
