package no.nordicsemi.android.mesh.transport;

import android.os.Parcel;
import android.os.Parcelable;
import no.nordicsemi.android.mesh.logger.MeshLogger;

import androidx.annotation.NonNull;

import java.security.InvalidParameterException;

import no.nordicsemi.android.mesh.data.ScheduleEntry;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.ArrayUtils;
import no.nordicsemi.android.mesh.utils.BitReader;
import no.nordicsemi.android.mesh.utils.MeshAddress;

public class SchedulerActionStatus extends ApplicationStatusMessage implements Parcelable {

    private static final String TAG = SchedulerActionStatus.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.SCHEDULER_ACTION_STATUS;
    private static final int SCHEDULER_ACTION_STATUS_LENGTH = 10;

    private int index;
    private ScheduleEntry entry;

    private static final Creator<SchedulerActionStatus> CREATOR = new Creator<SchedulerActionStatus>() {
        @Override
        public SchedulerActionStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            //noinspection ConstantConditions
            return new SchedulerActionStatus(message);
        }

        @Override
        public SchedulerActionStatus[] newArray(int size) {
            return new SchedulerActionStatus[size];
        }
    };

    /**
     * Scheduler Action Status is an unacknowledged message used to report the entry of the Schedule Register state of an element (see Section 5.1.4.2), identified by the Index field.
     *
     * @param message Message containing the index and schedule register.
     */
    public SchedulerActionStatus(@NonNull AccessMessage message) {
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
        MeshLogger.verbose(TAG, "Received scheduler action status from: " + MeshAddress.formatAddress(mMessage.getSrc(), true));

        if (mParameters.length == SCHEDULER_ACTION_STATUS_LENGTH) {
            BitReader bitReader = new BitReader(ArrayUtils.reverseArray(mParameters));
            try {
                entry = new ScheduleEntry(bitReader);
            } catch (InvalidParameterException e) {
                MeshLogger.verbose(TAG, "Couldn't parse ScheduleEntry.");
            }
            index = bitReader.getBits(4);
            MeshLogger.verbose(TAG, "Scheduler action status has index: "+index);
            MeshLogger.verbose(TAG, "Scheduler action status has entry: "+entry.toString());
        }

    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    /**
     * Enumerates (selects) a Schedule Register entry
     */
    public int getIndex() {
        return index;
    }

    /**
     * Bit field defining an entry in the Schedule Register
     */
    public ScheduleEntry getEntry() {
        return entry;
    }
}
