package no.nordicsemi.android.meshprovisioner;

import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Abstract class containing mesh beacon information
 */
@SuppressWarnings("unused")
public abstract class MeshBeacon implements Parcelable {

    final byte[] serviceData;
    private final int beaconType; //0x00 Unprovisioned beacon, 0x01 Secure beacon

    /**
     * Constructs a {@link MeshBeacon} object
     *
     * @param serviceData service data advertised by aa unprovisioned node
     * @throws IllegalArgumentException if service data provide is invalid
     */
    @SuppressWarnings("ConstantConditions")
    MeshBeacon(@NonNull final byte[] serviceData){
        if(serviceData == null)
            throw new IllegalArgumentException("Invalid service data");
        this.serviceData = serviceData;
        this.beaconType = serviceData[0];
    }

    /**
     * Returns the beacon type value
     */
    public final int getBeaconType() {
        return beaconType;
    }
}
