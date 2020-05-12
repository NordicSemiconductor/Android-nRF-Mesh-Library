package no.nordicsemi.android.mesh.transport;


import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * Creates the ConfigDefaultTtlSet message.
 */
@SuppressWarnings("unused")
public class ConfigDefaultTtlSet extends ConfigMessage {

    private static final String TAG = ConfigDefaultTtlSet.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_DEFAULT_TTL_SET;

    private int mTtl;

    /**
     * Constructs ConfigDefaultTtlSet message.
     *
     * @param ttl Time to live value. Must be either 0 or range from 2 - 127
     */
    public ConfigDefaultTtlSet(final int ttl) {
        if (!MeshParserUtils.isValidDefaultTtl(ttl)) {
            throw new IllegalArgumentException("TTL value must be either 0 or in range of 2 to 127!");
        }
        mTtl = ttl;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mParameters = new byte[]{(byte) mTtl};
        //Do nothing as ConfigNodeReset message does not have parameters
    }
}
