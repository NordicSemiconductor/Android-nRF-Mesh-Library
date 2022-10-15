package no.nordicsemi.android.mesh.transport;

import no.nordicsemi.android.mesh.logger.MeshLogger;

import androidx.annotation.NonNull;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.SecureUtils;

/**
 * To be used as a wrapper class when creating a TimeZoneGet message.
 */
public class TimeZoneGet extends ApplicationMessage {

    private static final String TAG = TimeZoneGet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.TIME_ZONE_GET;

    /**
     * Constructs TimeZoneGet message.
     *
     * @param appKey application key for this message
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public TimeZoneGet(@NonNull final ApplicationKey appKey) throws IllegalArgumentException {
        super(appKey);
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        MeshLogger.verbose(TAG, "Creating message");
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
    }
}
