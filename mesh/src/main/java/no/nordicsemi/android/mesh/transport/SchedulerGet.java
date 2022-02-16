package no.nordicsemi.android.mesh.transport;

import androidx.annotation.NonNull;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.SecureUtils;

public class SchedulerGet extends ApplicationMessage {

    private static final int OP_CODE = ApplicationMessageOpCodes.SCHEDULER_GET;

    /**
     * Scheduler Get is an acknowledged message used to get the current Schedule Register state of an element (see Mesh Model Spec. v1.0.1 Section 5.1.4.2).
     * The response to the Scheduler Get message is a Scheduler Status message.
     *
     * @param appKey {@link ApplicationKey} key for this message
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public SchedulerGet(@NonNull final ApplicationKey appKey) throws IllegalArgumentException {
        super(appKey);
        assembleMessageParameters();
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }
}
