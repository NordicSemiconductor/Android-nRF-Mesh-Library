package no.nordicsemi.android.mesh.transport;


import android.os.Parcel;
import android.os.Parcelable;
import no.nordicsemi.android.mesh.logger.MeshLogger;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.mesh.data.GlobalAltitude;
import no.nordicsemi.android.mesh.data.GlobalLatitude;
import no.nordicsemi.android.mesh.data.GlobalLongitude;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshAddress;

/**
 * To be used as a wrapper class for when creating the GenericLocationGlobalStatus Message.
 */
public final class GenericLocationGlobalStatus extends ApplicationStatusMessage implements Parcelable {
    private static final String TAG = GenericLocationGlobalStatus.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.GENERIC_LOCATION_GLOBAL_STATUS;
    private static final int GENERIC_LOCATION_GLOBAL_STATUS_LENGTH = 10;
    private GlobalLatitude latitude = GlobalLatitude.notConfigured();
    private GlobalLongitude longitude = GlobalLongitude.notConfigured();
    private GlobalAltitude altitude = GlobalAltitude.notConfigured();

    private static final Parcelable.Creator<GenericLocationGlobalStatus> CREATOR = new Parcelable.Creator<GenericLocationGlobalStatus>() {
        @Override
        public GenericLocationGlobalStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            return new GenericLocationGlobalStatus(message);
        }

        @Override
        public GenericLocationGlobalStatus[] newArray(int size) {
            return new GenericLocationGlobalStatus[size];
        }
    };

    /**
     * Constructs the GenericLocationGlobalStatus message.
     *
     * @param message Access Message
     */
    public GenericLocationGlobalStatus(@NonNull final AccessMessage message) {
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
        MeshLogger.verbose(TAG, "Received generic location global status from: " + MeshAddress.formatAddress(mMessage.getSrc(), true));
        if (mParameters.length == GENERIC_LOCATION_GLOBAL_STATUS_LENGTH) {
            final ByteBuffer buffer = ByteBuffer.wrap(mParameters).order(ByteOrder.LITTLE_ENDIAN);
            latitude = GlobalLatitude.of(buffer.getInt());
            longitude = GlobalLongitude.of(buffer.getInt());
            altitude = GlobalAltitude.of(buffer.getShort());
            MeshLogger.verbose(TAG, latitude.toString());
            MeshLogger.verbose(TAG, longitude.toString());
            MeshLogger.verbose(TAG, altitude.toString());
        }
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    /**
     * Returns the Global Latitude
     *
     * @return a GlobalLatitude instance
     */
    @NonNull
    public GlobalLatitude getLatitude() {
        return latitude;
    }

    /**
     * Returns the Global Longitude
     *
     * @return a GlobalLongitude instance
     */
    @NonNull
    public GlobalLongitude getLongitude() {
        return longitude;
    }

    /**
     * Returns the Global Altitude
     *
     * @return a GlobalAltitude instance
     */
    @NonNull
    public GlobalAltitude getAltitude() {
        return altitude;
    }
}
