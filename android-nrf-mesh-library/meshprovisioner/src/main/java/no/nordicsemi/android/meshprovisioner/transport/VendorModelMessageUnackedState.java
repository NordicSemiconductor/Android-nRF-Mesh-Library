package no.nordicsemi.android.meshprovisioner.transport;

import androidx.annotation.NonNull;
import android.util.Log;

@SuppressWarnings({"WeakerAccess", "unused"})
class VendorModelMessageUnackedState extends GenericMessageState {

    private static final String TAG = VendorModelMessageUnackedState.class.getSimpleName();

    /**
     * Constructs {@link VendorModelMessageAckedState}
     *
     * @param src                       Source address
     * @param dst                       Destination address to which the message must be sent to
     * @param vendorModelMessageUnacked Wrapper class {@link VendorModelMessageStatus} containing the
     *                                  opcode and parameters for {@link VendorModelMessageStatus} message
     * @param callbacks                 {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException exception for invalid arguments
     */
    VendorModelMessageUnackedState(final int src,
                                   final int dst,
                                   @NonNull final VendorModelMessageUnacked vendorModelMessageUnacked,
                                   @NonNull final MeshTransport meshTransport,
                                   @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(src, dst, vendorModelMessageUnacked, meshTransport, callbacks);
        this.mSrc = src;
        this.mDst = dst;
        createAccessMessage();
    }

    @Override
    public MeshMessageState.MessageState getState() {
        return MessageState.VENDOR_MODEL_UNACKNOWLEDGED_STATE;
    }

    @Override
    protected final void createAccessMessage() {
        final VendorModelMessageUnacked vendorModelMessageUnacked = (VendorModelMessageUnacked) mMeshMessage;
        final ApplicationKey key = vendorModelMessageUnacked.getAppKey();
        final int akf = vendorModelMessageUnacked.getAkf();
        final int aid = vendorModelMessageUnacked.getAid();
        final int aszmic = vendorModelMessageUnacked.getAszmic();
        final int opCode = vendorModelMessageUnacked.getOpCode();
        final byte[] parameters = vendorModelMessageUnacked.getParameters();
        final int companyIdentifier = vendorModelMessageUnacked.getCompanyIdentifier();
        message = mMeshTransport.createVendorMeshMessage(companyIdentifier, mSrc, mDst, key, akf, aid, aszmic, opCode, parameters);
        vendorModelMessageUnacked.setMessage(message);
    }

    @Override
    public void executeSend() {
        Log.v(TAG, "Sending acknowledged vendor model message");
        super.executeSend();
    }
}
