package no.nordicsemi.android.mesh.transport;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshAddress;

/**
 * Creates the ConfigLowPowerNodePollTimeoutGet message.
 */
public class ConfigLowPowerNodePollTimeoutGet extends ConfigMessage {

    private static final String TAG = ConfigLowPowerNodePollTimeoutGet.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_LOW_POWER_NODE_POLLTIMEOUT_GET;
    final int address;

    /**
     * Constructs ConfigDefaultTtlGet message.
     *
     * @param address Unicast address of the Low Power Node
     */
    public ConfigLowPowerNodePollTimeoutGet(final int address) {
        if (!MeshAddress.isValidUnicastAddress(address))
            throw new IllegalArgumentException("Invalid unicast address, unicast address must be a 16-bit value, and must range from 0x0001 to 0x7FFF");
        this.address = address;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        final ByteBuffer paramsBuffer = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
        paramsBuffer.putShort((short) address);
        mParameters = paramsBuffer.array();
    }
}
