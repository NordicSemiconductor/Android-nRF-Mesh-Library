package no.nordicsemi.android.mesh.transport;


import android.util.Log;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * Creates the ConfigBeaconStatus message.
 */
public class ConfigBeaconStatus extends ConfigStatusMessage {

    private static final String TAG = ConfigBeaconStatus.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_BEACON_STATUS;

    private boolean enable;

    /**
     * Constructs ConfigBeaconStatus message.
     *
     * @param message {@link AccessMessage}
     */
    public ConfigBeaconStatus(@NonNull final AccessMessage message) {
        super(message);
        mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {
        enable = MeshParserUtils.unsignedByteToInt(mParameters[0]) == ProvisionedBaseMeshNode.ENABLED;
        Log.d(TAG, "Secure Network Beacon State: " + enable);
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    /**
     * Returns the true if the Secure Network beacon State is set to send periodic Secure Network Beacons or false otherwise.
     */
    public boolean isEnable() {
        return enable;
    }
}
