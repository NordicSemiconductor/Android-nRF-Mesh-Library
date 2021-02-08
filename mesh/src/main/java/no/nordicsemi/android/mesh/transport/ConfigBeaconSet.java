package no.nordicsemi.android.mesh.transport;


import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;

/**
 * Creates the ConfigBeaconSet message.
 */
public class ConfigBeaconSet extends ConfigMessage {

    private static final String TAG = ConfigBeaconSet.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_BEACON_SET;
    private final boolean enable;

    /**
     * Constructs ConfigBeaconSet message.
     *
     * @param enable True to enable sending periodic Secure Network Beacons or false otherwise
     */
    public ConfigBeaconSet(final boolean enable) {
        this.enable = enable;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mParameters = new byte[]{(byte) (enable ? 0x01 : 0x00)};
    }
}
