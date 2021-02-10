package no.nordicsemi.android.mesh.transport;


import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * Creates the ConfigNodeIdentitySet message.
 */
public class ConfigNodeIdentitySet extends ConfigMessage {

    private static final String TAG = ConfigNodeIdentitySet.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_NODE_IDENTITY_SET;
    private final NetworkKey networkKey;
    private final boolean enable;

    /**
     * Constructs ConfigNodeIdentitySet message.
     *
     * @param networkKey NetworkKey.
     * @param enable     True to enable advertising NodeIdentity.
     */
    public ConfigNodeIdentitySet(@NonNull final NetworkKey networkKey, final boolean enable) {
        this.networkKey = networkKey;
        this.enable = enable;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        final byte[] netKeyIndex = MeshParserUtils.addKeyIndexPadding(networkKey.getKeyIndex());
        mParameters = new byte[]{netKeyIndex[1], (byte) ((netKeyIndex[0] & 0xFF) & 0x0F), (byte) (enable ? 0x01 : 0x00)};
    }
}
