package no.nordicsemi.android.mesh.transport;

import android.os.Parcel;
import android.os.Parcelable;
import no.nordicsemi.android.mesh.logger.MeshLogger;

import androidx.annotation.NonNull;

import java.security.InvalidParameterException;

import no.nordicsemi.android.mesh.data.GenericTransitionTime;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.ArrayUtils;
import no.nordicsemi.android.mesh.utils.BitReader;
import no.nordicsemi.android.mesh.utils.MeshAddress;

public class GenericDefaultTransitionTimeStatus extends ApplicationStatusMessage implements Parcelable {

    private static final String TAG = GenericDefaultTransitionTimeStatus.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.GENERIC_DEFAULT_TRANSITION_TIME_STATUS;
    private static final int GENERIC_DEFAULT_TRANSITION_TIME_STATUS_LENGTH = 1;

    private GenericTransitionTime genericTransitionTime;

    private static final Creator<GenericDefaultTransitionTimeStatus> CREATOR = new Creator<GenericDefaultTransitionTimeStatus>() {
        @Override
        public GenericDefaultTransitionTimeStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            //noinspection ConstantConditions
            return new GenericDefaultTransitionTimeStatus(message);
        }

        @Override
        public GenericDefaultTransitionTimeStatus[] newArray(int size) {
            return new GenericDefaultTransitionTimeStatus[size];
        }
    };

    /**
     * Generic Default Transition Time Status is an unacknowledged message used to report the Generic Default Transition Time state of an element (see Section 3.1.3).
     *
     * @param message Message containing the [TransitionTime]
     */
    public GenericDefaultTransitionTimeStatus(@NonNull AccessMessage message) {
        super(message);
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        final AccessMessage message = (AccessMessage) mMessage;
        parcel.writeParcelable(message, flags);
    }

    @Override
    void parseStatusParameters() {
        MeshLogger.verbose(TAG, "Received default transition time status from: " + MeshAddress.formatAddress(mMessage.getSrc(), true));
        if (mParameters.length == GENERIC_DEFAULT_TRANSITION_TIME_STATUS_LENGTH) {
            BitReader bitReader = new BitReader(ArrayUtils.reverseArray(mParameters));
            try {
                genericTransitionTime = new GenericTransitionTime(bitReader.getBits(GENERIC_DEFAULT_TRANSITION_TIME_STATUS_LENGTH));
                MeshLogger.verbose(TAG, "Parsed Transition time status: "+ genericTransitionTime.toString());
            } catch (InvalidParameterException e) {
                MeshLogger.verbose(TAG, "Couldn't parse TransitionTime.");
            }
        }
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    public GenericTransitionTime getGenericTransitionTime() {
        return genericTransitionTime;
    }
}
