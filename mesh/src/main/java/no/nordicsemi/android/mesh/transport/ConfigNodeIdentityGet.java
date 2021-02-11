package no.nordicsemi.android.mesh.transport;


import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;

/**
 * Creates the ConfigNodeIdentityGet message.
 */
public class ConfigNodeIdentityGet extends ConfigMessage {

    private static final String TAG = ConfigNodeIdentityGet.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_NODE_IDENTITY_GET;

    /**
     * Constructs ConfigNodeIdentityGet message.
     */
    public ConfigNodeIdentityGet() {
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
