package no.nordicsemi.android.mesh.utils;

import android.os.Parcel;
import android.os.Parcelable;

import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;

/**
 * Class containing Network Transmit values in a {@link ProvisionedMeshNode}
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class NetworkTransmitSettings implements Parcelable {

    public static final int MIN_TRANSMIT_COUNT = 0b000;
    public static final int MAX_TRANSMIT_COUNT = 0b111;
    public static final int MIN_TRANSMISSIONS = 1;
    public static final int MAX_TRANSMISSIONS = 8;
    public static final int MIN_TRANSMISSION_INTERVAL_STEPS = 0b00000;
    public static final int MAX_TRANSMISSION_INTERVAL_STEPS = 0b11111;
    public static final int MIN_TRANSMISSION_INTERVAL = 10;
    public static final int MAX_TRANSMISSION_INTERVAL = 320;
    private final int networkTransmitCount;
    private final int networkIntervalSteps;

    /**
     * Constructs {@link NetworkTransmitSettings}
     *
     * @param networkTransmitCount Number of transmissions for each Network PDU originating from the node.
     * @param networkIntervalSteps Number of 10-millisecond steps between transmissions.
     */
    public NetworkTransmitSettings(final int networkTransmitCount, final int networkIntervalSteps) {
        if (networkTransmitCount < MIN_TRANSMIT_COUNT || networkTransmitCount > MAX_TRANSMIT_COUNT) {
            throw new IllegalArgumentException("Network Transmit count must be in range "
                    +  MIN_TRANSMIT_COUNT + "-" + MAX_TRANSMIT_COUNT + ".");
        }

        if (networkIntervalSteps < MIN_TRANSMISSION_INTERVAL_STEPS || networkIntervalSteps > MAX_TRANSMISSION_INTERVAL_STEPS) {
            throw new IllegalArgumentException("Network Transmit Interval steps must be in range "
                    +  MIN_TRANSMISSION_INTERVAL_STEPS + "-" + MAX_TRANSMISSION_INTERVAL_STEPS + ".");
        }
        this.networkTransmitCount = networkTransmitCount;
        this.networkIntervalSteps = networkIntervalSteps;
    }

    protected NetworkTransmitSettings(Parcel in) {
        networkTransmitCount = in.readInt();
        networkIntervalSteps = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(networkTransmitCount);
        dest.writeInt(networkIntervalSteps);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NetworkTransmitSettings> CREATOR = new Creator<NetworkTransmitSettings>() {
        @Override
        public NetworkTransmitSettings createFromParcel(Parcel in) {
            return new NetworkTransmitSettings(in);
        }

        @Override
        public NetworkTransmitSettings[] newArray(int size) {
            return new NetworkTransmitSettings[size];
        }
    };

    /**
     * Returns the network transmit count
     */
    public int getNetworkTransmitCount() {
        return networkTransmitCount;
    }

    /**
     * Returns the Transmit Count.
     */
    public int getTransmissionCount() {
        return networkTransmitCount;
    }

    /**
     * Returns the number of transmissions i.e. Transmit count + 1.
     */
    public int getTransmissions() {
        return networkTransmitCount + 1;
    }

    /**
     * Returns the network interval steps
     */
    public int getNetworkIntervalSteps() {
        return networkIntervalSteps;
    }

    /**
     * Returns the Network transmission interval.
     */
    public int getNetworkTransmissionInterval() {
        return (networkIntervalSteps + 1) * 10;
    }

    /**
     * Decodes the Network Transmission Interval steps
     *
     * @param interval Interval between 10-320 ms with a step of 10 ms
     * @return the interval as steps
     * @throws IllegalArgumentException if the Network Transmission Interval is not 10-320 ms with a
     *                                  step of 10 ms
     */
    public static int decodeNetworkTransmissionInterval(final int interval) {
        if ((interval >= 10 && interval <= 320) && (interval % 10 != 0))
            throw new IllegalArgumentException("Network Transmission Interval must be 10-320 ms with a step of 10 ms");
        return (interval / 10) - 1;
    }

    /**
     * Returns the Network transmission interval for a given number of Interval steps.
     * The transmission interval is calculated using the formula:
     * transmission interval = (Network Retransmit Interval Steps + 1) * 10
     *
     * @param networkIntervalSteps 5-bit value representing the number of 10 millisecond steps that
     *                             controls the interval between message transmissions of Network
     *                             PDUs originating from the node.
     * @return Network transmission interval.
     */
    public static int getNetworkTransmissionInterval(final int networkIntervalSteps) {
        return (networkIntervalSteps + 1) * 10;
    }

    /**
     * Returns the number of Transmissions. Netwokr transmit count can be calculated using the
     * following formula.
     * Network transmit Count =  transmissions - 1.
     *
     * @param transmissions Number of transmissions.
     * @return Network Transmit Count.
     */
    public static int getTransmissionCount(final int transmissions) {
        if (transmissions < MIN_TRANSMISSIONS || transmissions > MAX_TRANSMISSIONS) {
            throw new IllegalArgumentException("Network Transmissions must be in range 1-8.");
        }
        return transmissions - 1;
    }
}
