package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import androidx.annotation.NonNull;

/**
 * Abstract state class that handles Generic Message States
 */
abstract class GenericMessageState extends MeshMessageState {

    private static final String TAG = GenericMessageState.class.getSimpleName();


    GenericMessageState(@NonNull final Context context,
                        @NonNull final byte[] src,
                        @NonNull final byte[] dst,
                        @NonNull final MeshMessage meshMessage,
                        @NonNull final MeshTransport meshTransport,
                        @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, meshMessage, meshTransport, callbacks);
        if (src.length != 2)
            throw new IllegalArgumentException("Source address must be a unicast address with 2 bytes!");
        this.mSrc = src;
        if (dst.length != 2)
            throw new IllegalArgumentException("Destination address must be 2 bytes!");
        this.mDst = dst;
    }
}
