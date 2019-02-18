package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling {@link ConfigProxyGet} messages.
 */
@SuppressWarnings("WeakerAccess")
class ConfigProxyGetState extends ConfigMessageState {

    private static final String TAG = ConfigProxyGetState.class.getSimpleName();
    private final byte[] mDeviceKey;

    /**
     * Constructs {@link ConfigProxyGetState}
     *
     * @param context        Context of the application
     * @param src            source address
     * @param dst            destination address
     * @param deviceKey      device key
     * @param configProxyGet Wrapper class {@link ConfigProxyGet} containing the opcode and parameters for {@link ConfigProxyGet} message
     * @param callbacks      {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    @Deprecated
    ConfigProxyGetState(@NonNull final Context context,
                        @NonNull final byte[] src,
                        @NonNull final byte[] dst,
                        @NonNull final byte[] deviceKey,
                        @NonNull final ConfigProxyGet configProxyGet,
                        @NonNull final MeshTransport meshTransport,
                        @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), deviceKey, configProxyGet, meshTransport, callbacks);
    }

    /**
     * Constructs {@link ConfigProxyGetState}
     *
     * @param context        Context of the application
     * @param src            source address
     * @param dst            destination address
     * @param deviceKey      device key
     * @param configProxyGet Wrapper class {@link ConfigProxyGet} containing the opcode and parameters for {@link ConfigProxyGet} message
     * @param callbacks      {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    ConfigProxyGetState(@NonNull final Context context,
                        final int src,
                        final int dst,
                        @NonNull final byte[] deviceKey,
                        @NonNull final ConfigProxyGet configProxyGet,
                        @NonNull final MeshTransport meshTransport,
                        @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        super(context, configProxyGet, meshTransport, callbacks);
        this.mSrc = src;
        this.mDst = dst;
        this.mDeviceKey = deviceKey;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.CONFIG_PROXY_GET_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final ConfigProxyGet configProxyGet = (ConfigProxyGet) mMeshMessage;
        final int akf = configProxyGet.getAkf();
        final int aid = configProxyGet.getAid();
        final int aszmic = configProxyGet.getAszmic();
        final int opCode = configProxyGet.getOpCode();
        final byte[] parameters = configProxyGet.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, mDeviceKey, akf, aid, aszmic, opCode, parameters);
        configProxyGet.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending config proxy get");
        super.executeSend();

        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
        }
    }
}
