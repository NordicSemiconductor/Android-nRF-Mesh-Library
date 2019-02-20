package no.nordicsemi.android.meshprovisioner.transport;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling LightLightnessGetState messages.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
class LightLightnessGetState extends GenericMessageState {

    private static final String TAG = LightLightnessGetState.class.getSimpleName();

    /**
     * Constructs LightLightnessGetState
     *
     * @param context           Context of the application
     * @param src               Source address
     * @param dst               Destination address to which the message must be sent to
     * @param lightLightnessGet Wrapper class {@link LightLightnessGet} containing the opcode and parameters for {@link LightLightnessGet} message
     * @param callbacks         {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    @Deprecated
    LightLightnessGetState(@NonNull final Context context,
                           @NonNull final byte[] src,
                           @NonNull final byte[] dst,
                           @NonNull final LightLightnessGet lightLightnessGet,
                           @NonNull final MeshTransport meshTransport,
                           @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), lightLightnessGet, meshTransport, callbacks);
    }

    /**
     * Constructs LightLightnessGetState
     *
     * @param context           Context of the application
     * @param src               Source address
     * @param dst               Destination address to which the message must be sent to
     * @param lightLightnessGet Wrapper class {@link LightLightnessGet} containing the opcode and parameters for {@link LightLightnessGet} message
     * @param callbacks         {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    LightLightnessGetState(@NonNull final Context context,
                           final int src,
                           final int dst,
                           @NonNull final LightLightnessGet lightLightnessGet,
                           @NonNull final MeshTransport meshTransport,
                           @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, lightLightnessGet, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.LIGHT_LIGHTNESS_GET_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final LightLightnessGet lightLightnessGet = (LightLightnessGet) mMeshMessage;
        final byte[] key = lightLightnessGet.getAppKey();
        final int akf = lightLightnessGet.getAkf();
        final int aid = lightLightnessGet.getAid();
        final int aszmic = lightLightnessGet.getAszmic();
        final int opCode = lightLightnessGet.getOpCode();
        final byte[] parameters = lightLightnessGet.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, key, akf, aid, aszmic, opCode, parameters);
        lightLightnessGet.setMessage(message);
    }

    @Override
    public void executeSend() {
        Log.v(TAG, "Sending Light Lightness get");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
        }
    }
}
