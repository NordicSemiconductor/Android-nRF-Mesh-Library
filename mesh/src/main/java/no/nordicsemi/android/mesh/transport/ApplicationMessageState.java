package no.nordicsemi.android.mesh.transport;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.utils.MeshAddress;

/**
 * Abstract state class that handles Application Message States
 */
class ApplicationMessageState extends MeshMessageState {

    UUID mLabel;

    /**
     * Constructs the application message state
     *
     * @param src           Source address
     * @param dst           Destination address
     * @param meshMessage   {@link MeshMessage} to be sent
     * @param meshTransport {@link MeshTransport} transport
     * @param callbacks     {@link InternalMeshMsgHandlerCallbacks} callbacks
     * @throws IllegalArgumentException if src or dst address is invalid
     */
    ApplicationMessageState(final int src,
                            final int dst,
                            @NonNull final MeshMessage meshMessage,
                            @NonNull final MeshTransport meshTransport,
                            @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        this(src, dst, null, meshMessage, meshTransport, callbacks);
    }

    /**
     * Constructs the application message state
     *
     * @param src           Source address
     * @param dst           Destination address
     * @param label         Label UUID of destination address
     * @param meshMessage   {@link MeshMessage} to be sent
     * @param meshTransport {@link MeshTransport} transport
     * @param callbacks     {@link InternalMeshMsgHandlerCallbacks} callbacks
     * @throws IllegalArgumentException if src or dst address is invalid
     */
    ApplicationMessageState(final int src,
                            final int dst,
                            @Nullable final UUID label,
                            @NonNull final MeshMessage meshMessage,
                            @NonNull final MeshTransport meshTransport,
                            @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(meshMessage, meshTransport, callbacks);
        this.mSrc = src;
        if (!MeshAddress.isAddressInRange(src)) {
            throw new IllegalArgumentException("Invalid address, a source address must be a valid 16-bit value!");
        }
        this.mDst = dst;
        if (!MeshAddress.isAddressInRange(dst)) {
            throw new IllegalArgumentException("Invalid address, a destination address must be a valid 16-bit value");
        }
        mLabel = label;
        createAccessMessage();
    }

    /**
     * Creates the access message to be sent
     */
    protected void createAccessMessage() {
        final ApplicationMessage applicationMessage = (ApplicationMessage) mMeshMessage;
        final ApplicationKey key = applicationMessage.getAppKey();
        final int akf = applicationMessage.getAkf();
        final int aid = applicationMessage.getAid();
        final int aszmic = applicationMessage.getAszmic();
        final int opCode = applicationMessage.getOpCode();
        final byte[] parameters = applicationMessage.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, mLabel, applicationMessage.messageTtl,
                key, akf, aid, aszmic, opCode, parameters);
        applicationMessage.setMessage(message);
    }

    @Override
    public MessageState getState() {
        return MessageState.APPLICATION_MESSAGE_STATE;
    }
}
