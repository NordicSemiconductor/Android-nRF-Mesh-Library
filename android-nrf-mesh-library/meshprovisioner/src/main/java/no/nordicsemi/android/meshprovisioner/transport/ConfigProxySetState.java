package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling {@link ConfigProxySet} messages.
 */
@SuppressWarnings("WeakerAccess")
class ConfigProxySetState extends ConfigMessageState {

    private static final String TAG = ConfigProxySetState.class.getSimpleName();
    private final byte[] mDeviceKey;

    /**
     * Constructs {@link ConfigProxySetState}
     *
     * @param context        Context of the application
     * @param src            source address
     * @param dst            destination address
     * @param deviceKey      device key
     * @param configProxySet Wrapper class {@link ConfigProxySet} containing the opcode and parameters for {@link ConfigProxySet} message
     * @param callbacks      {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     * @deprecated in favour of {@link @ConfigProxySetState}
     */
    @Deprecated
    ConfigProxySetState(@NonNull final Context context,
                        @NonNull final byte[] src,
                        @NonNull final byte[] dst,
                        @NonNull final byte[] deviceKey,
                        @NonNull final ConfigProxySet configProxySet,
                        @NonNull final MeshTransport meshTransport,
                        @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), deviceKey, configProxySet, meshTransport, callbacks);
    }

    /**
     * Constructs {@link ConfigProxySetState}
     *
     * @param context        Context of the application
     * @param src            source address
     * @param dst            destination address
     * @param deviceKey      device key
     * @param configProxySet Wrapper class {@link ConfigProxySet} containing the opcode and parameters for {@link ConfigProxySet} message
     * @param callbacks      {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    ConfigProxySetState(@NonNull final Context context,
                        final int src,
                        final int dst,
                        @NonNull final byte[] deviceKey,
                        @NonNull final ConfigProxySet configProxySet,
                        @NonNull final MeshTransport meshTransport,
                        @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        super(context, configProxySet, meshTransport, callbacks);
        this.mSrc = src;
        this.mDst = dst;
        this.mDeviceKey = deviceKey;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.CONFIG_PROXY_SET_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final ConfigProxySet configProxySet = (ConfigProxySet) mMeshMessage;
        final int akf = configProxySet.getAkf();
        final int aid = configProxySet.getAid();
        final int aszmic = configProxySet.getAszmic();
        final int opCode = configProxySet.getOpCode();
        final byte[] parameters = configProxySet.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, mDeviceKey, akf, aid, aszmic, opCode, parameters);
        configProxySet.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending config proxy set");
        super.executeSend();

        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
        }
    }
}
