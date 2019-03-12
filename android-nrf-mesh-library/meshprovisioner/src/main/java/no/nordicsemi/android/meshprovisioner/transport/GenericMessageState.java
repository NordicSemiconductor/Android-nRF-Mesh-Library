package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;

import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;

/**
 * Abstract state class that handles Generic Message States
 */
class GenericMessageState extends MeshMessageState {

    GenericMessageState(@NonNull final Context context,
                        final int src,
                        final int dst,
                        @NonNull final MeshMessage meshMessage,
                        @NonNull final MeshTransport meshTransport,
                        @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, meshMessage, meshTransport, callbacks);
        if (!MeshAddress.isAddressInRange(src))
            throw new IllegalArgumentException("Invalid address, a source address must be a valid 16-bit value!");
        this.mSrc = src;
        if (!MeshAddress.isAddressInRange(dst))
            throw new IllegalArgumentException("Invalid address, a destination address must be a valid 16-bit value");
        this.mDst = dst;
        createAccessMessage();
    }

    /**
     * Creates the access message to be sent
     */
    protected void createAccessMessage() {
        final GenericMessage genericMessage = (GenericMessage) mMeshMessage;
        final byte[] key = genericMessage.getAppKey();
        final int akf = genericMessage.getAkf();
        final int aid = genericMessage.getAid();
        final int aszmic = genericMessage.getAszmic();
        final int opCode = genericMessage.getOpCode();
        final byte[] parameters = genericMessage.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, key, akf, aid, aszmic, opCode, parameters);
    }

    @Override
    public MessageState getState() {
        return MessageState.GENERIC_MESSAGE_STATE;
    }
}
