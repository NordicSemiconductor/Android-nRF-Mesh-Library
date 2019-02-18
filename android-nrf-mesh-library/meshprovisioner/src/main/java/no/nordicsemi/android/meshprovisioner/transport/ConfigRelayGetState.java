package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State that handles the ConfigRelayGet message
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class ConfigRelayGetState extends ConfigMessageState {

    private static final String TAG = ConfigRelayGetState.class.getSimpleName();
    private final byte[] mDeviceKey;

    /**
     * Constructs the state for {@link ConfigRelayGet} message
     *
     * @param context        context
     * @param src            source address
     * @param dst            destination address
     * @param deviceKey      device key
     * @param configRelayGet {@link ConfigRelayGet} message
     * @param meshTransport  {@link MeshTransport}
     * @param callbacks      {@link InternalMeshMsgHandlerCallbacks} Internal mesh handler callbacks
     */
    @Deprecated
    ConfigRelayGetState(@NonNull final Context context,
                        @NonNull final byte[] src,
                        @NonNull final byte[] dst,
                        @NonNull final byte[] deviceKey,
                        @NonNull final ConfigRelayGet configRelayGet,
                        @NonNull final MeshTransport meshTransport,
                        @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), deviceKey, configRelayGet, meshTransport, callbacks);
    }

    /**
     * Constructs the state for {@link ConfigRelayGet} message
     *
     * @param context        context
     * @param src            source address
     * @param dst            destination address
     * @param deviceKey      device key
     * @param configRelayGet {@link ConfigRelayGet} message
     * @param meshTransport  {@link MeshTransport}
     * @param callbacks      {@link InternalMeshMsgHandlerCallbacks} Internal mesh handler callbacks
     */
    ConfigRelayGetState(@NonNull final Context context,
                        final int src,
                        final int dst,
                        @NonNull final byte[] deviceKey,
                        @NonNull final ConfigRelayGet configRelayGet,
                        @NonNull final MeshTransport meshTransport,
                        @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        super(context, configRelayGet, meshTransport, callbacks);
        this.mSrc = src;
        this.mDst = dst;
        this.mDeviceKey = deviceKey;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.CONFIG_RELAY_GET_STATE;
    }

    private void createAccessMessage() {
        final ConfigRelayGet configRelayGet = (ConfigRelayGet) mMeshMessage;
        final int akf = configRelayGet.getAkf();
        final int aid = configRelayGet.getAid();
        final int aszmic = configRelayGet.getAszmic();
        final int opCode = configRelayGet.getOpCode();
        final byte[] parameters = configRelayGet.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, mDeviceKey, akf, aid, aszmic, opCode, parameters);
        configRelayGet.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending config relay get");
        super.executeSend();

        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
        }
    }
}
