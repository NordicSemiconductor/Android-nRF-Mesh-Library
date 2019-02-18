package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State that handles the ConfigRelayGet message
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class ConfigRelaySetState extends ConfigMessageState {

    private static final String TAG = ConfigRelaySetState.class.getSimpleName();
    private final byte[] mDeviceKey;

    /**
     * Constructs the state for {@link ConfigRelayGet} message
     *
     * @param context        context
     * @param src            source address
     * @param dst            destination address
     * @param deviceKey      device key
     * @param configRelaySet {@link ConfigRelaySet} message
     * @param meshTransport  {@link MeshTransport}
     * @param callbacks      {@link InternalMeshMsgHandlerCallbacks} Internal mesh handler callbacks
     */
    ConfigRelaySetState(@NonNull final Context context,
                        @NonNull final byte[] src,
                        @NonNull final byte[] dst,
                        @NonNull final byte[] deviceKey,
                        @NonNull final ConfigRelaySet configRelaySet,
                        @NonNull final MeshTransport meshTransport,
                        @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), deviceKey, configRelaySet, meshTransport, callbacks);
    }

    /**
     * Constructs the state for {@link ConfigRelayGet} message
     *
     * @param context        context
     * @param src            source address
     * @param dst            destination address
     * @param deviceKey      device key
     * @param configRelaySet {@link ConfigRelaySet} message
     * @param meshTransport  {@link MeshTransport}
     * @param callbacks      {@link InternalMeshMsgHandlerCallbacks} Internal mesh handler callbacks
     */
    ConfigRelaySetState(@NonNull final Context context,
                        final int src,
                        final int dst,
                        @NonNull final byte[] deviceKey,
                        @NonNull final ConfigRelaySet configRelaySet,
                        @NonNull final MeshTransport meshTransport,
                        @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        super(context, configRelaySet, meshTransport, callbacks);
        this.mSrc = src;
        this.mDst = dst;
        this.mDeviceKey = deviceKey;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.CONFIG_RELAY_SET_STATE;
    }

    private void createAccessMessage() {
        final ConfigRelaySet configRelaySet = (ConfigRelaySet) mMeshMessage;
        final int akf = configRelaySet.getAkf();
        final int aid = configRelaySet.getAid();
        final int aszmic = configRelaySet.getAszmic();
        final int opCode = configRelaySet.getOpCode();
        final byte[] parameters = configRelaySet.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, mDeviceKey, akf, aid, aszmic, opCode, parameters);
        configRelaySet.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending config relay set");
        super.executeSend();

        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
        }
    }
}
