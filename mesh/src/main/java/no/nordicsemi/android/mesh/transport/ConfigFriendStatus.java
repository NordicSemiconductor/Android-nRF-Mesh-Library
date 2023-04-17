package no.nordicsemi.android.mesh.transport;


import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.logger.MeshLogger;
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * Creates the ConfigFriendStatus message.
 */
public class ConfigFriendStatus extends ConfigStatusMessage {

    private static final String TAG = ConfigFriendStatus.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_FRIEND_STATUS;

    private boolean enabled;

    /**
     * Constructs ConfigFriendStatus message.
     *
     * @param message {@link AccessMessage}
     */
    public ConfigFriendStatus(@NonNull final AccessMessage message) {
        super(message);
        mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {
        enabled = MeshParserUtils.unsignedByteToInt(mParameters[0]) == ProvisionedBaseMeshNode.ENABLED;
        MeshLogger.debug(TAG, "Friend status: " + enabled);
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    /**
     * Returns the true if the Friend feature is enabled or not.
     */
    public boolean isEnabled() {
        return enabled;
    }
}
