package no.nordicsemi.android.mesh.transport;

import android.os.Parcel;
import android.os.Parcelable;
import no.nordicsemi.android.mesh.logger.MeshLogger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;

public class TimeRoleStatus extends ApplicationStatusMessage implements Parcelable {

    private static final String TAG = TimeRoleStatus.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.TIME_ROLE_STATUS;
    private static final int TIME_ROLE_STATUS_LENGTH = 1;

    private static final Creator<TimeRoleStatus> CREATOR = new Creator<TimeRoleStatus>() {
        @Override
        public TimeRoleStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            //noinspection ConstantConditions
            return new TimeRoleStatus(message);
        }

        @Override
        public TimeRoleStatus[] newArray(int size) {
            return new TimeRoleStatus[size];
        }
    };

    private Byte timeRole;

    public TimeRoleStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {
        if (mParameters != null && mParameters.length == TIME_ROLE_STATUS_LENGTH) {
            timeRole = mParameters[0];
            MeshLogger.verbose(TAG, "Time Role status has timeRole: " + timeRole);
        } else {
            MeshLogger.verbose(TAG, "Time Role status has no values");
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
    public Byte getTimeRole() {
        return timeRole;
    }

    /**
     * Gets the time role as a human-readable string
     * @return String representation of time role
     */
    @Nullable
    public String getTimeRoleDescription() {
        if (timeRole == null) {
            return null;
        }
        switch (timeRole) {
            case 0x00:
                return "None";
            case 0x01:
                return "Mesh Time Authority";
            case 0x02:
                return "Mesh Time Relay";
            case 0x03:
                return "Mesh Time Client";
            default:
                return "Unknown (" + String.format("0x%02X", timeRole) + ")";
        }
    }
}