package no.nordicsemi.android.mesh.transport;

import static no.nordicsemi.android.mesh.transport.TimeStatus.SUB_SECOND_BIT_SIZE;
import static no.nordicsemi.android.mesh.transport.TimeStatus.TAI_SECONDS_BIT_SIZE;
import static no.nordicsemi.android.mesh.transport.TimeStatus.TIME_AUTHORITY_BIT_SIZE;
import static no.nordicsemi.android.mesh.transport.TimeStatus.TIME_BIT_SIZE;
import static no.nordicsemi.android.mesh.transport.TimeStatus.TIME_ZONE_OFFSET_BIT_SIZE;
import static no.nordicsemi.android.mesh.transport.TimeStatus.UNCERTAINTY_BIT_SIZE;
import static no.nordicsemi.android.mesh.transport.TimeStatus.UTC_DELTA_BIT_SIZE;

import androidx.annotation.NonNull;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.MeshTAITime;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.ArrayUtils;
import no.nordicsemi.android.mesh.utils.BitWriter;
import no.nordicsemi.android.mesh.utils.SecureUtils;

public class TimeSet extends ApplicationMessage {

    private final MeshTAITime taiTime;

    /**
     * Time Set is an acknowledged message used to set the Time state of an element (see Section 5.1.1).
     * The response to the Time Set message is a Time Status message.
     *
     * @param taiTime The time in TAI format.
     */
    public TimeSet(@NonNull final ApplicationKey appKey, MeshTAITime taiTime) {
        super(appKey);
        this.taiTime = taiTime;
        assembleMessageParameters();
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        BitWriter bitWriter = new BitWriter(TIME_BIT_SIZE);
        bitWriter.write(taiTime.getTaiSeconds(), TAI_SECONDS_BIT_SIZE);
        bitWriter.write(taiTime.getSubSecond(), SUB_SECOND_BIT_SIZE);
        bitWriter.write(taiTime.getUncertainty(), UNCERTAINTY_BIT_SIZE);
        if (taiTime.isTimeAuthority()) {
            bitWriter.write(1, TIME_AUTHORITY_BIT_SIZE);
        } else {
            bitWriter.write(0, TIME_AUTHORITY_BIT_SIZE);
        }
        bitWriter.write(taiTime.getUtcDelta(), UTC_DELTA_BIT_SIZE);
        bitWriter.write(taiTime.getTimeZoneOffset(), TIME_ZONE_OFFSET_BIT_SIZE);

        mParameters = ArrayUtils.reverseArray(bitWriter.toByteArray());
    }

    @Override
    public int getOpCode() {
        return ApplicationMessageOpCodes.TIME_SET;
    }
}
