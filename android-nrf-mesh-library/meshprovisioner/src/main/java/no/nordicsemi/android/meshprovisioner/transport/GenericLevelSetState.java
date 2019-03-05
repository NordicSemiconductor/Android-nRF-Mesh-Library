package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling GenericLevelSet messages.
 */
@SuppressWarnings("WeakerAccess")
class GenericLevelSetState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = GenericLevelSetState.class.getSimpleName();

    /**
     * Constructs {@link GenericLevelSetState}
     *
     * @param context         Context of the application
     * @param src             Source address
     * @param dst             Destination address to which the message must be sent to
     * @param genericLevelSet Wrapper class {@link GenericLevelSet} containing the opcode and parameters for {@link GenericLevelSet} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     * @deprecated in favour of {@link GenericLevelSet}
     */
    @Deprecated
    GenericLevelSetState(@NonNull final Context context,
                         @NonNull final byte[] src,
                         @NonNull final byte[] dst,
                         @NonNull final GenericLevelSet genericLevelSet,
                         @NonNull final MeshTransport meshTransport,
                         @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), genericLevelSet, meshTransport, callbacks);
    }

    /**
     * Constructs {@link GenericLevelSetState}
     *
     * @param context         Context of the application
     * @param src             Source address
     * @param dst             Destination address to which the message must be sent to
     * @param genericLevelSet Wrapper class {@link GenericLevelSet} containing the opcode and parameters for {@link GenericLevelSet} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    GenericLevelSetState(@NonNull final Context context,
                         final int src,
                         final int dst,
                         @NonNull final GenericLevelSet genericLevelSet,
                         @NonNull final MeshTransport meshTransport,
                         @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, genericLevelSet, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.GENERIC_LEVEL_SET_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final GenericLevelSet genericLevelSet = (GenericLevelSet) mMeshMessage;
        final byte[] key = genericLevelSet.getAppKey();
        final int akf = genericLevelSet.getAkf();
        final int aid = genericLevelSet.getAid();
        final int aszmic = genericLevelSet.getAszmic();
        final int opCode = genericLevelSet.getOpCode();
        final byte[] parameters = genericLevelSet.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, key, akf, aid, aszmic, opCode, parameters);
        genericLevelSet.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending Generic Level set acknowledged ");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
        }
    }
}
