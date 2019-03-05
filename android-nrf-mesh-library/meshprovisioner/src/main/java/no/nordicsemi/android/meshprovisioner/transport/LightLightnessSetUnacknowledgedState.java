package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling GenericLevelSet messages.
 */
@SuppressWarnings("WeakerAccess")
class LightLightnessSetUnacknowledgedState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = LightLightnessSetUnacknowledgedState.class.getSimpleName();

    /**
     * Constructs {@link LightLightnessSetUnacknowledgedState}
     *
     * @param context                         Context of the application
     * @param src                             Source address
     * @param dst                             Destination address to which the message must be sent to
     * @param lightLightnessSetUnacknowledged Wrapper class {@link LightLightnessSetUnacknowledged} containing the opcode and parameters for {@link GenericLevelSet} message
     * @param callbacks                       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    @Deprecated
    LightLightnessSetUnacknowledgedState(@NonNull final Context context,
                                         @NonNull final byte[] src,
                                         @NonNull final byte[] dst,
                                         @NonNull final LightLightnessSetUnacknowledged lightLightnessSetUnacknowledged,
                                         @NonNull final MeshTransport meshTransport,
                                         @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), lightLightnessSetUnacknowledged, meshTransport, callbacks);
    }

    /**
     * Constructs {@link LightLightnessSetUnacknowledgedState}
     *
     * @param context                         Context of the application
     * @param src                             Source address
     * @param dst                             Destination address to which the message must be sent to
     * @param lightLightnessSetUnacknowledged Wrapper class {@link LightLightnessSetUnacknowledged} containing the opcode and parameters for {@link GenericLevelSet} message
     * @param callbacks                       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    LightLightnessSetUnacknowledgedState(@NonNull final Context context,
                                         final int src,
                                         final int dst,
                                         @NonNull final LightLightnessSetUnacknowledged lightLightnessSetUnacknowledged,
                                         @NonNull final MeshTransport meshTransport,
                                         @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, lightLightnessSetUnacknowledged, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.LIGHT_LIGHTNESS_SET_UNACKNOWLEDGED_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final LightLightnessSetUnacknowledged lightLightnessSetUnacknowledged = (LightLightnessSetUnacknowledged) mMeshMessage;
        final byte[] key = lightLightnessSetUnacknowledged.getAppKey();
        final int akf = lightLightnessSetUnacknowledged.getAkf();
        final int aid = lightLightnessSetUnacknowledged.getAid();
        final int aszmic = lightLightnessSetUnacknowledged.getAszmic();
        final int opCode = lightLightnessSetUnacknowledged.getOpCode();
        final byte[] parameters = lightLightnessSetUnacknowledged.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, key, akf, aid, aszmic, opCode, parameters);
        lightLightnessSetUnacknowledged.setMessage(message);
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
