package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling GenericLevelSet messages.
 */
@SuppressWarnings("WeakerAccess")
class LightLightnessSetState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = LightLightnessSetState.class.getSimpleName();

    /**
     * Constructs {@link LightLightnessSetState}
     *
     * @param context           Context of the application
     * @param src               Source address
     * @param dst               Destination address to which the message must be sent to
     * @param lightLightnessSet Wrapper class {@link LightLightnessSet} containing the opcode and parameters for {@link GenericLevelSet} message
     * @param callbacks         {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     * @deprecated in favour of {@link #LightLightnessSetState(Context, int, int, LightLightnessSet, MeshTransport, InternalMeshMsgHandlerCallbacks)}
     */
    @Deprecated
    LightLightnessSetState(@NonNull final Context context,
                           @NonNull final byte[] src,
                           @NonNull final byte[] dst,
                           @NonNull final LightLightnessSet lightLightnessSet,
                           @NonNull final MeshTransport meshTransport,
                           @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), lightLightnessSet, meshTransport, callbacks);
    }

    /**
     * Constructs {@link LightLightnessSetState}
     *
     * @param context           Context of the application
     * @param src               Source address
     * @param dst               Destination address to which the message must be sent to
     * @param lightLightnessSet Wrapper class {@link LightLightnessSet} containing the opcode and parameters for {@link GenericLevelSet} message
     * @param callbacks         {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    LightLightnessSetState(@NonNull final Context context,
                           final int src,
                           final int dst,
                           @NonNull final LightLightnessSet lightLightnessSet,
                           @NonNull final MeshTransport meshTransport,
                           @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, lightLightnessSet, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.LIGHT_LIGHTNESS_SET_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final LightLightnessSet lightLightnessSet = (LightLightnessSet) mMeshMessage;
        final byte[] key = lightLightnessSet.getAppKey();
        final int akf = lightLightnessSet.getAkf();
        final int aid = lightLightnessSet.getAid();
        final int aszmic = lightLightnessSet.getAszmic();
        final int opCode = lightLightnessSet.getOpCode();
        final byte[] parameters = lightLightnessSet.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, key, akf, aid, aszmic, opCode, parameters);
        lightLightnessSet.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending light lightness set acknowledged");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
        }
    }
}
