package no.nordicsemi.android.mesh.transport;

import androidx.annotation.NonNull;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.SecureUtils;

public class TimeRoleSet extends ApplicationMessage {

    private static final int OP_CODE = ApplicationMessageOpCodes.TIME_ROLE_SET;
    private static final int TIME_ROLE_SET_PARAMS_LENGTH = 1;

    private final byte timeRole;

    /**
     * Constructs TimeRoleSet message
     * @param appKey Application key for this message
     * @param timeRole Time role value (0x00 = None, 0x01 = Mesh Time Authority, 0x02 = Mesh Time Relay, 0x03 = Mesh Time Client)
     */
    public TimeRoleSet(@NonNull final ApplicationKey appKey, final byte timeRole) {
        super(appKey);
        this.timeRole = timeRole;
        assembleMessageParameters();
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        final byte[] parameters = new byte[TIME_ROLE_SET_PARAMS_LENGTH];
        parameters[0] = timeRole;
        mParameters = parameters;
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    public byte getTimeRole() {
        return timeRole;
    }
}