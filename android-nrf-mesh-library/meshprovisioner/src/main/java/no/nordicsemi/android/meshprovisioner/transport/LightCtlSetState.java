package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

/**
 * State class for handling LightCtlSet messages.
 */
class LightCtlSetState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = LightCtlSetState.class.getSimpleName();

    /**
     * Constructs {@link LightCtlSetState}
     *
     * @param context     Context of the application
     * @param dst         Destination address to which the message must be sent to
     * @param lightCtlSet Wrapper class {@link LightCtlSetState} containing the opcode and parameters for {@link LightCtlSetState} message
     * @param callbacks   {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    LightCtlSetState(@NonNull final Context context,
                     @NonNull final byte[] src,
                     @NonNull final byte[] dst,
                     @NonNull final LightCtlSet lightCtlSet,
                     @NonNull final MeshTransport meshTransport,
                     @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, lightCtlSet, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.LIGHT_CTL_SET_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final LightCtlSet lightCtlSet = (LightCtlSet) mMeshMessage;
        final byte[] key = lightCtlSet.getAppKey();
        final int akf = lightCtlSet.getAkf();
        final int aid = lightCtlSet.getAid();
        final int aszmic = lightCtlSet.getAszmic();
        final int opCode = lightCtlSet.getOpCode();
        final byte[] parameters = lightCtlSet.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, key, akf, aid, aszmic, opCode, parameters);
        lightCtlSet.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending light ctl set acknowledged ");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
        }
    }
}
