package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * State class for handling LightCtlSetState messages.
 */
class LightHslSetUnacknowledgedState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = LightHslSetUnacknowledgedState.class.getSimpleName();

    /**
     * Constructs {@link LightHslSetUnacknowledgedState}
     *
     * @param context         Context of the application
     * @param dstAddress      Destination address to which the message must be sent to
     * @param lightHslSetUnacknowledged Wrapper class {@link LightLightnessSet} containing the opcode and parameters for {@link GenericLevelSet} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    LightHslSetUnacknowledgedState(@NonNull final Context context,
                                   @NonNull final byte[] dstAddress,
                                   @NonNull final LightHslSetUnacknowledged lightHslSetUnacknowledged,
                                   @NonNull final MeshTransport meshTransport,
                                   @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, dstAddress, lightHslSetUnacknowledged, meshTransport, callbacks);
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
        message = mMeshTransport.createMeshMessage(mNode, mSrc, mDstAddress, key, akf, aid, aszmic, opCode, parameters);
        lightHslSetUnacknowledged.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending light hsl set acknowledged ");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null) {
                mMeshStatusCallbacks.onMeshMessageSent(mMeshMessage);
                //We must update update the mesh network state here for unacknowledged messages
                //If not the sequence numbers would be invalid for unacknowledged messages and will be dropped by the node.
                //Mesh network state for acknowledged messages are updated in the DefaultNoOperationState once the status is received.
                mInternalTransportCallbacks.updateMeshNetwork(mMeshMessage);
            }
        }
    }
}
