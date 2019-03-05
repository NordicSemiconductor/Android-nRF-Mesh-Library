package no.nordicsemi.android.meshprovisioner.transport;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling LightCtlGetState messages.
 */
@SuppressWarnings("WeakerAccess")
class LightCtlGetState extends GenericMessageState {

    private static final String TAG = LightCtlGetState.class.getSimpleName();

    /**
     * Constructs LightCtlGetState
     *
     * @param context     Context of the application
     * @param src         Source address
     * @param dst         Destination address to which the message must be sent to
     * @param lightCtlGet Wrapper class {@link LightCtlGet} containing the opcode and parameters for {@link LightCtlGet} message
     * @param callbacks   {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     * @deprecated in favour of {@link #LightCtlGetState(Context, int, int, LightCtlGet, MeshTransport, InternalMeshMsgHandlerCallbacks)}
     */
    @Deprecated
    LightCtlGetState(@NonNull final Context context,
                     @NonNull final byte[] src,
                     @NonNull final byte[] dst,
                     @NonNull final LightCtlGet lightCtlGet,
                     @NonNull final MeshTransport meshTransport,
                     @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), lightCtlGet, meshTransport, callbacks);
    }

    /**
     * Constructs LightCtlGetState
     *
     * @param context     Context of the application
     * @param src         Source address
     * @param dst         Destination address to which the message must be sent to
     * @param lightCtlGet Wrapper class {@link LightCtlGet} containing the opcode and parameters for {@link LightCtlGet} message
     * @param callbacks   {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    LightCtlGetState(@NonNull final Context context,
                     final int src,
                     final int dst,
                     @NonNull final LightCtlGet lightCtlGet,
                     @NonNull final MeshTransport meshTransport,
                     @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, lightCtlGet, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.LIGHT_CTL_GET_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final LightCtlGet lightCtlGet = (LightCtlGet) mMeshMessage;
        final byte[] key = lightCtlGet.getAppKey();
        final int akf = lightCtlGet.getAkf();
        final int aid = lightCtlGet.getAid();
        final int aszmic = lightCtlGet.getAszmic();
        final int opCode = lightCtlGet.getOpCode();
        final byte[] parameters = lightCtlGet.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, key, akf, aid, aszmic, opCode, parameters);
        lightCtlGet.setMessage(message);
    }

    @Override
    public void executeSend() {
        Log.v(TAG, "Sending Light Ctl get");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
        }
    }
}
