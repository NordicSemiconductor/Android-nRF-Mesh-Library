package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling LightCtlSetUnacknowledged messages.
 */
@SuppressWarnings("WeakerAccess")
class LightCtlSetUnacknowledgedState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = LightCtlSetUnacknowledgedState.class.getSimpleName();

    /**
     * Constructs {@link LightCtlSetUnacknowledgedState}
     *
     * @param context                   Context of the application
     * @param src                       Source address
     * @param dst                       Destination address to which the message must be sent to
     * @param lightCtlSetUnacknowledged Wrapper class {@link LightCtlSetUnacknowledged} containing the opcode and parameters for {@link LightCtlSetUnacknowledged} message
     * @param callbacks                 {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     * @deprecated in favour of {@link #LightCtlSetUnacknowledgedState(Context, int, int, LightCtlSetUnacknowledged, MeshTransport, InternalMeshMsgHandlerCallbacks)}
     */
    @Deprecated
    LightCtlSetUnacknowledgedState(@NonNull final Context context,
                                   @NonNull final byte[] src,
                                   @NonNull final byte[] dst,
                                   @NonNull final LightCtlSetUnacknowledged lightCtlSetUnacknowledged,
                                   @NonNull final MeshTransport meshTransport,
                                   @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), lightCtlSetUnacknowledged, meshTransport, callbacks);
    }

    /**
     * Constructs {@link LightCtlSetUnacknowledgedState}
     *
     * @param context                   Context of the application
     * @param src                       Source address
     * @param dst                       Destination address to which the message must be sent to
     * @param lightCtlSetUnacknowledged Wrapper class {@link LightCtlSetUnacknowledged} containing the opcode and parameters for {@link LightCtlSetUnacknowledged} message
     * @param callbacks                 {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    LightCtlSetUnacknowledgedState(@NonNull final Context context,
                                   final int src,
                                   final int dst,
                                   @NonNull final LightCtlSetUnacknowledged lightCtlSetUnacknowledged,
                                   @NonNull final MeshTransport meshTransport,
                                   @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, lightCtlSetUnacknowledged, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.LIGHT_CTL_SET_UNACKNOWLEDGED_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final LightCtlSetUnacknowledged lightCtlSetUnacknowledged = (LightCtlSetUnacknowledged) mMeshMessage;
        final byte[] key = lightCtlSetUnacknowledged.getAppKey();
        final int akf = lightCtlSetUnacknowledged.getAkf();
        final int aid = lightCtlSetUnacknowledged.getAid();
        final int aszmic = lightCtlSetUnacknowledged.getAszmic();
        final int opCode = lightCtlSetUnacknowledged.getOpCode();
        final byte[] parameters = lightCtlSetUnacknowledged.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, key, akf, aid, aszmic, opCode, parameters);
        lightCtlSetUnacknowledged.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending light ctl set acknowledged ");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null) {
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
            }
        }
    }
}
