package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling ConfigNodeReset messages.
 */
@SuppressWarnings("WeakerAccess")
class ConfigNodeResetState extends ConfigMessageState {

    private static final String TAG = ConfigNodeResetState.class.getSimpleName();
    private final byte[] mDeviceKey;

    /**
     * Constructs {@link ConfigNodeResetState}
     *
     * @param context         Context of the application
     * @param src             source address
     * @param dst             destination address
     * @param deviceKey       device key
     * @param configNodeReset Wrapper class {@link ConfigNodeReset} containing the opcode and parameters for {@link ConfigNodeReset} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     * @deprecated in favour of {@link ConfigNodeResetState}
     */
    @Deprecated
    ConfigNodeResetState(@NonNull final Context context,
                         @NonNull final byte[] src,
                         @NonNull final byte[] dst,
                         @NonNull final byte[] deviceKey,
                         @NonNull final ConfigNodeReset configNodeReset,
                         @NonNull final MeshTransport meshTransport,
                         @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), deviceKey, configNodeReset, meshTransport, callbacks);
    }

    /**
     * Constructs {@link ConfigNodeResetState}
     *
     * @param context         Context of the application
     * @param src             source address
     * @param dst             destination address
     * @param deviceKey       device key
     * @param configNodeReset Wrapper class {@link ConfigNodeReset} containing the opcode and parameters for {@link ConfigNodeReset} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    ConfigNodeResetState(@NonNull final Context context,
                         final int src,
                         final int dst,
                         @NonNull final byte[] deviceKey,
                         @NonNull final ConfigNodeReset configNodeReset,
                         @NonNull final MeshTransport meshTransport,
                         @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        super(context, configNodeReset, meshTransport, callbacks);
        this.mSrc = src;
        this.mDst = dst;
        this.mDeviceKey = deviceKey;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.CONFIG_NODE_RESET_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {

        final ConfigNodeReset configNodeReset = (ConfigNodeReset) mMeshMessage;
        final int akf = configNodeReset.getAkf();
        final int aid = configNodeReset.getAid();
        final int aszmic = configNodeReset.getAszmic();
        final int opCode = configNodeReset.getOpCode();
        final byte[] parameters = configNodeReset.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, mDeviceKey, akf, aid, aszmic, opCode, parameters);
        configNodeReset.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending config node reset");
        super.executeSend();

        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
        }
    }
}
