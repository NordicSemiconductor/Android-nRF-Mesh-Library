package no.nordicsemi.android.meshprovisioner.meshmessagestates;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.InternalMeshMsgHandlerCallbacks;
import no.nordicsemi.android.meshprovisioner.messages.GenericOnOffSet;
import no.nordicsemi.android.meshprovisioner.messages.GenericOnOffStatus;
import no.nordicsemi.android.meshprovisioner.messagetypes.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messagetypes.ControlMessage;
import no.nordicsemi.android.meshprovisioner.messagetypes.Message;
import no.nordicsemi.android.meshprovisioner.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.transport.LowerTransportLayerCallbacks;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling GenericOnOffSetState messages.
 */
public class GenericOnOffSetState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = GenericOnOffSetState.class.getSimpleName();
    private final GenericOnOffSet mGenericOnOffSet;

    /**
     * Constructs {@link GenericOnOffSetState}
     *
     * @param context         Context of the application
     * @param dstAddress      Destination address to which the message must be sent to
     * @param genericOnOffSet Wrapper class {@link GenericOnOffSet} containing the opcode and parameters for {@link GenericOnOffSet} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    public GenericOnOffSetState(@NonNull final Context context,
                                @NonNull final byte[] dstAddress,
                                @NonNull final GenericOnOffSet genericOnOffSet,
                                @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, dstAddress, genericOnOffSet.getMeshNode(), callbacks);
        this.mGenericOnOffSet = genericOnOffSet;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.GENERIC_ON_OFF_SET_STATE;
    }

    @Override
    public boolean parseMeshPdu(final byte[] pdu) {
        final Message message = mMeshTransport.parsePdu(mSrc, pdu);
        if (message != null) {
            if (message instanceof AccessMessage) {
                if(message.getOpCode() == ApplicationMessageOpCodes.GENERIC_ON_OFF_STATUS) {
                    final GenericOnOffStatus genericOnOffStatus = new GenericOnOffStatus(mNode, (AccessMessage) message);
                    mInternalTransportCallbacks.updateMeshNode(mNode);
                    mMeshStatusCallbacks.onGenericOnOffStatusReceived(genericOnOffStatus);
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
        final byte[] key = mGenericOnOffSet.getAppKey();
        final int akf = mGenericOnOffSet.getAkf();
        final int aid = mGenericOnOffSet.getAid();
        final int aszmic = mGenericOnOffSet.getAszmic();
        final int opCode = mGenericOnOffSet.getOpCode();
        final byte[] parameters = mGenericOnOffSet.getParameters();
        message = mMeshTransport.createMeshMessage(mNode, mSrc, mDstAddress, key, akf, aid, aszmic, opCode, parameters);
        mPayloads.putAll(message.getNetworkPdu());
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending Generic OnOff set acknowledged");
        super.executeSend();
    }
}
