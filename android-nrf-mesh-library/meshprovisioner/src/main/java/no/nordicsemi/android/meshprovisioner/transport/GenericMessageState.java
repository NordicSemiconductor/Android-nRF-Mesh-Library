package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;

import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;

/**
 * Abstract state class that handles Generic Message States
 */
abstract class GenericMessageState extends MeshMessageState {

    private static final String TAG = GenericMessageState.class.getSimpleName();


    GenericMessageState(@NonNull final Context context,
                        final int src,
                        final int dst,
                        @NonNull final MeshMessage meshMessage,
                        @NonNull final MeshTransport meshTransport,
                        @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, meshMessage, meshTransport, callbacks);
        if (!MeshAddress.isAddressInRange(src))
            throw new IllegalArgumentException("Invalid address, a source address must be a valid 16-bit value!");
        this.mSrc = src;
        if (!MeshAddress.isAddressInRange(dst))
            throw new IllegalArgumentException("Invalid address, a destination address must be a valid 16-bit value");
        this.mDst = dst;
    }
}
