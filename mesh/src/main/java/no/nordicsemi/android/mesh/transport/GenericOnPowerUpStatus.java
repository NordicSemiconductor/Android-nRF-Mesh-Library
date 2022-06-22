package no.nordicsemi.android.mesh.transport;

import android.os.Parcel;
import android.os.Parcelable;
import no.nordicsemi.android.mesh.logger.MeshLogger;

import androidx.annotation.NonNull;

import java.security.InvalidParameterException;

import no.nordicsemi.android.mesh.data.OnPowerUpState;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.ArrayUtils;
import no.nordicsemi.android.mesh.utils.BitReader;
import no.nordicsemi.android.mesh.utils.MeshAddress;

public class GenericOnPowerUpStatus extends ApplicationStatusMessage implements Parcelable {

    private static final String TAG = GenericOnPowerUpStatus.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.GENERIC_ON_POWER_UP_STATUS;
    private static final int GENERIC_ON_POWER_UP_STATUS_LENGTH = 8;

    private OnPowerUpState onPowerUpState = OnPowerUpState.BT_MESH_UNKNOWN;

    private static final Creator<GenericOnPowerUpStatus> CREATOR = new Creator<GenericOnPowerUpStatus>() {
        @Override
        public GenericOnPowerUpStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            //noinspection ConstantConditions
            return new GenericOnPowerUpStatus(message);
        }

        @Override
        public GenericOnPowerUpStatus[] newArray(int size) {
            return new GenericOnPowerUpStatus[size];
        }
    };

    /**
     * Generic on Power up Status is an unacknowledged message used to report the on power up state.
     *
     * @param message Message containing the on Power Up State.
     */
    public GenericOnPowerUpStatus(@NonNull AccessMessage message) {
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
        MeshLogger.verbose(TAG, "Received generic power up status from: " + MeshAddress.formatAddress(mMessage.getSrc(), true));

        if (mParameters.length == GENERIC_ON_POWER_UP_STATUS_LENGTH / 8) {
            BitReader bitReader = new BitReader(ArrayUtils.reverseArray(mParameters));
            try {
                onPowerUpState = OnPowerUpState.fromValue(bitReader.getBits(GENERIC_ON_POWER_UP_STATUS_LENGTH));
            } catch (InvalidParameterException e) {
                MeshLogger.verbose(TAG, "Couldn't parse on power up state.");
            }
            MeshLogger.verbose(TAG, "Generic on power up status has state: "+onPowerUpState);
        }
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    public OnPowerUpState getOnPowerUpState() {
        return onPowerUpState;
    }
}
