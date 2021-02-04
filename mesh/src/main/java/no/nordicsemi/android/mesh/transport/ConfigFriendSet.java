package no.nordicsemi.android.mesh.transport;


import no.nordicsemi.android.mesh.Features;
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;

/**
 * Creates the ConfigFriendSet message.
 */
public class ConfigFriendSet extends ConfigMessage {

    private static final String TAG = ConfigFriendSet.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_FRIEND_SET;
    private final boolean enable;

    /**
     * Constructs ConfigFriendSet message.
     *
     * @param enable True to enable friend feature or false otherwise.
     */
    public ConfigFriendSet(final boolean enable) {
        this.enable = enable;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mParameters = new byte[]{(byte) (enable ? Features.ENABLED : Features.DISABLED)};
    }
}
