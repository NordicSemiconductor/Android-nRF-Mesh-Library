package no.nordicsemi.android.mesh.transport;


import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;

/**
 * Creates the ConfigFriendGet message.
 */
public class ConfigFriendGet extends ConfigMessage {

    private static final String TAG = ConfigFriendGet.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_FRIEND_GET;

    /**
     * Constructs ConfigFriendGet message.
     */
    public ConfigFriendGet() {
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
