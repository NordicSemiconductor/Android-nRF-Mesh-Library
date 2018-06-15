package no.nordicsemi.android.meshprovisioner.configuration;


import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshConfigurationStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.messages.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messages.ControlMessage;
import no.nordicsemi.android.meshprovisioner.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.transport.LowerTransportLayerCallbacks;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

public class GenericOnOffSet extends ConfigMessage implements LowerTransportLayerCallbacks{


    private static final String TAG = GenericOnOffSet.class.getSimpleName();
    private static final int GENERIC_ON_OFF_SET_PARAMS_LENGTH = 4;
    public static final int GENERIC_ON_OFF_TRANSITION_STEP_0 = 0;
    public static final int GENERIC_ON_OFF_TRANSITION_STEP_1 = 1;
    public static final int GENERIC_ON_OFF_TRANSITION_STEP_2 = 2;
    public static final int GENERIC_ON_OFF_TRANSITION_STEP_3 = 3;

    private final int mAszmic;
    private final byte[] mElementAddress;
    private final MeshModel mMeshModel;
    private Integer mTransitionSteps = null;
    private Integer mTransitionResolution = null;
    private final boolean mState;
    private final int mAppKeyIndex;

    public GenericOnOffSet(final Context context, final ProvisionedMeshNode provisionedMeshNode, final MeshModel model, final boolean aszmic,
                           final byte[] elementAddress, final int appKeyIndex, final int transitionSteps, final int transitionResolution, final boolean state) {
        super(context, provisionedMeshNode);
        this.mAszmic = aszmic ? 1 : 0;
        this.mElementAddress = elementAddress;
        this.mMeshModel = model;
        this.mAppKeyIndex = appKeyIndex;
        this.mTransitionSteps = transitionSteps;
        this.mTransitionResolution = transitionResolution;
        this.mState = state;
        createAccessMessage();
    }

    public GenericOnOffSet(final Context context, final ProvisionedMeshNode provisionedMeshNode, final MeshModel model, final boolean aszmic,
                            final byte[] elementAddress, final int appKeyIndex, final boolean state) {
        super(context, provisionedMeshNode);
        this.mAszmic = aszmic ? 1 : 0;
        this.mElementAddress = elementAddress;
        this.mMeshModel = model;
        this.mAppKeyIndex = appKeyIndex;
        this.mState = state;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.GENERIC_ON_OFF_SET;
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
        paramsBuffer = ByteBuffer.allocate(GENERIC_ON_OFF_SET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        paramsBuffer.put((byte) (mState ? 0x01 : 0x00));
        paramsBuffer.put((byte) mProvisionedMeshNode.getSequenceNumber());
        paramsBuffer.put((byte) (mTransitionSteps << 6 | mTransitionResolution));
        paramsBuffer.put((byte) 5);
        parameters = paramsBuffer.array();

        final byte[] key = MeshParserUtils.toByteArray(mMeshModel.getBoundAppkeys().get(mAppKeyIndex));
        int akf = 1;
        int aid = SecureUtils.calculateK4(key);
        final AccessMessage accessMessage = mMeshTransport.createMeshMessage(mProvisionedMeshNode, mSrc, key, akf, aid, mAszmic, ApplicationMessageOpCodes.GENERIC_ON_OFF_SET, parameters);
        mPayloads.putAll(accessMessage.getNetworkPdu());
    }

    /**
     * Starts sending the mesh pdu
     */
    public void executeSend() {
        if (!mPayloads.isEmpty()) {
            for (int i = 0; i < mPayloads.size(); i++) {
                mInternalTransportCallbacks.sendPdu(mProvisionedMeshNode, mPayloads.get(i));
            }

            if (mConfigStatusCallbacks != null)
                mConfigStatusCallbacks.onAppKeyAddSent(mProvisionedMeshNode);
        }
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {

    }
}
