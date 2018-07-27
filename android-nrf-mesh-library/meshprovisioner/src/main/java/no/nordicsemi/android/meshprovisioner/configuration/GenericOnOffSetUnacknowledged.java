package no.nordicsemi.android.meshprovisioner.configuration;


import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshConfigurationStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.messages.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messages.ControlMessage;
import no.nordicsemi.android.meshprovisioner.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

public class GenericOnOffSetUnacknowledged extends ConfigMessage {


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

    public GenericOnOffSetUnacknowledged(final Context context, final ProvisionedMeshNode provisionedMeshNode, final MeshModel model, final boolean aszmic,
                                         final byte[] dstAddress, final int appKeyIndex, final Integer transitionSteps, final Integer transitionResolution, final Integer delay, final boolean state) {
        super(context, provisionedMeshNode);
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
        return MessageState.GENERIC_ON_OFF_SET_UNACKNOWLEDGED;
    }

    @Override
    protected void parseMessage(final byte[] pdu) {
        //Do nothing here
    }

    public void setTransportCallbacks(final InternalTransportCallbacks callbacks) {
        this.mInternalTransportCallbacks = callbacks;
    }

    public void setConfigurationStatusCallbacks(final MeshConfigurationStatusCallbacks callbacks) {
        this.mConfigStatusCallbacks = callbacks;
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
            paramsBuffer.put((byte) (mTransitionSteps << 6 | mTransitionResolution));
            final int delay = mDelay;
            paramsBuffer.put((byte) delay);
        }
        parameters = paramsBuffer.array();

        final byte[] key = MeshParserUtils.toByteArray(mMeshModel.getBoundAppkeys().get(mAppKeyIndex));
        int akf = 1;
        int aid = SecureUtils.calculateK4(key);
        accessMessage = mMeshTransport.createMeshMessage(mProvisionedMeshNode, mSrc, dstAddress, key, akf, aid, mAszmic, ApplicationMessageOpCodes.GENERIC_ON_OFF_SET_UNACKNOWLEDGED, parameters);
        mPayloads.putAll(accessMessage.getNetworkPdu());
    }

    /**
     * Starts sending the mesh pdu
     */
    public void executeSend() {
        Log.v(TAG, "Sending Generic OnOff set unacknowledged: " + (mState ? "ON" : "OFF"));
        if (!mPayloads.isEmpty()) {
            for (int i = 0; i < mPayloads.size(); i++) {
                if (mInternalTransportCallbacks != null) {
                    mInternalTransportCallbacks.sendPdu(mProvisionedMeshNode, mPayloads.get(i));
                }
            }

            if (mConfigStatusCallbacks != null)
                mConfigStatusCallbacks.onPublicationSetSent(mProvisionedMeshNode);
        }
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        final ControlMessage message = mMeshTransport.createSegmentBlockAcknowledgementMessage(controlMessage);
        Log.v(TAG, "Sending acknowledgement: " + MeshParserUtils.bytesToHex(message.getNetworkPdu().get(0), false));
        mInternalTransportCallbacks.sendPdu(mProvisionedMeshNode, message.getNetworkPdu().get(0));
        mConfigStatusCallbacks.onBlockAcknowledgementSent(mProvisionedMeshNode);
    }
}
