package no.nordicsemi.android.mesh.transport;


import no.nordicsemi.android.mesh.logger.MeshLogger;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.data.GlobalAltitude;
import no.nordicsemi.android.mesh.data.GlobalLatitude;
import no.nordicsemi.android.mesh.data.GlobalLongitude;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.SecureUtils;

/**
 * To be used as a wrapper class for when creating the GenericLocationGlobalSetUnacknowledged message.
 */
public final class GenericLocationGlobalSetUnacknowledged extends ApplicationMessage {
    private static final String TAG = GenericLocationGlobalSetUnacknowledged.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.GENERIC_LOCATION_GLOBAL_SET_UNACKNOWLEDGED;
    private static final int GENERIC_LOCATION_GLOBAL_SET_LENGTH = 10;
    private GlobalLatitude latitude;
    private GlobalLongitude longitude;
    private GlobalAltitude altitude;

    /**
     * Constructs GenericLocationGlobalSetUnacknowledged message.
     *
     * @param appKey    {@link ApplicationKey} key for this message
     * @param latitude  {@link GlobalLatitude} global latitude
     * @param longitude {@link GlobalLongitude} global longitude
     * @param altitude  {@link GlobalAltitude} global altitude
     */
    public GenericLocationGlobalSetUnacknowledged(
            @NonNull final ApplicationKey appKey,
            @NonNull final GlobalLatitude latitude,
            @NonNull final GlobalLongitude longitude,
            @NonNull final GlobalAltitude altitude) {
        super(appKey);
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        assembleMessageParameters();
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        final ByteBuffer buffer = ByteBuffer.allocate(GENERIC_LOCATION_GLOBAL_SET_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        MeshLogger.verbose(TAG, "Creating message");
        MeshLogger.verbose(TAG, latitude.toString());
        MeshLogger.verbose(TAG, longitude.toString());
        MeshLogger.verbose(TAG, altitude.toString());
        buffer.putInt(latitude.getEncodedValue());
        buffer.putInt(longitude.getEncodedValue());
        buffer.putShort(altitude.getEncodedValue());
        mParameters = buffer.array();
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
