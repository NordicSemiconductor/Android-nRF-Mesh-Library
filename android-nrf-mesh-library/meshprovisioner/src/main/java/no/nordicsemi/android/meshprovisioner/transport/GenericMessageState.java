package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Abstract state class that handles Generic Message States
 */
abstract class GenericMessageState extends MeshMessageState {

    private static final String TAG = GenericMessageState.class.getSimpleName();
    final byte[] mDstAddress;

    GenericMessageState(@NonNull final Context context,
                               @NonNull final byte[] dstAddress,
                               @NonNull final MeshMessage meshMessage,
                               @NonNull final MeshTransport meshTransport,
                               @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, meshMessage, meshTransport, callbacks);
        if (dstAddress.length != 2)
            throw new IllegalArgumentException("Destination address must be 2 bytes!");
        this.mDstAddress = dstAddress;
    }
}
