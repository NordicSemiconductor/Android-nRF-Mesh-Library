package no.nordicsemi.android.mesh.transport;

import no.nordicsemi.android.mesh.logger.MeshLogger;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.BitWriter;
import no.nordicsemi.android.mesh.utils.SecureUtils;

import static android.content.ContentValues.TAG;

public class SchedulerActionGet extends ApplicationMessage {

    private static final int OP_CODE = ApplicationMessageOpCodes.SCHEDULER_ACTION_GET;
    private final int index;


    /**
     * Scheduler Action Get is an acknowledged message used to report the action defined by the entry of
     * the Schedule Register state of an element (see Mesh Model Spec. v1.0.1 Section 5.1.4.2), identified by the Index field.
     * <p>
     * The response to the Scheduler Action Get message is a Scheduler Action Status message.
     *
     * @param appKey the appkey
     * @param index  Index of the Schedule Register entry to get
     */
    public SchedulerActionGet(@NonNull ApplicationKey appKey, int index) {
        super(appKey);
        this.index = Math.min(index, 0x0F);
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        MeshLogger.verbose(TAG, SchedulerActionGet.class.getSimpleName() +  " with index: " + index);

        mParameters = new byte[] { (byte) index };
    }
}
