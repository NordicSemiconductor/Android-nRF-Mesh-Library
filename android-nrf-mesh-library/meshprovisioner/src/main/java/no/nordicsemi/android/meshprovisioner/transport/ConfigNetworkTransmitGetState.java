package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

@SuppressWarnings("WeakerAccess")
public final class ConfigNetworkTransmitGetState extends ConfigMessageState {

    private static final String TAG = ConfigNetworkTransmitGetState.class.getSimpleName();
    private final byte[] mDeviceKey;

    /**
     * Constructs the state for creating ConfigNetworkTransmitGet message
     *
     * @param context                  context
     * @param src                      source address
     * @param dst                      destination address
     * @param deviceKey                device key
     * @param configNetworkTransmitGet {@link ConfigNetworkTransmitGet}
     * @param meshTransport            {@link MeshTransport}
     * @param callbacks                {@link InternalMeshMsgHandlerCallbacks}
     * @deprecated in favour of {@link ConfigNetworkTransmitGetState}
     */
    @Deprecated
    ConfigNetworkTransmitGetState(@NonNull final Context context,
                                  @NonNull final byte[] src,
                                  @NonNull final byte[] dst,
                                  @NonNull final byte[] deviceKey,
                                  @NonNull final ConfigNetworkTransmitGet configNetworkTransmitGet,
                                  @NonNull final MeshTransport meshTransport,
                                  @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), deviceKey, configNetworkTransmitGet, meshTransport, callbacks);
    }

    /**
     * Constructs the state for creating ConfigNetworkTransmitGet message
     *
     * @param context                  context
     * @param src                      source address
     * @param dst                      destination address
     * @param deviceKey                device key
     * @param configNetworkTransmitGet {@link ConfigNetworkTransmitGet}
     * @param meshTransport            {@link MeshTransport}
     * @param callbacks                {@link InternalMeshMsgHandlerCallbacks}
     */
    ConfigNetworkTransmitGetState(@NonNull final Context context,
                                  final int src,
                                  final int dst,
                                  @NonNull final byte[] deviceKey,
                                  @NonNull final ConfigNetworkTransmitGet configNetworkTransmitGet,
                                  @NonNull final MeshTransport meshTransport,
                                  @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        super(context, configNetworkTransmitGet, meshTransport, callbacks);
        this.mSrc = src;
        this.mDst = dst;
        this.mDeviceKey = deviceKey;
        createAccessMessage();
    }

    @Override
    public MeshMessageState.MessageState getState() {
        return MeshMessageState.MessageState.CONFIG_NETWORK_TRANSMIT_GET_STATE;
    }

    private void createAccessMessage() {
        final ConfigNetworkTransmitGet configNetworkTransmitGet = (ConfigNetworkTransmitGet) mMeshMessage;
        final int akf = configNetworkTransmitGet.getAkf();
        final int aid = configNetworkTransmitGet.getAid();
        final int aszmic = configNetworkTransmitGet.getAszmic();
        final int opCode = configNetworkTransmitGet.getOpCode();
        final byte[] parameters = configNetworkTransmitGet.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, mDeviceKey, akf, aid, aszmic, opCode, parameters);
        configNetworkTransmitGet.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending config network transmit get");
        super.executeSend();

        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
        }
    }
}
