package no.nordicsemi.android.meshprovisioner.transport;


import no.nordicsemi.android.meshprovisioner.opcodes.ConfigMessageOpCodes;

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
     * @param node                Mesh node this message is to be sent to.
     * @param aszmic              Size of message integrity check
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public ConfigNodeReset(final ProvisionedMeshNode node, final int aszmic)  {
        super(node, aszmic);
        createAccessMessage();
    }

    private void createAccessMessage() {
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
