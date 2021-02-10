package no.nordicsemi.android.mesh.transport;


import android.util.Log;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

import static no.nordicsemi.android.mesh.transport.ProvisionedBaseMeshNode.NodeIdentityState;

/**
 * Creates the ConfigNodeIdentitySet message.
 */
public class ConfigNodeIdentitySet extends ConfigMessage {

    private static final String TAG = ConfigNodeIdentitySet.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_NODE_IDENTITY_SET;
    private final NetworkKey networkKey;
    @NodeIdentityState
    private final int nodeIdentityState;

    /**
     * Constructs ConfigNodeIdentitySet message.
     *
     * @param networkKey        NetworkKey.
     * @param nodeIdentityState True to enable advertising NodeIdentity.
     */
    public ConfigNodeIdentitySet(@NonNull final NetworkKey networkKey, @NodeIdentityState final int nodeIdentityState) {
        this.networkKey = networkKey;
        this.nodeIdentityState = nodeIdentityState;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        Log.d(TAG, "Node Identity: " + nodeIdentityState);
        final byte[] netKeyIndex = MeshParserUtils.addKeyIndexPadding(networkKey.getKeyIndex());
        mParameters = new byte[]{netKeyIndex[1], (byte) ((netKeyIndex[0] & 0xFF) & 0x0F), (byte) nodeIdentityState};
    }
}
