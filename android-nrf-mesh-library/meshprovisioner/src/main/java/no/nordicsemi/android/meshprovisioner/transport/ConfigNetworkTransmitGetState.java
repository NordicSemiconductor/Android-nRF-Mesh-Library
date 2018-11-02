package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;


public final class ConfigNetworkTransmitGetState extends ConfigMessageState {

    private static final String TAG = ConfigNetworkTransmitGetState.class.getSimpleName();


    public ConfigNetworkTransmitGetState(@NonNull final Context context,
                                         @NonNull final ConfigNetworkTransmitGet configNetworkTransmitGet,
                                         @NonNull final MeshTransport meshTransport,
                                         @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        super(context, configNetworkTransmitGet, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MeshMessageState.MessageState getState() {
        return MeshMessageState.MessageState.CONFIG_NETWORK_TRANSMIT_GET_STATE;
    }

    private void createAccessMessage() {
        final byte[] key = mNode.getDeviceKey();
        final ConfigNetworkTransmitGet configNetworkTransmitGet = (ConfigNetworkTransmitGet) mMeshMessage;
        final int akf = configNetworkTransmitGet.getAkf();
        final int aid = configNetworkTransmitGet.getAid();
        final int aszmic = configNetworkTransmitGet.getAszmic();
        final int opCode = configNetworkTransmitGet.getOpCode();
        final byte[] parameters = configNetworkTransmitGet.getParameters();
        message = mMeshTransport.createMeshMessage(mNode, mSrc, key, akf, aid, aszmic, opCode, parameters);
        configNetworkTransmitGet.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending config network transmit get");
        super.executeSend();

        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mMeshMessage);
        }
    }
}
