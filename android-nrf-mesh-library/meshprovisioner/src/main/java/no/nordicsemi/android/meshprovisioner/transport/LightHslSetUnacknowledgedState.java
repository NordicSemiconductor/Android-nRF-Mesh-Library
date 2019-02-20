package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling LightCtlSetState messages.
 */
@SuppressWarnings("WeakerAccess")
class LightHslSetUnacknowledgedState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = LightHslSetUnacknowledgedState.class.getSimpleName();

    /**
     * Constructs {@link LightHslSetUnacknowledgedState}
     *
     * @param context                   Context of the application
     * @param src                       Source address
     * @param dst                       Destination address to which the message must be sent to
     * @param lightHslSetUnacknowledged Wrapper class {@link LightLightnessSet} containing the opcode and parameters for {@link GenericLevelSet} message
     * @param callbacks                 {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     * @deprecated  in favour of {@link #LightHslSetUnacknowledgedState(Context, int, int, LightHslSetUnacknowledged, MeshTransport, InternalMeshMsgHandlerCallbacks)}
     */
    @Deprecated
    LightHslSetUnacknowledgedState(@NonNull final Context context,
                                   @NonNull final byte[] src,
                                   @NonNull final byte[] dst,
                                   @NonNull final LightHslSetUnacknowledged lightHslSetUnacknowledged,
                                   @NonNull final MeshTransport meshTransport,
                                   @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), lightHslSetUnacknowledged, meshTransport, callbacks);
    }

    /**
     * Constructs {@link LightHslSetUnacknowledgedState}
     *
     * @param context                   Context of the application
     * @param src                       Source address
     * @param dst                       Destination address to which the message must be sent to
     * @param lightHslSetUnacknowledged Wrapper class {@link LightLightnessSet} containing the opcode and parameters for {@link GenericLevelSet} message
     * @param callbacks                 {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    LightHslSetUnacknowledgedState(@NonNull final Context context,
                                   final int src,
                                   final int dst,
                                   @NonNull final LightHslSetUnacknowledged lightHslSetUnacknowledged,
                                   @NonNull final MeshTransport meshTransport,
                                   @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, lightHslSetUnacknowledged, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.LIGHT_HSL_SET_UNACKNOWLEDGED_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final LightHslSetUnacknowledged lightHslSetUnacknowledged = (LightHslSetUnacknowledged) mMeshMessage;
        final byte[] key = lightHslSetUnacknowledged.getAppKey();
        final int akf = lightHslSetUnacknowledged.getAkf();
        final int aid = lightHslSetUnacknowledged.getAid();
        final int aszmic = lightHslSetUnacknowledged.getAszmic();
        final int opCode = lightHslSetUnacknowledged.getOpCode();
        final byte[] parameters = lightHslSetUnacknowledged.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, key, akf, aid, aszmic, opCode, parameters);
        lightHslSetUnacknowledged.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending light hsl set acknowledged ");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null) {
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
            }
        }
    }
}
