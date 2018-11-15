package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * State class for handling LightCtlSetState messages.
 */
class LightHslSetState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = LightHslSetState.class.getSimpleName();

    /**
     * Constructs {@link LightHslSetState}
     *
     * @param context         Context of the application
     * @param dstAddress      Destination address to which the message must be sent to
     * @param lightHslSet Wrapper class {@link LightLightnessSet} containing the opcode and parameters for {@link GenericLevelSet} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    LightHslSetState(@NonNull final Context context,
                     @NonNull final byte[] dstAddress,
                     @NonNull final LightHslSet lightHslSet,
                     @NonNull final MeshTransport meshTransport,
                     @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, dstAddress, lightHslSet, meshTransport, callbacks);
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
        message = mMeshTransport.createMeshMessage(mNode, mSrc, mDstAddress, key, akf, aid, aszmic, opCode, parameters);
        lightHslSet.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending light hsl set acknowledged ");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mMeshMessage);
        }
    }
}
