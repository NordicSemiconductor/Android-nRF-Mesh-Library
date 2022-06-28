package no.nordicsemi.android.mesh.transport;

import no.nordicsemi.android.mesh.logger.MeshLogger;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.InternalTransportCallbacks;
import no.nordicsemi.android.mesh.MeshStatusCallbacks;

/**
 * State class for handling VendorModelMessageAckedState messages.
 */
class VendorModelMessageAckedState extends ApplicationMessageState {

    private static final String TAG = VendorModelMessageAckedState.class.getSimpleName();

    /**
     * Constructs {@link VendorModelMessageAckedState}
     *
     * @param src                     Source address
     * @param dst                     Destination address to which the message must be sent to
     * @param vendorModelMessageAcked Wrapper class {@link VendorModelMessageStatus} containing the
     *                                opcode and parameters for {@link VendorModelMessageStatus} message
     * @param handlerCallbacks          {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @param transportCallbacks        {@link InternalTransportCallbacks} callbacks
     * @param statusCallbacks           {@link MeshStatusCallbacks} callbacks
     * @throws IllegalArgumentException exception for invalid arguments
     */
    VendorModelMessageAckedState(final int src,
                                 final int dst,
                                 @NonNull final VendorModelMessageAcked vendorModelMessageAcked,
                                 @NonNull final MeshTransport meshTransport,
                                 @NonNull final InternalMeshMsgHandlerCallbacks handlerCallbacks,
                                 @NonNull final InternalTransportCallbacks transportCallbacks,
                                 @NonNull  final MeshStatusCallbacks statusCallbacks) throws IllegalArgumentException {
        this(src, dst, null, vendorModelMessageAcked, meshTransport, handlerCallbacks, transportCallbacks, statusCallbacks);
    }

    /**
     * Constructs {@link VendorModelMessageAckedState}
     *
     * @param src                     Source address
     * @param dst                     Destination address to which the message must be sent to
     * @param label                   Label UUID of destination address
     * @param vendorModelMessageAcked Wrapper class {@link VendorModelMessageStatus} containing the
     *                                opcode and parameters for {@link VendorModelMessageStatus} message
     * @param handlerCallbacks          {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @param transportCallbacks        {@link InternalTransportCallbacks} callbacks
     * @param statusCallbacks           {@link MeshStatusCallbacks} callbacks
     * @throws IllegalArgumentException exception for invalid arguments
     */
    VendorModelMessageAckedState(final int src,
                                 final int dst,
                                 @Nullable UUID label,
                                 @NonNull final VendorModelMessageAcked vendorModelMessageAcked,
                                 @NonNull final MeshTransport meshTransport,
                                 @NonNull final InternalMeshMsgHandlerCallbacks handlerCallbacks,
                                 @NonNull final InternalTransportCallbacks transportCallbacks,
                                 @NonNull  final MeshStatusCallbacks statusCallbacks) throws IllegalArgumentException {
        super(src, dst, label, vendorModelMessageAcked, meshTransport, handlerCallbacks, transportCallbacks, statusCallbacks);
    }

    @Override
    public MessageState getState() {
        return MessageState.VENDOR_MODEL_ACKNOWLEDGED_STATE;
    }

    @Override
    protected final void createAccessMessage() {
        final VendorModelMessageAcked message = (VendorModelMessageAcked) mMeshMessage;
        final ApplicationKey key = message.getAppKey();
        final int akf = message.getAkf();
        final int aid = message.getAid();
        final int aszmic = message.getAszmic();
        final int opCode = message.getOpCode();
        final byte[] parameters = message.getParameters();
        final int companyIdentifier = message.getCompanyIdentifier();
        this.message = mMeshTransport.createVendorMeshMessage(companyIdentifier, mSrc, mDst, mLabel,
                message.messageTtl, key, akf, aid, aszmic, opCode, parameters);
        message.setMessage(this.message);
    }

    @Override
    public void executeSend() {
        MeshLogger.verbose(TAG, "Sending acknowledged vendor model message");
        super.executeSend();
    }
}
