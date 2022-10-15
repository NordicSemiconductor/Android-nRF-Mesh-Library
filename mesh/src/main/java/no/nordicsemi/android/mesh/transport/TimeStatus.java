package no.nordicsemi.android.mesh.transport;

import android.os.Parcel;
import android.os.Parcelable;
import no.nordicsemi.android.mesh.logger.MeshLogger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.ArrayUtils;
import no.nordicsemi.android.mesh.utils.BitReader;

public class TimeStatus extends ApplicationStatusMessage implements Parcelable {

    private static final String TAG = TimeStatus.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.TIME_STATUS;

    private static final Creator<TimeStatus> CREATOR = new Creator<TimeStatus>() {
        @Override
        public TimeStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            //noinspection ConstantConditions
            return new TimeStatus(message);
        }

        @Override
        public TimeStatus[] newArray(int size) {
            return new TimeStatus[size];
        }
    };

    private Integer taiSeconds;
    private Byte subSecond;
    private Byte uncertainty;
    private Boolean timeAuthority;
    private Short utcDelta;
    private Byte timeZoneOffset;


    public TimeStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }


    @Override
    void parseStatusParameters() {
        BitReader bitReader = new BitReader(ArrayUtils.reverseArray(mParameters));
        if (bitReader.bitsLeft() == TIME_BIT_SIZE) {
            timeZoneOffset = (byte) (bitReader.getBits(TIME_ZONE_OFFSET_BIT_SIZE) - TIME_ZONE_START_RANGE);
            utcDelta = (short) (bitReader.getBits(UTC_DELTA_BIT_SIZE) - UTC_DELTA_START_RANGE);
            timeAuthority = bitReader.getBits(TIME_AUTHORITY_BIT_SIZE) == 1;
            uncertainty = (byte) bitReader.getBits(UNCERTAINTY_BIT_SIZE);
            subSecond = (byte) bitReader.getBits(SUB_SECOND_BIT_SIZE);
            taiSeconds = bitReader.getBits(TAI_SECONDS_BIT_SIZE);
            MeshLogger.verbose(TAG, "Time status has taiSeconds: "+taiSeconds);
            MeshLogger.verbose(TAG, "Time status has subSecond: "+subSecond);
            MeshLogger.verbose(TAG, "Time status has uncertainty: "+uncertainty);
            MeshLogger.verbose(TAG, "Time status has timeAuthority: "+timeAuthority);
            MeshLogger.verbose(TAG, "Time status has utcDelta: "+utcDelta);
            MeshLogger.verbose(TAG, "Time status has timeZoneOffset: "+timeZoneOffset);
        } else {
            MeshLogger.verbose(TAG, "Time status has no values");
        }
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
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

    @Nullable
    public Integer getTaiSeconds() {
        return taiSeconds;
    }

    @Nullable
    public Byte getSubSecond() {
        return subSecond;
    }

    @Nullable
    public Byte getUncertainty() {
        return uncertainty;
    }

    @Nullable
    public Boolean isTimeAuthority() {
        return timeAuthority;
    }

    @Nullable
    public Short getUtcDelta() {
        return utcDelta;
    }

    @Nullable
    public Byte getTimeZoneOffset() {
        return timeZoneOffset;
    }

    static final int TAI_SECONDS_BIT_SIZE = 40;
    static final int SUB_SECOND_BIT_SIZE = 8;
    static final int UNCERTAINTY_BIT_SIZE = 8;
    static final int TIME_AUTHORITY_BIT_SIZE = 1;
    static final int UTC_DELTA_BIT_SIZE = 15;
    static final int TIME_ZONE_OFFSET_BIT_SIZE = 8;
    static final int TIME_BIT_SIZE = TAI_SECONDS_BIT_SIZE + SUB_SECOND_BIT_SIZE + UNCERTAINTY_BIT_SIZE + TIME_AUTHORITY_BIT_SIZE
            + UTC_DELTA_BIT_SIZE + TIME_ZONE_OFFSET_BIT_SIZE;
    static final int TIME_ZONE_START_RANGE = 0x40;
    static final int UTC_DELTA_START_RANGE = 0xFF;
}
