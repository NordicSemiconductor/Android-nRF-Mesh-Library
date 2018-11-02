package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;


public final class ConfigNetworkTransmitSetState extends ConfigMessageState {

    private static final String TAG = ConfigNetworkTransmitSetState.class.getSimpleName();


    public ConfigNetworkTransmitSetState(@NonNull final Context context,
                                         @NonNull final ConfigNetworkTransmitSet configNetworkTransmitSet,
                                         @NonNull final MeshTransport meshTransport,
                                         @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        super(context, configNetworkTransmitSet, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.CONFIG_NETWORK_TRANSMIT_SET_STATE;
    }

    private void createAccessMessage() {
        final byte[] key = mNode.getDeviceKey();
        final ConfigNetworkTransmitSet configNetworkTransmitSet = (ConfigNetworkTransmitSet) mMeshMessage;
        final int akf = configNetworkTransmitSet.getAkf();
        final int aid = configNetworkTransmitSet.getAid();
        final int aszmic = configNetworkTransmitSet.getAszmic();
        final int opCode = configNetworkTransmitSet.getOpCode();
        final byte[] parameters = configNetworkTransmitSet.getParameters();
        Log.v(TAG, "State, parameters: " + MeshParserUtils.bytesToHex(parameters, false));
        message = mMeshTransport.createMeshMessage(mNode, mSrc, key, akf, aid, aszmic, opCode, parameters);
        configNetworkTransmitSet.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending config network transmit set");
        super.executeSend();

        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mMeshMessage);
        }
    }
}
