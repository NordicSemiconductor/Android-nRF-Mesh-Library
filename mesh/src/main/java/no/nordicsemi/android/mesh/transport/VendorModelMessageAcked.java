package no.nordicsemi.android.mesh.transport;

import androidx.annotation.NonNull;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.utils.SecureUtils;

/**
 * To be used as a wrapper class when creating an acknowledged VendorMode message.
 */
@SuppressWarnings("unused")
public class VendorModelMessageAcked extends GenericMessage {

    private static final String TAG = VendorModelMessageAcked.class.getSimpleName();
    private static final int VENDOR_MODEL_OPCODE_LENGTH = 4;

    private final int mModelIdentifier;
    private final int mCompanyIdentifier;
    private final int mOpCode;

    /**
     * Constructs VendorModelMessageAcked message.
     *
     * @param appKey            {@link ApplicationKey} for this message
     * @param modelId           32-bit Model identifier
     * @param companyIdentifier 16-bit Company identifier of the vendor model
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public VendorModelMessageAcked(@NonNull final ApplicationKey appKey,
                                   final int modelId,
                                   final int companyIdentifier,
                                   final int opCode,
                                   @NonNull final byte[] parameters) {
        super(appKey);
        this.mModelIdentifier = modelId;
        this.mCompanyIdentifier = companyIdentifier;
        //Set the opCode to a 3-bit opCode
        this.mOpCode = (0xC0 | opCode) << 16;
        mParameters = parameters;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return mOpCode;
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
    }


    /**
     * Returns the company identifier of the model
     *
     * @return 16-bit company identifier
     */
    public final int getCompanyIdentifier() {
        return mCompanyIdentifier;
    }

    /**
     * Returns the model identifier for this message
     */
    public int getModelIdentifier() {
        return mModelIdentifier;
    }
}
