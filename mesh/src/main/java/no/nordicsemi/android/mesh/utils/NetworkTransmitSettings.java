package no.nordicsemi.android.mesh.utils;

import android.os.Parcel;
import android.os.Parcelable;

import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;

/**
 * Class containing Network Transmit values in a {@link ProvisionedMeshNode}
 */
@SuppressWarnings("WeakerAccess")
public class NetworkTransmitSettings implements Parcelable {

    private final int networkTransmitCount;
    private final int networkIntervalSteps;

    /**
     * Constructs {@link NetworkTransmitSettings}
     *
     * @param networkTransmitCount Number of transmissions for each Network PDU originating from the node
     * @param networkIntervalSteps Number of 10-millisecond steps between transmissions
     */
    public NetworkTransmitSettings(final int networkTransmitCount, final int networkIntervalSteps) {
        if (networkTransmitCount < 1 || networkTransmitCount > 8) {
            throw new IllegalArgumentException("Network Transmit count must be in range 1-8.");
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
     * Returns the number of transmissions.
     */
    public int getTransmissionCount() {
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
     * @param interval Interval between 10-320 ms with a step of 10 ms
     * @return the interval as steps
     * @throws IllegalArgumentException if the Network Transmission Interval is not 10-320 ms with a step of 10 ms
     */
    public static int decodeNetworkTransmissionInterval(final int interval) {
        if ((interval >= 10 && interval <= 320) && (interval % 10 != 0))
            throw new IllegalArgumentException("Network Transmission Interval must be 10-320 ms with a step of 10 ms");
        return (interval / 10) - 1;
    }
}
