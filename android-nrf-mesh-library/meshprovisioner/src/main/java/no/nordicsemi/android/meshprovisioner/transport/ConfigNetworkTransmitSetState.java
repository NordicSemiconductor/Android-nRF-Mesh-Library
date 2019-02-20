package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;


@SuppressWarnings("WeakerAccess")
public final class ConfigNetworkTransmitSetState extends ConfigMessageState {

    private static final String TAG = ConfigNetworkTransmitSetState.class.getSimpleName();
    private final byte[] mDeviceKey;

    /**
     * Constructs the state for creating ConfigNetworkTransmitSet message
     *
     * @param context                  context
     * @param src                      source address
     * @param dst                      destination address
     * @param deviceKey                device key
     * @param configNetworkTransmitSet {@link ConfigNetworkTransmitSet}
     * @param meshTransport            {@link MeshTransport}
     * @param callbacks                {@link InternalMeshMsgHandlerCallbacks}
     */
    @Deprecated
    ConfigNetworkTransmitSetState(@NonNull final Context context,
                                  @NonNull final byte[] src,
                                  @NonNull final byte[] dst,
                                  @NonNull final byte[] deviceKey,
                                  @NonNull final ConfigNetworkTransmitSet configNetworkTransmitSet,
                                  @NonNull final MeshTransport meshTransport,
                                  @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), deviceKey, configNetworkTransmitSet, meshTransport, callbacks);
    }

    /**
     * Constructs the state for creating ConfigNetworkTransmitSet message
     *
     * @param context                  context
     * @param src                      source address
     * @param dst                      destination address
     * @param deviceKey                device key
     * @param configNetworkTransmitSet {@link ConfigNetworkTransmitSet}
     * @param meshTransport            {@link MeshTransport}
     * @param callbacks                {@link InternalMeshMsgHandlerCallbacks}
     */
    ConfigNetworkTransmitSetState(@NonNull final Context context,
                                  final int src,
                                  final int dst,
                                  @NonNull final byte[] deviceKey,
                                  @NonNull final ConfigNetworkTransmitSet configNetworkTransmitSet,
                                  @NonNull final MeshTransport meshTransport,
                                  @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        super(context, configNetworkTransmitSet, meshTransport, callbacks);
        this.mSrc = src;
        this.mDst = dst;
        this.mDeviceKey = deviceKey;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.CONFIG_NETWORK_TRANSMIT_SET_STATE;
    }

    private void createAccessMessage() {
        final ConfigNetworkTransmitSet configNetworkTransmitSet = (ConfigNetworkTransmitSet) mMeshMessage;
        final int akf = configNetworkTransmitSet.getAkf();
        final int aid = configNetworkTransmitSet.getAid();
        final int aszmic = configNetworkTransmitSet.getAszmic();
        final int opCode = configNetworkTransmitSet.getOpCode();
        final byte[] parameters = configNetworkTransmitSet.getParameters();
        Log.v(TAG, "State, parameters: " + MeshParserUtils.bytesToHex(parameters, false));
        message = mMeshTransport.createMeshMessage(mSrc, mDst, mDeviceKey, akf, aid, aszmic, opCode, parameters);
        configNetworkTransmitSet.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending config network transmit set");
        super.executeSend();

        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
        }
    }
}
