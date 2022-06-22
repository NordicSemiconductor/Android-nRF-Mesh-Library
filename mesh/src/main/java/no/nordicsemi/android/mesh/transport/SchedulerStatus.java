package no.nordicsemi.android.mesh.transport;

import android.os.Parcel;
import android.os.Parcelable;
import no.nordicsemi.android.mesh.logger.MeshLogger;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.mesh.data.ScheduleEntry;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshAddress;

public class SchedulerStatus extends ApplicationStatusMessage implements Parcelable {

    private static final String TAG = SchedulerStatus.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.SCHEDULER_STATUS;
    private int schedules = ScheduleEntry.Action.NoAction.getValue();

    private static final Creator<SchedulerStatus> CREATOR = new Creator<SchedulerStatus>() {
        @Override
        public SchedulerStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            //noinspection ConstantConditions
            return new SchedulerStatus(message);
        }

        @Override
        public SchedulerStatus[] newArray(int size) {
            return new SchedulerStatus[size];
        }
    };

    /**
     * Scheduler Status is an unacknowledged message used to report the current Schedule Register state of an element (see Mesh Model Spec. v1.0.1 Section 5.1.4.2).
     *
     * @param message Access Message containing tht schedules
     */
    public SchedulerStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {
        MeshLogger.verbose(TAG, "Received scheduler status from: " + MeshAddress.formatAddress(mMessage.getSrc(), true));
        final ByteBuffer buffer = ByteBuffer.wrap(mParameters).order(ByteOrder.LITTLE_ENDIAN);
        schedules = buffer.get();
        MeshLogger.verbose(TAG, "Schedules action: " + schedules);
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    /**
     * Bit field indicating defined Actions in the `Schedule Register`
     * Each bit of the Schedules field set to 1 identifies a corresponding entry of the Schedule Register
     */
    public int getSchedules() {
        return schedules;
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
}
