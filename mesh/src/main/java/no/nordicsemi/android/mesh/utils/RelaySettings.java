package no.nordicsemi.android.mesh.utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;

/**
 * Class containing Relay Settings values in a {@link ProvisionedMeshNode}
 */
@SuppressWarnings({"WeakerAccess"})
public class RelaySettings implements Parcelable {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({RELAY_FEATURE_DISABLED, RELAY_FEATURE_ENABLED, RELAY_FEATURE_NOT_SUPPORTED})
    public @interface RelayState {
    }

    public static final int RELAY_FEATURE_DISABLED = 0x00;   //The node support Relay feature that is disabled
    public static final int RELAY_FEATURE_ENABLED = 0x01;    //The node supports Relay feature that is enabled
    public static final int RELAY_FEATURE_NOT_SUPPORTED = 0x02;  //Relay feature is not supported

    private final int relayTransmitCount;
    private final int relayIntervalSteps;

    /**
     * Constructs {@link RelaySettings}
     *
     * @param relayTransmitCount Number of retransmissions on advertising bearer for each Network PDU relayed by the node
     * @param relayIntervalSteps Number of 10-millisecond steps between retransmissions
     */
    public RelaySettings(final int relayTransmitCount, final int relayIntervalSteps) {
        this.relayTransmitCount = relayTransmitCount;
        this.relayIntervalSteps = relayIntervalSteps;
    }

    protected RelaySettings(Parcel in) {
        relayTransmitCount = in.readInt();
        relayIntervalSteps = in.readInt();
    }

    /**
     * Returns if relaying is supported by the node
     *
     * @param relay {@link RelayState}
     * @return true if supported and false otherwise
     */
    public static boolean isRelaySupported(@RelayState final int relay) {
        switch (relay) {
            case RELAY_FEATURE_DISABLED:
            case RELAY_FEATURE_ENABLED:
                return true;
            case RELAY_FEATURE_NOT_SUPPORTED:
            default:
                return false;
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(relayTransmitCount);
        dest.writeInt(relayIntervalSteps);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RelaySettings> CREATOR = new Creator<RelaySettings>() {
        @Override
        public RelaySettings createFromParcel(Parcel in) {
            return new RelaySettings(in);
        }

        @Override
        public RelaySettings[] newArray(int size) {
            return new RelaySettings[size];
        }
    };

    /**
     * Returns the number of retransmissions on advertising bearer for each Network PDU relayed by the node
     */
    public int getRelayTransmitCount() {
        return relayTransmitCount;
    }

    /**
     * Returns the number of total retransmissions.
     */
    public int getTotalTransmissionsCount() {
        return relayTransmitCount + 1;
    }

    /**
     * Returns the number of 10-millisecond steps between retransmissions
     */
    public int getRelayIntervalSteps() {
        return relayIntervalSteps;
    }

    /**
     * Returns the interval interval set by the relayState settings
     */
    public int getRetransmissionIntervals() {
        return (relayIntervalSteps + 1) * 10;
    }


    /**
     * Decodes the Relay Retransmit Interval as steps
     *
     * @param interval Interval between 10-320 ms
     * @return the interval as steps
     * @throws IllegalArgumentException if the Relay Retransmit Interval is not in range of 10-320 ms
     */
    public static int decodeRelayRetransmitInterval(final int interval) {
        if ((interval >= 10 && interval <= 320) && (interval % 10 != 0))
            throw new IllegalArgumentException("Relay Retransmit Interval must be in range of 10-320 ms.");
        return (interval / 10) - 1;
    }
}
