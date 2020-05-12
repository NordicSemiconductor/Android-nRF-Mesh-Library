package no.nordicsemi.android.mesh.transport;


import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;

/**
 * To be used as a wrapper class to create a ConfigNodeReset message.
 */
@SuppressWarnings("unused")
public class ConfigNodeReset extends ConfigMessage {

    private static final String TAG = ConfigNodeReset.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_NODE_RESET;

    /**
     * Constructs ConfigNodeReset message.
     *
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public ConfigNodeReset() {
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
