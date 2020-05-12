package no.nordicsemi.android.mesh;

import android.os.Parcelable;
import androidx.annotation.NonNull;

/**
 * Abstract class containing mesh beacon information
 */
@SuppressWarnings("unused")
public abstract class MeshBeacon implements Parcelable {

    private static final String TAG = MeshBeacon.class.getSimpleName();
    static final int MESH_BEACON = 0x2B;
    final byte[] beaconData;
    final int beaconType;


    /**
     * Constructs a {@link MeshBeacon} object
     *
     * @param beaconData beacon data advertised by the mesh beacon
     * @throws IllegalArgumentException if beacon data provided is empty or null
     */
    @SuppressWarnings("ConstantConditions")
    MeshBeacon(@NonNull final byte[] beaconData) {
        if (beaconData == null)
            throw new IllegalArgumentException("Invalid beacon data");
        this.beaconData = beaconData;
        beaconType = beaconData[0];
    }

    /**
     * Returns the beacon type value
     */
    public abstract int getBeaconType();

}
