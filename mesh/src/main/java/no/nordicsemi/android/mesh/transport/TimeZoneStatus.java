package no.nordicsemi.android.mesh.transport;

import android.os.Parcel;
import android.os.Parcelable;
import no.nordicsemi.android.mesh.logger.MeshLogger;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.mesh.data.TimeZoneOffset;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshAddress;

/**
 * To be used as a wrapper class for when creating the TimeZoneStatus Message.
 */
public final class TimeZoneStatus extends ApplicationStatusMessage implements Parcelable {
    private static final String TAG = TimeZoneStatus.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.TIME_ZONE_STATUS;
    private static final int TIME_ZONE_STATUS_LENGTH = 7;
    private TimeZoneOffset currentTimeZoneOffset = TimeZoneOffset.of((byte) 0x40);
    private TimeZoneOffset newTimeZoneOffset = TimeZoneOffset.of((byte) 0x40);
    private long timeOfChange = 0x0;

    private static final Parcelable.Creator<TimeZoneStatus> CREATOR = new Parcelable.Creator<TimeZoneStatus>() {
        @Override
        public TimeZoneStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            return new TimeZoneStatus(message);
        }

        @Override
        public TimeZoneStatus[] newArray(int size) {
            return new TimeZoneStatus[size];
        }
    };

    /**
     * Constructs the TimeZoneStatus message.
     *
     * @param message Access Message
     */
    public TimeZoneStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        final AccessMessage message = (AccessMessage) mMessage;
        dest.writeParcelable(message, flags);
    }

    @Override
    void parseStatusParameters() {
        MeshLogger.verbose(TAG, "Received time zone status from: " + MeshAddress.formatAddress(mMessage.getSrc(), true));
        if (mParameters.length == TIME_ZONE_STATUS_LENGTH) {
            final ByteBuffer buffer = ByteBuffer.wrap(mParameters).order(ByteOrder.LITTLE_ENDIAN);
            currentTimeZoneOffset = TimeZoneOffset.of(buffer.get());
            newTimeZoneOffset = TimeZoneOffset.of(buffer.get());
            timeOfChange = ((long) buffer.getInt() & 0xFFFFFFFFl) | (((long) buffer.get() & 0xFF) << 32);
            MeshLogger.verbose(TAG, "current " + currentTimeZoneOffset.toString());
            MeshLogger.verbose(TAG, "new " + newTimeZoneOffset.toString());
            MeshLogger.verbose(TAG, "time of change: " + timeOfChange);
        }
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    /**
     * Returns the Current Time Zone Offset
     *
     * @return a TimeZoneOffset instance
     */
    @NonNull
    public TimeZoneOffset getCurrentTimeZoneOffset() {
        return currentTimeZoneOffset;
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
