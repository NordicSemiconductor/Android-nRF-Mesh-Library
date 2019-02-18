package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling VendorModelMessageAckedState messages.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
class VendorModelMessageAckedState extends GenericMessageState {

    private static final String TAG = VendorModelMessageAckedState.class.getSimpleName();

    /**
     * Constructs {@link VendorModelMessageAckedState}
     *
     * @param context                 Context of the application
     * @param src                     Source address
     * @param dst                     Destination address to which the message must be sent to
     * @param vendorModelMessageAcked Wrapper class {@link VendorModelMessageStatus} containing the opcode and parameters for {@link VendorModelMessageStatus} message
     * @param callbacks               {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException exception for invalid arguments
     */
    @Deprecated
    VendorModelMessageAckedState(@NonNull final Context context,
                                 @NonNull final byte[] src,
                                 @NonNull final byte[] dst,
                                 @NonNull final VendorModelMessageAcked vendorModelMessageAcked,
                                 @NonNull final MeshTransport meshTransport,
                                 @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), vendorModelMessageAcked, meshTransport, callbacks);
    }

    /**
     * Constructs {@link VendorModelMessageAckedState}
     *
     * @param context                 Context of the application
     * @param src                     Source address
     * @param dst                     Destination address to which the message must be sent to
     * @param vendorModelMessageAcked Wrapper class {@link VendorModelMessageStatus} containing the opcode and parameters for {@link VendorModelMessageStatus} message
     * @param callbacks               {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException exception for invalid arguments
     */
    VendorModelMessageAckedState(@NonNull final Context context,
                                 final int src,
                                 final int dst,
                                 @NonNull final VendorModelMessageAcked vendorModelMessageAcked,
                                 @NonNull final MeshTransport meshTransport,
                                 @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, vendorModelMessageAcked, meshTransport, callbacks);
        this.mSrc = src;
        this.mDst = dst;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.VENDOR_MODEL_ACKNOWLEDGED_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final VendorModelMessageAcked vendorModelMessageAcked = (VendorModelMessageAcked) mMeshMessage;
        final byte[] key = vendorModelMessageAcked.getAppKey();
        final int akf = vendorModelMessageAcked.getAkf();
        final int aid = vendorModelMessageAcked.getAid();
        final int aszmic = vendorModelMessageAcked.getAszmic();
        final int opCode = vendorModelMessageAcked.getOpCode();
        final byte[] parameters = vendorModelMessageAcked.getParameters();
        final int companyIdentifier = vendorModelMessageAcked.getCompanyIdentifier();
        message = mMeshTransport.createVendorMeshMessage(companyIdentifier, mSrc, mDst, key, akf, aid, aszmic, opCode, parameters);
        vendorModelMessageAcked.setMessage(message);
    }

    @Override
    public void executeSend() {
        Log.v(TAG, "Sending acknowledged vendor model message");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
        }
    }
}
