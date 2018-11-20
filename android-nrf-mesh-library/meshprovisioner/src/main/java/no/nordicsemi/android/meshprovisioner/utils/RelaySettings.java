package no.nordicsemi.android.meshprovisioner.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;

/**
 * Class containing Relay Settings values in a {@link ProvisionedMeshNode}
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class RelaySettings implements Parcelable {
    private static final int RELAY_DISABLED = 0x00; //The node support Relay feature that is disabled
    private static final int RELAY_ENABLED = 0x01; //The node supports Relay feature that is enabled
    private static final int RELAY_NOT_SUPPORTED = 0x02; //Relay feature is not supported

    private final int relayTransmitCount;
    private final int relayIntervalSteps;

    /**
     * Constructs {@link RelaySettings}
     * @param relayTransmitCount Number of retransmissions on advertising bearer for each Network PDU relayed by the node
     * @param relayIntervalSteps Number of 10-millisecond steps between retransmissions
     */
    public RelaySettings(final int relayTransmitCount, final int relayIntervalSteps){
        this.relayTransmitCount = relayTransmitCount;
        this.relayIntervalSteps = relayIntervalSteps;
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
            return new RelaySettings(in.readInt(), in.readInt());
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
     * Returns the number of 10-millisecond steps between retransmissions
     */
    public int getRelayIntervalSteps() {
        return relayIntervalSteps;
    }

    /**
     * Returns the interval interval set by the relayState settings
     */
    public int getRetransmissionIntervals(){
        return (relayIntervalSteps + 1) * 10;
    }

}
