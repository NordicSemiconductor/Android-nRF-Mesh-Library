package no.nordicsemi.android.mesh.transport;


import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * Creates the ConfigNodeIdentityStatus message.
 */
public class ConfigNodeIdentityStatus extends ConfigStatusMessage {

    private static final String TAG = ConfigNodeIdentityStatus.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_BEACON_STATUS;
    private int netKeyIndex;
    private boolean enable;

    /**
     * Constructs ConfigNodeIdentityStatus message.
     *
     * @param message {@link AccessMessage}
     */
    public ConfigNodeIdentityStatus(@NonNull final AccessMessage message) {
        super(message);
        mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {
        mStatusCode = mParameters[0];
        final byte[] netKeyIndex = new byte[]{(byte) (mParameters[2] & 0x0F), mParameters[1]};
        this.netKeyIndex = ByteBuffer.wrap(netKeyIndex).order(ByteOrder.BIG_ENDIAN).getShort();
        enable = MeshParserUtils.unsignedByteToInt(mParameters[3]) == ProvisionedBaseMeshNode.ENABLED;
        Log.d(TAG, "Node Identity State: " + enable);
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    /**
     * Returns if the message was successful
     *
     * @return true if the message was successful or false otherwise
     */
    public final boolean isSuccessful() {
        return mStatusCode == 0x00;
    }

    /**
     * Returns true if the Node is advertising with Node Identity for a subnet is running or false otherwise.
     */
    public boolean isEnable() {
        return enable;
    }

    /**
     * Returns the net key index.
     */
    public int getNetKeyIndex() {
        return netKeyIndex;
    }
}
