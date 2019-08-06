package no.nordicsemi.android.meshprovisioner.transport;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import no.nordicsemi.android.meshprovisioner.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

/**
 * To be used as a wrapper class when creating a unacknowledged VendorModel message.
 */
@SuppressWarnings("unused")
public class VendorModelMessageUnacked extends GenericMessage {

    private static final String TAG = VendorModelMessageUnacked.class.getSimpleName();

    private final int mModelIdentifier;
    private final int mCompanyIdentifier;
    private final int opCode;

    /**
     * Constructs VendorModelMessageAcked message.
     *
     * @param appKey            {@link ApplicationKey} for this message
     * @param modelId           model identifier
     * @param companyIdentifier Company identifier of the vendor model
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public VendorModelMessageUnacked(@NonNull final ApplicationKey appKey,
                                     final int modelId,
                                     final int companyIdentifier,
                                     final int opCode,
                                     @Nullable final byte[] parameters) {
        super(appKey);
        this.mModelIdentifier = modelId;
        this.mCompanyIdentifier = companyIdentifier;
        this.opCode = opCode;
        mParameters = parameters;
        assembleMessageParameters();
    }

    @Override
    final void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
    }

    @Override
    public int getOpCode() {
        return opCode;
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
