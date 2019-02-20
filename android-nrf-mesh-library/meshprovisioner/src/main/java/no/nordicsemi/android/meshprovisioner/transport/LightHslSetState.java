package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling LightCtlSetState messages.
 */
@SuppressWarnings("unused")
class LightHslSetState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = LightHslSetState.class.getSimpleName();

    /**
     * Constructs {@link LightHslSetState}
     *
     * @param context     Context of the application
     * @param src         Source address
     * @param dst         Destination address to which the message must be sent to
     * @param lightHslSet Wrapper class {@link LightLightnessSet} containing the opcode and parameters for {@link GenericLevelSet} message
     * @param callbacks   {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    @Deprecated
    LightHslSetState(@NonNull final Context context,
                     @NonNull final byte[] src,
                     @NonNull final byte[] dst,
                     @NonNull final LightHslSet lightHslSet,
                     @NonNull final MeshTransport meshTransport,
                     @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), lightHslSet, meshTransport, callbacks);
    }

    /**
     * Constructs {@link LightHslSetState}
     *
     * @param context     Context of the application
     * @param src         Source address
     * @param dst         Destination address to which the message must be sent to
     * @param lightHslSet Wrapper class {@link LightLightnessSet} containing the opcode and parameters for {@link GenericLevelSet} message
     * @param callbacks   {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    LightHslSetState(@NonNull final Context context,
                     final int src,
                     final int dst,
                     @NonNull final LightHslSet lightHslSet,
                     @NonNull final MeshTransport meshTransport,
                     @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, lightHslSet, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.LIGHT_HSL_SET_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final LightHslSet lightHslSet = (LightHslSet) mMeshMessage;
        final byte[] key = lightHslSet.getAppKey();
        final int akf = lightHslSet.getAkf();
        final int aid = lightHslSet.getAid();
        final int aszmic = lightHslSet.getAszmic();
        final int opCode = lightHslSet.getOpCode();
        final byte[] parameters = lightHslSet.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, key, akf, aid, aszmic, opCode, parameters);
        lightHslSet.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending light hsl set acknowledged ");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
        }
    }
}
