package no.nordicsemi.android.mesh.transport;


import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;

/**
 * Creates the ConfigDefaultTtlGet message.
 */
@SuppressWarnings("unused")
public class ConfigDefaultTtlGet extends ConfigMessage {

    private static final String TAG = ConfigDefaultTtlGet.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_DEFAULT_TTL_GET;

    /**
     * Constructs ConfigDefaultTtlGet message.
     */
    public ConfigDefaultTtlGet() {
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        //Do nothing as ConfigNodeReset message does not have parameters
    }
}
