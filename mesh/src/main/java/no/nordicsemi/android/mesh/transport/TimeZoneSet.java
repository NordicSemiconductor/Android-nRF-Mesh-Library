package no.nordicsemi.android.mesh.transport;

import no.nordicsemi.android.mesh.logger.MeshLogger;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.data.TimeZoneOffset;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.SecureUtils;

/**
 * To be used as a wrapper class for when creating the TimeZoneSet message.
 */
public final class TimeZoneSet extends ApplicationMessage {
    private static final String TAG = TimeZoneSet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.TIME_ZONE_SET;
    private static final int TIME_ZONE_SET_LENGTH = 6;
    private TimeZoneOffset newTimeZoneOffset;
    private long timeOfChange;

    /**
     * Constructs TimeZoneSet message.
     *
     * @param appKey            {@link ApplicationKey} key for this message
     * @param newTimeZoneOffset {@link TimeZoneOffset} new time zone offset
     * @param timeOfChange      point in time (TAI seconds) for change
     */
    public TimeZoneSet(
            @NonNull final ApplicationKey appKey,
            @NonNull final TimeZoneOffset newTimeZoneOffset,
            @NonNull final long timeOfChange) {
        super(appKey);
        this.newTimeZoneOffset = newTimeZoneOffset;
        this.timeOfChange = timeOfChange;
        assembleMessageParameters();
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        final ByteBuffer buffer = ByteBuffer.allocate(TIME_ZONE_SET_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        MeshLogger.verbose(TAG, "Creating message");
        MeshLogger.verbose(TAG, newTimeZoneOffset.toString());
        MeshLogger.verbose(TAG, "time of change:" + timeOfChange);
        buffer.put(newTimeZoneOffset.getEncodedValue());
        buffer.putInt((int) timeOfChange);
        buffer.put((byte) (timeOfChange >> 32));
        mParameters = buffer.array();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    /**
     * Returns the New Time Zone Offset
     *
     * @return a TimeZoneOffset instance
     */
    @NonNull
    public TimeZoneOffset getNewTimeZoneOffset() {
        return newTimeZoneOffset;
    }

    /**
     * Returns the point in time (using the TAI Seconds format) when the New Time Zone Offset shall be applied.
     *
     * @return TAI in seconds
     */
    public long getTimeOfChange() {
        return timeOfChange;
    }
}
