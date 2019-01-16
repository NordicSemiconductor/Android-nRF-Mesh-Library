package no.nordicsemi.android.meshprovisioner.utils;

import android.os.Parcel;
import android.os.Parcelable;

import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;

/**
 * Class containing Network Transmit values in a {@link ProvisionedMeshNode}
 */
@SuppressWarnings("WeakerAccess")
public class NetworkTransmitSettings implements Parcelable {

    private final int networkTransmitCount;
    private final int networkIntervalSteps;

    /**
     * Constructs {@link NetworkTransmitSettings}
     * @param networkTransmitCount Number of transmissions for each Network PDU originating from the node
     * @param networkIntervalSteps Number of 10-millisecond steps between transmissions
     */
    public NetworkTransmitSettings(final int networkTransmitCount, final int networkIntervalSteps){
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
    public int getTransmissionCount(){
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
    public int getNetworkTransmissionInterval(){
        return (networkIntervalSteps + 1) * 10;
    }
}
