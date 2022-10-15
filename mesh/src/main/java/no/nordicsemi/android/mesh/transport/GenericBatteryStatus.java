package no.nordicsemi.android.mesh.transport;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.logger.MeshLogger;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshAddress;

/**
 * To be used as a wrapper class to create generic level status message.
 */
@SuppressWarnings({"WeakerAccess"})
public class GenericBatteryStatus extends ApplicationStatusMessage {

    private static final String TAG = GenericBatteryStatus.class.getSimpleName();
    private static final int GENERIC_BATTERY_STATUS_MANDATORY_LENGTH = 8;
    private static final int OP_CODE = ApplicationMessageOpCodes.GENERIC_BATTERY_STATUS;
    private int mBatteryLevel;
    private int mTimeToDischarge;
    private int mTimeToCharge;
    private int mFlags;

    /**
     * Constructs GenericBatteryStatus message
     * @param message access message
     */
    public GenericBatteryStatus(@NonNull AccessMessage message) {
        super(message);
        this.mMessage = message;
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {
        MeshLogger.verbose(TAG, "Received generic battery status from: " + MeshAddress.formatAddress(mMessage.getSrc(), true));
        mBatteryLevel = mParameters[0] & 0xFF;
        MeshLogger.verbose(TAG, "Battery level: " + mBatteryLevel);
        if (mParameters.length >= GENERIC_BATTERY_STATUS_MANDATORY_LENGTH) {
            mTimeToDischarge = (mParameters[1] & 0xFF) | ((mParameters[2] & 0xFF) << 8) | ((mParameters[3] & 0xFF) << 16);
            mTimeToCharge = (mParameters[4] & 0xFF) | ((mParameters[5] & 0xFF) << 8) | ((mParameters[6] & 0xFF) << 16);
            mFlags = mParameters[7] & 0xFF;
            MeshLogger.verbose(TAG, "Time to discharge: " + mTimeToDischarge);
            MeshLogger.verbose(TAG, "Time to charge: " + mTimeToCharge);
            MeshLogger.verbose(TAG, "Flags: " + mFlags);
        }
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    /**
     * Returns the battery level of the node
     *
     * @return battery level
     */
    public int getBatteryLevel() {
        return mBatteryLevel;
    }

    /**
     * Returns the time to discharge of the battery
     *
     * @return time to discharge
     */
    public int getTimeToDischarge() {
        return mTimeToDischarge;
    }

    /**
     * Returns the time to charge of the battery
     *
     * @return time to charge
     */
    public int getTimeToCharge() {
        return mTimeToCharge;
    }


    /**
     * Returns the battery flags
     *
     * @return battery flags
     */
    public int getFlags() {
        return mFlags;
    }

    /**
     * Returns the battery presence
     *
     * @return BatteryPresence
     */
    public BatteryPresence getBatteryPresence() {
        return BatteryPresence.getBatteryPresence(mFlags & 0x03);
    }

    /**
     * Returns the battery charge level
     *
     * @return BatteryIndicator
     */
    public BatteryIndicator getBatteryIndicator() {
        return BatteryIndicator.getBatteryIndicator((mFlags >> 2) & 0x03);
    }

    /**
     * Returns the battery charging state
     *
     * @return BatteryChargingState
     */
    public BatteryChargingState getBatteryChargingState() {
        return BatteryChargingState.getBatteryChargingState((mFlags >> 4) & 0x03);
    }

    /**
     * Returns the battery serviceability
     *
     * @return BatteryServiceability
     */
    public BatteryServiceability batteryServiceability() {
        return BatteryServiceability.getBatteryServiceability((mFlags >> 6) & 0x03);
    }

    /**
     * Battery presence values enumeration
     */
    public enum BatteryPresence {
        NOT_PRESENT(0b00),
        REMOVABLE(0b01),
        NOT_REMOVABLE(0b10),
        UNKNOWN(0b11);

        private final int flag;
        //Constructor to initialize the instance variable
        BatteryPresence(int flag) {
            this.flag = flag;
        }

        public int getFlag() {
            return this.flag;
        }

        public static BatteryPresence getBatteryPresence(int flag) {
            for (BatteryPresence bp : BatteryPresence.values()) {
                if (bp.flag == flag) return bp;
            }
            throw new IllegalArgumentException("BatteryPresence flag not found");
        }
    }


    /**
     * Battery indicator values enumeration
     */
    public enum BatteryIndicator {
        CRITICALLY_LOW(0b00),
        LOW(0b01),
        GOOD(0b10),
        UNKNOWN(0b11);

        private final int flag;
        //Constructor to initialize the instance variable
        BatteryIndicator(int flag) {
            this.flag = flag;
        }

        public int getFlag() {
            return this.flag;
        }

        public static BatteryIndicator getBatteryIndicator(int flag) {
            for (BatteryIndicator bi : BatteryIndicator.values()) {
                if (bi.flag == flag) return bi;
            }
            throw new IllegalArgumentException("BatteryIndicator flag not found");
        }
    }



    /**
     * Battery charging state values enumeration
     */
    public enum BatteryChargingState {
        NOT_CHARGEABLE(0b00),
        NOT_CHARGING(0b01),
        CHARGING(0b10),
        UNKNOWN(0b11);

        private final int flag;
        //Constructor to initialize the instance variable
        BatteryChargingState(int flag) {
            this.flag = flag;
        }

        public int getFlag() {
            return this.flag;
        }

        public static BatteryChargingState getBatteryChargingState(int flag) {
            for (BatteryChargingState bcs : BatteryChargingState.values()) {
                if (bcs.flag == flag) return bcs;
            }
            throw new IllegalArgumentException("BatteryChargingState flag not found");
        }
    }


    /**
     * Battery serviceability values enumeration
     */
    public enum BatteryServiceability {
        RESERVED(0b00),
        SERVICE_NOT_REQUIRED(0b01),
        SERVICE_REQUIRED(0b10),
        UNKNOWN(0b11);

        private final int flag;
        //Constructor to initialize the instance variable
        BatteryServiceability(int flag) {
            this.flag = flag;
        }

        public int getFlag() {
            return this.flag;
        }

        public static BatteryServiceability getBatteryServiceability(int flag) {
            for (BatteryServiceability bs : BatteryServiceability.values()) {
                if (bs.flag == flag) return bs;
            }
            throw new IllegalArgumentException("BatteryServiceability flag not found");
        }
    }

}
