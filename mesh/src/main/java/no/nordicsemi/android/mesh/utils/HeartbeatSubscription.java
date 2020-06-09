package no.nordicsemi.android.mesh.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;

/**
 * Class containing the Heartbeat subscription configuration.
 */
@SuppressWarnings("unused")
public class HeartbeatSubscription extends Heartbeat implements Parcelable {


    @Expose
    @SerializedName("source")
    private final int src;
    @Expose
    @SerializedName("destination")
    private final int dst;
    @Expose
    @SerializedName("period")
    private final int periodLog;
    @Expose
    @SerializedName("count")
    private final int countLog;
    @Expose
    @SerializedName("minHops")
    private final int minHops;
    @Expose
    @SerializedName("maxHops")
    private final int maxHops;

    /**
     * Heartbeat subscription.
     *
     * @param src       Source address for Heartbeat messages.
     * @param dst       Destination address for Heartbeat messages.
     * @param periodLog Remaining period for processing Heartbeat messages.
     * @param countLog  Number of Heartbeat messages received.
     * @param minHops   Minimum hops when receiving Heartbeat messages.
     * @param maxHops   Maximum hops when receiving Heartbeat messages.
     */
    public HeartbeatSubscription(final int src, final int dst, final int periodLog, final int countLog, final int minHops, final int maxHops) {
        this.src = src;
        this.dst = dst;
        this.periodLog = periodLog;
        this.countLog = countLog;
        this.minHops = minHops;
        this.maxHops = maxHops;
    }

    private HeartbeatSubscription(Parcel in) {
        src = in.readInt();
        dst = in.readInt();
        periodLog = in.readInt();
        countLog = in.readInt();
        minHops = in.readInt();
        maxHops = in.readInt();
    }

    public static final Creator<HeartbeatSubscription> CREATOR = new Creator<HeartbeatSubscription>() {
        @Override
        public HeartbeatSubscription createFromParcel(Parcel in) {
            return new HeartbeatSubscription(in);
        }

        @Override
        public HeartbeatSubscription[] newArray(int size) {
            return new HeartbeatSubscription[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(src);
        dest.writeInt(dst);
        dest.writeInt(periodLog);
        dest.writeInt(countLog);
        dest.writeInt(minHops);
        dest.writeInt(maxHops);
    }

    @NonNull
    @Override
    public String toString() {
        return "Source address: " + Integer.toHexString(src) +
                "\nDestination address: " + Integer.toHexString(dst) +
                "\nPeriod Log: " + Integer.toHexString(periodLog) +
                "\nCount Log: " + Integer.toHexString(countLog) +
                "\nMin Hops: " + minHops +
                "\n Max Hops: " + maxHops;
    }

    /**
     * Returns the source address.
     */
    public int getSrc() {
        return src;
    }

    /**
     * Returns the destination address.
     */
    public int getDst() {
        return dst;
    }

    /**
     * Returns the period for processing.
     */
    public int getPeriodLog() {
        return periodLog;
    }

    /**
     * Returns the subscriptions count.
     */
    public int getCountLog() {
        return countLog;
    }

    /**
     * Returns the minimum number of hopes when receiving heartbeat messages.
     */
    public int getMinHops() {
        return minHops;
    }

    /**
     * Returns the maximum number of hopes when receiving heartbeat messages.
     */
    public int getMaxHops() {
        return maxHops;
    }
}
