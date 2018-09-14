package no.nordicsemi.android.meshprovisioner.configuration;


import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.meshprovisioner.InternalMeshMsgHandlerCallbacks;
import no.nordicsemi.android.meshprovisioner.messages.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messages.ControlMessage;
import no.nordicsemi.android.meshprovisioner.messages.Message;
import no.nordicsemi.android.meshprovisioner.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

public class GenericOnOffSetUnacknowledged extends GenericMessageState {


    private static final String TAG = GenericOnOffSetUnacknowledged.class.getSimpleName();
    private static final int GENERIC_ON_OFF_SET_TRANSITION_PARAMS_LENGTH = 4;
    private static final int GENERIC_ON_OFF_SET_PARAMS_LENGTH = 2;
    public static final int GENERIC_ON_OFF_TRANSITION_STEP_0 = 0;
    public static final int GENERIC_ON_OFF_TRANSITION_STEP_1 = 1;
    public static final int GENERIC_ON_OFF_TRANSITION_STEP_2 = 2;
    public static final int GENERIC_ON_OFF_TRANSITION_STEP_3 = 3;

    private final int mAszmic;
    private final byte[] dstAddress;
    private final MeshModel mMeshModel;
    private final Integer mTransitionSteps;
    private final Integer mTransitionResolution;
    private final Integer mDelay;
    private final boolean mState;

    public GenericOnOffSetUnacknowledged(final Context context, final ProvisionedMeshNode provisionedMeshNode,
                                         final InternalMeshMsgHandlerCallbacks callbacks,
                                         final MeshModel model, final boolean aszmic,
                                         final byte[] dstAddress, final int appKeyIndex,
                                         final Integer transitionSteps, final Integer transitionResolution, final Integer delay, final boolean state) {
        super(context, provisionedMeshNode, callbacks);
        this.mAszmic = aszmic ? 1 : 0;
        this.dstAddress = dstAddress;
        this.mMeshModel = model;
        this.mAppKeyIndex = appKeyIndex;
        this.mTransitionSteps = transitionSteps;
        this.mTransitionResolution = transitionResolution;
        this.mDelay = delay;
        this.mState = state;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.GENERIC_ON_OFF_SET_UNACKNOWLEDGED_STATE;
    }

    @Override
    protected boolean parseMeshPdu(final byte[] pdu) {
        final Message message = mMeshTransport.parsePdu(mSrc, pdu);
        if (message != null) {
            if (message instanceof AccessMessage) {
                final byte[] accessPayload = ((AccessMessage) message).getAccessPdu();
                Log.v(TAG, "Unexpected access message received: " + MeshParserUtils.bytesToHex(accessPayload, false));
            } else {
                parseControlMessage((ControlMessage) message, mPayloads.size());
                return true;
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
        ByteBuffer paramsBuffer;
        byte[] parameters;
        if(mTransitionSteps == null || mTransitionResolution == null || mDelay == null) {
            paramsBuffer = ByteBuffer.allocate(GENERIC_ON_OFF_SET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.put((byte) (mState ? 0x01 : 0x00));
            paramsBuffer.put((byte) mProvisionedMeshNode.getSequenceNumber());
        } else {
            paramsBuffer = ByteBuffer.allocate(GENERIC_ON_OFF_SET_TRANSITION_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.put((byte) (mState ? 0x01 : 0x00));
            paramsBuffer.put((byte) mProvisionedMeshNode.getSequenceNumber());
            paramsBuffer.put((byte) (mTransitionResolution << 6 | mTransitionSteps));
            final int delay = mDelay;
            paramsBuffer.put((byte) delay);
        }
        parameters = paramsBuffer.array();

        final byte[] key = MeshParserUtils.toByteArray(mMeshModel.getBoundAppkeys().get(mAppKeyIndex));
        int akf = 1;
        int aid = SecureUtils.calculateK4(key);
        message = mMeshTransport.createMeshMessage(mProvisionedMeshNode, mSrc, dstAddress, key, akf, aid, mAszmic, ApplicationMessageOpCodes.GENERIC_ON_OFF_SET_UNACKNOWLEDGED, parameters);
        mPayloads.putAll(message.getNetworkPdu());
    }

    @Override
    public void executeSend() {
        Log.v(TAG, "Sending Generic OnOff set unacknowledged: " + (mState ? "ON" : "OFF"));
        super.executeSend();

        if (!mPayloads.isEmpty()) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onGenericOnOffSetUnacknowledgedSent(mProvisionedMeshNode);
        }
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        final ControlMessage message = mMeshTransport.createSegmentBlockAcknowledgementMessage(controlMessage);
        Log.v(TAG, "Sending acknowledgement: " + MeshParserUtils.bytesToHex(message.getNetworkPdu().get(0), false));
        mInternalTransportCallbacks.sendPdu(mProvisionedMeshNode, message.getNetworkPdu().get(0));
        mMeshStatusCallbacks.onBlockAcknowledgementSent(mProvisionedMeshNode);
    }
}
