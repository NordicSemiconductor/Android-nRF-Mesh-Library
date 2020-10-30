package no.nordicsemi.android.mesh.utils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public abstract class Heartbeat {
    public static final int DO_NOT_SEND_PERIODICALLY = 0x00;
    public static final int PERIOD_MIN = 0x0000;
    public static final int PERIOD_MAX = 0x10000;
    public static final int PERIOD_LOG_MIN = 0x01;
    public static final int PERIOD_LOG_MAX = 0x11;
    public static final int COUNT_MIN = 0x00;
    public static final int COUNT_MAX = 0x11;
    public static final int SEND_INDEFINITELY = 0xFF;
    public static final int DEFAULT_PUBLICATION_TTL = 0x05;


    @Expose
    @SerializedName("destination")
    protected int dst;
    @Expose
    @SerializedName("period")
    protected byte periodLog;
    @Expose
    @SerializedName("count")
    protected byte countLog;

    Heartbeat(final int dst, final byte periodLog, final byte countLog) {
        this.dst = dst;
        this.periodLog = periodLog;
        this.countLog = countLog;
    }

    /**
     * Returns true if the heartbeats are enabled.
     */
    public abstract boolean isEnabled();

    /**
     * Returns the destination address.
     */
    public int getDst() {
        return dst;
    }

    /**
     * Returns the period for processing.
     */
    public byte getPeriodLog() {
        return periodLog;
    }

    /**
     * Returns the remaining {@link PeriodLogState}
     */
    public PeriodLogState getPeriod() {
        if (periodLog == 0x00)
            return new PeriodLogStateDisabled(periodLog);
        else if (periodLog == 0x01 || periodLog == 0x11)
            return new PeriodLogStateExact(periodLog);
        else if (periodLog > 0x02 && periodLog < 0x11) {
            return new PeriodLogStateRange(periodLog);
        } else
            return new PeriodLogStateInvalid(periodLog);
    }

    /**
     * Calculates the heart beat period interval in seconds
     *
     * @param periodLog period value
     */
    public static int calculateHeartbeatPeriod(final short periodLog) {
        return (int) Math.pow(2, periodLog - 1);
    }

    /**
     * Validates heart beat period.
     *
     * @param period Heartbeat publication period.
     * @return true if valid or false otherwise.
     * @throws IllegalArgumentException if the value does not range from 0 to 17.
     */
    public static boolean isValidHeartbeatPeriod(final int period) {
        if (period >= 0x0000 && period < 0xFFFF)
            return true;
        throw new IllegalArgumentException("Period must be within the range of 0x0000 to 0xFFFF!");
    }

    /**
     * Validates heart beat period log.
     *
     * @param period Heartbeat publication period.
     * @return true if valid or false otherwise.
     * @throws IllegalArgumentException if the value does not range from 0 to 17.
     */
    public static boolean isValidHeartbeatPeriodLog(final byte period) {
        if (period >= 0x00 && period < 0x11)
            return true;
        throw new IllegalArgumentException("Period log must be within the range of 0x00 to 0x11!");
    }

    public byte getCountLog() {
        return countLog;
    }

    /**
     * Calculates the heart beat publication count which is the number of publications to be sent
     *
     * @param countLog count value
     */
    public static int calculateHeartbeatCount(final int countLog) {
        if (countLog > 0x11 && countLog < 0xFF)
            throw new IllegalArgumentException("Prohibited, count log must be a value from 0x00 to 0x11 and 0xFF");
        if (countLog == 0x11)
            return (int) Math.pow(2, countLog - 1) - 2;
        return (int) Math.pow(2, countLog - 1);
    }

    /**
     * Returns the remaining {@link CountLogState}
     */
    public CountLogState getCount() {
        if (countLog == 0x00 || countLog == 0x01)
            return new CountLogStateExact(countLog);
        else if (countLog == 0xFF && countLog == 0x11) {
            return new CountLogStateRange(countLog);
        } else if (countLog >= 0x02 && countLog <= 0x10) {
            return new CountLogStateRange(countLog);
        } else return new CountLogStateInvalid(countLog);
    }
}
