package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

@SuppressWarnings("WeakerAccess")
class VendorModelMessageUnackedState extends GenericMessageState {

    private static final String TAG = VendorModelMessageUnackedState.class.getSimpleName();

    /**
     * Constructs {@link VendorModelMessageAckedState}
     *
     * @param context                   Context of the application
     * @param src                       Source address
     * @param dst                       Destination address to which the message must be sent to
     * @param vendorModelMessageUnacked Wrapper class {@link VendorModelMessageStatus} containing the opcode and parameters for {@link VendorModelMessageStatus} message
     * @param callbacks                 {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException exception for invalid arguments
     * @deprecated in favour of {@link #VendorModelMessageUnackedState(Context, int, int, VendorModelMessageUnacked, MeshTransport, InternalMeshMsgHandlerCallbacks)}
     */
    @Deprecated
    VendorModelMessageUnackedState(@NonNull final Context context,
                                   @NonNull final byte[] src,
                                   @NonNull final byte[] dst,
                                   @NonNull final VendorModelMessageUnacked vendorModelMessageUnacked,
                                   @NonNull final MeshTransport meshTransport,
                                   @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), vendorModelMessageUnacked, meshTransport, callbacks);
    }

    /**
     * Constructs {@link VendorModelMessageAckedState}
     *
     * @param context                   Context of the application
     * @param src                       Source address
     * @param dst                       Destination address to which the message must be sent to
     * @param vendorModelMessageUnacked Wrapper class {@link VendorModelMessageStatus} containing the opcode and parameters for {@link VendorModelMessageStatus} message
     * @param callbacks                 {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException exception for invalid arguments
     */
    VendorModelMessageUnackedState(@NonNull final Context context,
                                   final int src,
                                   final int dst,
                                   @NonNull final VendorModelMessageUnacked vendorModelMessageUnacked,
                                   @NonNull final MeshTransport meshTransport,
                                   @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, vendorModelMessageUnacked, meshTransport, callbacks);
        this.mSrc = src;
        this.mDst = dst;
        createAccessMessage();
    }

    @Override
    public MeshMessageState.MessageState getState() {
        return MessageState.VENDOR_MODEL_UNACKNOWLEDGED_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final VendorModelMessageUnacked vendorModelMessageUnacked = (VendorModelMessageUnacked) mMeshMessage;
        final byte[] key = vendorModelMessageUnacked.getAppKey();
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
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null) {
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
            }
        }
    }
}
