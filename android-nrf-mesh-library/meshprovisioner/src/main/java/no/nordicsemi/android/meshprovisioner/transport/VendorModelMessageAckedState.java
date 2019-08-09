package no.nordicsemi.android.meshprovisioner.transport;

import android.util.Log;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.meshprovisioner.ApplicationKey;

/**
 * State class for handling VendorModelMessageAckedState messages.
 */
@SuppressWarnings({"unused"})
class VendorModelMessageAckedState extends GenericMessageState {

    private static final String TAG = VendorModelMessageAckedState.class.getSimpleName();

    /**
     * Constructs {@link VendorModelMessageAckedState}
     *
     * @param src                     Source address
     * @param dst                     Destination address to which the message must be sent to
     * @param vendorModelMessageAcked Wrapper class {@link VendorModelMessageStatus} containing the
     *                                opcode and parameters for {@link VendorModelMessageStatus} message
     * @param callbacks               {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException exception for invalid arguments
     */
    VendorModelMessageAckedState(final int src,
                                 final int dst,
                                 @NonNull final VendorModelMessageAcked vendorModelMessageAcked,
                                 @NonNull final MeshTransport meshTransport,
                                 @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        this(src, dst, null, vendorModelMessageAcked, meshTransport, callbacks);
    }

    /**
     * Constructs {@link VendorModelMessageAckedState}
     *
     * @param src                     Source address
     * @param dst                     Destination address to which the message must be sent to
     * @param label                   Label UUID of destination address
     * @param vendorModelMessageAcked Wrapper class {@link VendorModelMessageStatus} containing the
     *                                opcode and parameters for {@link VendorModelMessageStatus} message
     * @param callbacks               {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException exception for invalid arguments
     */
    VendorModelMessageAckedState(final int src,
                                 final int dst,
                                 @Nullable UUID label,
                                 @NonNull final VendorModelMessageAcked vendorModelMessageAcked,
                                 @NonNull final MeshTransport meshTransport,
                                 @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(src, dst, label, vendorModelMessageAcked, meshTransport, callbacks);
    }

    @Override
    public MessageState getState() {
        return MessageState.VENDOR_MODEL_ACKNOWLEDGED_STATE;
    }

    @Override
    protected final void createAccessMessage() {
        final VendorModelMessageAcked vendorModelMessageAcked = (VendorModelMessageAcked) mMeshMessage;
        final ApplicationKey key = vendorModelMessageAcked.getAppKey();
        final int akf = vendorModelMessageAcked.getAkf();
        final int aid = vendorModelMessageAcked.getAid();
        final int aszmic = vendorModelMessageAcked.getAszmic();
        final int opCode = vendorModelMessageAcked.getOpCode();
        final byte[] parameters = vendorModelMessageAcked.getParameters();
        final int companyIdentifier = vendorModelMessageAcked.getCompanyIdentifier();
        message = mMeshTransport.createVendorMeshMessage(companyIdentifier, mSrc, mDst, mLabel, key, akf, aid, aszmic, opCode, parameters);
        vendorModelMessageAcked.setMessage(message);
    }

    @Override
    public void executeSend() {
        Log.v(TAG, "Sending acknowledged vendor model message");
        super.executeSend();
    }
}
