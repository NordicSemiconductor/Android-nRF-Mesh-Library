package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * This generic class handles the mesh messages received or sent.
 * <p>
 * This class handles sending, resending and parsing mesh messages. Each message sent by the library has its own state.
 * {@link ConfigMessageState} and {@link GenericMessageState} extends this class based on the type of the message.
 * Currently the library supports basic Configuration and Generic Messages.
 * </p>
 */
abstract class MeshMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = MeshMessageState.class.getSimpleName();

    protected final Context mContext;
    final MeshTransport mMeshTransport;
    private final InternalMeshMsgHandlerCallbacks meshMessageHandlerCallbacks;
    protected MeshMessage mMeshMessage;
    protected int mSrc;
    protected int mDst;
    protected InternalTransportCallbacks mInternalTransportCallbacks;
    protected MeshStatusCallbacks mMeshStatusCallbacks;
    protected Message message;
    private boolean isIncompleteTimerExpired;

    /**
     * Constructs the base mesh message state class
     *
     * @param context       Context
     * @param meshMessage   {@link MeshMessage} Mesh message
     * @param meshTransport {@link MeshTransport} Mesh transport
     * @param callbacks     {@link InternalMeshMsgHandlerCallbacks} Internal mesh message handler callbacks
     */
    MeshMessageState(@NonNull final Context context,
                     @NonNull final MeshMessage meshMessage,
                     @NonNull final MeshTransport meshTransport,
                     @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        this.mContext = context;
        this.mMeshMessage = meshMessage;
        this.message = meshMessage.getMessage();
        this.meshMessageHandlerCallbacks = callbacks;
        this.mMeshTransport = meshTransport;
        this.mMeshTransport.setLowerTransportLayerCallbacks(this);
    }

    /**
     * Set transport callbacks
     *
     * @param callbacks callbacks
     */
    void setTransportCallbacks(final InternalTransportCallbacks callbacks) {
        this.mInternalTransportCallbacks = callbacks;
    }

    /**
     * Set mesh status call backs
     *
     * @param callbacks callbacks
     */
    void setStatusCallbacks(final MeshStatusCallbacks callbacks) {
        this.mMeshStatusCallbacks = callbacks;
    }

    /**
     * Returns the current mesh state
     */
    public abstract MessageState getState();

    /**
     * Returns the mesh message relating to the state
     */
    public MeshMessage getMeshMessage() {
        return mMeshMessage;
    }

    /**
     * Returns the network pdu
     */
    public SparseArray<byte[]> getNetworkPdu() {
        return message.getNetworkPdu();
    }

    /**
     * Starts sending the mesh pdu
     */
    public void executeSend() {
        if (message.getNetworkPdu().size() > 0) {
            for (int i = 0; i < message.getNetworkPdu().size(); i++) {
                mInternalTransportCallbacks.sendMeshPdu(mDst, message.getNetworkPdu().get(i));
            }
        }
    }

    /**
     * Re-sends the mesh pdu segments that were lost in flight
     */
    final void executeResend(final List<Integer> retransmitPduIndexes) {
        if (message.getNetworkPdu().size() > 0 && !retransmitPduIndexes.isEmpty()) {
            for (int i = 0; i < retransmitPduIndexes.size(); i++) {
                final int segO = retransmitPduIndexes.get(i);
                if (message.getNetworkPdu().get(segO) != null) {
                    final byte[] pdu = message.getNetworkPdu().get(segO);
                    Log.v(TAG, "Resending segment " + segO + " : " + MeshParserUtils.bytesToHex(pdu, false));
                    final Message retransmitMeshMessage = mMeshTransport.createRetransmitMeshMessage(message, segO);
                    mInternalTransportCallbacks.sendMeshPdu(mDst, retransmitMeshMessage.getNetworkPdu().get(segO));
                }
            }
        }
    }

    boolean isSegmented() {
        return message.getNetworkPdu().size() > 1;
    }

    @Override
    public void onIncompleteTimerExpired() {
        Log.v(TAG, "Incomplete timer has expired, all segments were not received!");
        isIncompleteTimerExpired = true;
        if (meshMessageHandlerCallbacks != null) {

            meshMessageHandlerCallbacks.onIncompleteTimerExpired(true);

            if (mMeshStatusCallbacks != null) {
                mMeshStatusCallbacks.onTransactionFailed(mDst, true);
            }
        }
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        //We don't send acks here
        final ControlMessage message = mMeshTransport.createSegmentBlockAcknowledgementMessage(controlMessage);
        Log.v(TAG, "Sending acknowledgement: " + MeshParserUtils.bytesToHex(message.getNetworkPdu().get(0), false));
        mInternalTransportCallbacks.sendMeshPdu(message.getDst(), message.getNetworkPdu().get(0));
        mMeshStatusCallbacks.onBlockAcknowledgementSent(message.getDst());
    }

    public enum MessageState {

        //Proxy configuration message
        PROXY_CONFIG_SET_FILTER_TYPE_STATE(900),
        PROXY_CONFIG_ADD_ADDRESS_TO_FILTER_STATE(901),
        PROXY_CONFIG_REMOVE_ADDRESS_FROM_FILTER_STATE(902),

        //Configuration message States
        COMPOSITION_DATA_GET_STATE(0),
        APP_KEY_ADD_STATE(1),
        CONFIG_MODEL_APP_BIND_STATE(2),
        CONFIG_MODEL_APP_UNBIND_STATE(3),
        CONFIG_MODEL_PUBLICATION_GET_STATE(40),
        CONFIG_MODEL_PUBLICATION_SET_STATE(4),
        CONFIG_MODEL_SUBSCRIPTION_ADD_STATE(5),
        CONFIG_MODEL_SUBSCRIPTION_DELETE_STATE(6),
        CONFIG_NODE_RESET_STATE(7),
        CONFIG_NETWORK_TRANSMIT_SET_STATE(8),
        CONFIG_NETWORK_TRANSMIT_GET_STATE(9),
        CONFIG_RELAY_GET_STATE(10),
        CONFIG_RELAY_SET_STATE(11),
        CONFIG_PROXY_GET_STATE(10),
        CONFIG_PROXY_SET_STATE(11),

        //Application message States
        GENERIC_ON_OFF_GET_STATE(200),
        GENERIC_ON_OFF_SET_STATE(201),
        GENERIC_ON_OFF_SET_UNACKNOWLEDGED_STATE(202),

        GENERIC_LEVEL_GET_STATE(203),
        GENERIC_LEVEL_SET_STATE(204),
        GENERIC_LEVEL_SET_UNACKNOWLEDGED_STATE(205),

        LIGHT_LIGHTNESS_GET_STATE(300),
        LIGHT_LIGHTNESS_SET_STATE(301),
        LIGHT_LIGHTNESS_SET_UNACKNOWLEDGED_STATE(302),

        LIGHT_CTL_GET_STATE(303),
        LIGHT_CTL_SET_STATE(304),
        LIGHT_CTL_SET_UNACKNOWLEDGED_STATE(305),

        LIGHT_HSL_GET_STATE(306),
        LIGHT_HSL_SET_STATE(307),
        LIGHT_HSL_SET_UNACKNOWLEDGED_STATE(307),

        SCENE_GET_STATE(308),
        SCENE_REGISTER_GET_STATE(309),
        SCENE_STORE_STATE(310),
        SCENE_STORE_UNACKNOWLEDGED_STATE(311),
        SCENE_DELETE_STATE(312),
        SCENE_DELETE_UNACKNOWLEDGED_STATE(313),
        SCENE_RECALL_STATE(314),
        SCENE_RECALL_UNACKNOWLEDGED_STATE(315),

        VENDOR_MODEL_ACKNOWLEDGED_STATE(1000),
        VENDOR_MODEL_UNACKNOWLEDGED_STATE(1001);

        private int state;

        MessageState(final int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }
    }
}
