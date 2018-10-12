package no.nordicsemi.android.meshprovisioner.meshmessagestates;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.InternalMeshMsgHandlerCallbacks;
import no.nordicsemi.android.meshprovisioner.messages.GenericLevelGet;
import no.nordicsemi.android.meshprovisioner.messages.GenericLevelStatus;
import no.nordicsemi.android.meshprovisioner.messagetypes.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messagetypes.ControlMessage;
import no.nordicsemi.android.meshprovisioner.messagetypes.Message;
import no.nordicsemi.android.meshprovisioner.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling GenericLevelGet messages.
 */
public class GenericLevelGetState extends GenericMessageState {

    private static final String TAG = GenericLevelGetState.class.getSimpleName();
    private final GenericLevelGet mGenericLevelGet;

    /**
     * Constructs GenericLevelGetState
     *
     * @param context         Context of the application
     * @param dstAddress      Destination address to which the message must be sent to
     * @param genericLevelGet Wrapper class {@link GenericLevelGet} containing the opcode and parameters for {@link GenericLevelGet} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    public GenericLevelGetState(@NonNull final Context context,
                                @NonNull final byte[] dstAddress,
                                @NonNull final GenericLevelGet genericLevelGet,
                                @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, dstAddress, genericLevelGet.getMeshNode(), callbacks);
        this.mGenericLevelGet = genericLevelGet;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.GENERIC_LEVEL_GET_STATE;
    }

    @Override
    public boolean parseMeshPdu(final byte[] pdu) {
        final Message message = mMeshTransport.parsePdu(mSrc, pdu);
        if (message != null) {
            if (message instanceof AccessMessage) {
                if(message.getOpCode() == ApplicationMessageOpCodes.GENERIC_LEVEL_STATUS) {
                    final GenericLevelStatus genericLevelStatus = new GenericLevelStatus(mNode, (AccessMessage) message);
                    mInternalTransportCallbacks.updateMeshNode(mNode);
                    mMeshStatusCallbacks.onGenericLevelStatusReceived(genericLevelStatus);
                    return true;
                } else {
                    Log.v(TAG, "Unknown pdu received! " + MeshParserUtils.bytesToHex(((AccessMessage) message).getAccessPdu(), false));
                }
            } else {
                parseControlMessage((ControlMessage) message, mPayloads.size());
            }
        } else {
            Log.v(TAG, "Message reassembly may not be complete yet");
        }
        return false;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final byte[] key = mGenericLevelGet.getAppKey();
        final int akf = mGenericLevelGet.getAkf();
        final int aid = mGenericLevelGet.getAid();
        final int aszmic = mGenericLevelGet.getAszmic();
        final int opCode = mGenericLevelGet.getOpCode();
        final byte[] parameters = mGenericLevelGet.getParameters();
        message = mMeshTransport.createMeshMessage(mNode, mSrc, mDstAddress, key, akf, aid, aszmic,
                opCode, parameters);
        mPayloads.putAll(message.getNetworkPdu());
    }

    @Override
    public void executeSend() {
        Log.v(TAG, "Sending Generic Level get");
        super.executeSend();

        if (!mPayloads.isEmpty()) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onGenericLevelGetSent(mNode);
        }
    }
}
