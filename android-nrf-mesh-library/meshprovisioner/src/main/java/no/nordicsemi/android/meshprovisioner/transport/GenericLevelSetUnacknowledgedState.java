package no.nordicsemi.android.meshprovisioner.transport;


import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

/**
 * State class for handling unacknowledged GenericLevelSet messages.
 */
class GenericLevelSetUnacknowledgedState extends GenericMessageState {

    private static final String TAG = GenericLevelSetState.class.getSimpleName();

    /**
     * Constructs {@link GenericLevelSetUnacknowledgedState}
     *
     * @param context                Context of the application
     * @param src                    Source address
     * @param dst                    Destination address to which the message must be sent to
     * @param genericLevelSetUnacked Wrapper class {@link GenericLevelSetUnacknowledged} containing the opcode and parameters for {@link GenericLevelSetUnacknowledged} message
     * @param callbacks              {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    GenericLevelSetUnacknowledgedState(@NonNull final Context context,
                                       @NonNull final byte[] src,
                                       @NonNull final byte[] dst,
                                       @NonNull final GenericLevelSetUnacknowledged genericLevelSetUnacked,
                                       @NonNull final MeshTransport meshTransport,
                                       @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, genericLevelSetUnacked, meshTransport, callbacks);
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
        final GenericLevelSetUnacknowledged genericLevelSet = (GenericLevelSetUnacknowledged) mMeshMessage;
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
            if (mMeshStatusCallbacks != null) {
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
            }
        }
    }
}
