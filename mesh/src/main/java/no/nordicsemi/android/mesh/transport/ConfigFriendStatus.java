package no.nordicsemi.android.mesh.transport;


import android.util.Log;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * Creates the ConfigFriendStatus message.
 */
public class ConfigFriendStatus extends ConfigStatusMessage {

    private static final String TAG = ConfigFriendStatus.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_FRIEND_STATUS;

    private boolean enable;

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
        enable = MeshParserUtils.unsignedByteToInt(mParameters[0]) == ProvisionedBaseMeshNode.ENABLED;
        Log.d(TAG, "Friend status: " + enable);
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    /**
     * Returns the true if the Friend feature is enabled or not.
     */
    public boolean isEnable() {
        return enable;
    }
}
