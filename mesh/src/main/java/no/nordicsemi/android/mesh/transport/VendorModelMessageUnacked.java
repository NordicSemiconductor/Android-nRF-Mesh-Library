package no.nordicsemi.android.mesh.transport;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.utils.SecureUtils;

/**
 * To be used as a wrapper class when creating a unacknowledged VendorModel message.
 */
@SuppressWarnings("unused")
public class VendorModelMessageUnacked extends ApplicationMessage {

    private static final String TAG = VendorModelMessageUnacked.class.getSimpleName();

    private final int mModelIdentifier;
    private final int mCompanyIdentifier;
    private final int mOpCode;

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
                                     final int mOpCode,
                                     @Nullable final byte[] parameters) {
        super(appKey);
        this.mModelIdentifier = modelId;
        this.mCompanyIdentifier = companyIdentifier;
        this.mOpCode = mOpCode;
        mParameters = parameters;
        assembleMessageParameters();
    }

    @Override
    final void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
    }

    @Override
    public int getOpCode() {
        return mOpCode;
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
