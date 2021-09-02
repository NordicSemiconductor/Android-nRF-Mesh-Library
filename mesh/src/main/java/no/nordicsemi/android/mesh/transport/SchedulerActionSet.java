package no.nordicsemi.android.mesh.transport;

import androidx.annotation.NonNull;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.data.ScheduleEntry;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.ArrayUtils;
import no.nordicsemi.android.mesh.utils.BitWriter;
import no.nordicsemi.android.mesh.utils.SecureUtils;


public class SchedulerActionSet extends ApplicationMessage {

    private static final int OP_CODE = ApplicationMessageOpCodes.SCHEDULER_ACTION_SET;
    private static final int SCHEDULER_ACTION_SET_INDEX_PARAMS_LENGTH = 4 + ScheduleEntry.SCHEDULER_ENTRY_PARAMS_BITS_LENGTH;

    private final int index;
    private final ScheduleEntry entry;

    /**
     * Scheduler Action Set is an acknowledged message used to set the entry of the Schedule Register state of an element (see Mesh Model Spec. v1.0.1 Section 5.1.4.2), identified by the Index field.
     * The response to the Scheduler Action Set message is a Scheduler Action Status message.
     *
     * @param appKey the appkey
     * @param index  Index of the Schedule Register entry to set
     * @param entry  Bit field defining an entry in the Schedule Register
     */
    public SchedulerActionSet(@NonNull ApplicationKey appKey, int index, ScheduleEntry entry) {
        super(appKey);
        this.index = index;
        this.entry = entry;
        assembleMessageParameters();
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        BitWriter bitWriter = new BitWriter(SCHEDULER_ACTION_SET_INDEX_PARAMS_LENGTH);
        entry.assembleMessageParameters(bitWriter);
        bitWriter.write(index, 4);
        mParameters = ArrayUtils.reverseArray(bitWriter.toByteArray());
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }
}
