package no.nordicsemi.android.mesh;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Contains the information related to a secure network beacon.
 */
@SuppressWarnings("unused")
public class UnprovisionedBeacon extends MeshBeacon {
    private static final int BEACON_DATA_LENGTH = 19;
    private static final int OOB_INDEX = 17;
    private static final int URI_HASH_INDEX = 19;
    private final UUID uuid;
    private final byte[] oobInformation = new byte[2];
    private final byte[] uriHash = new byte[4];

    /**
     * Constructs a {@link UnprovisionedBeacon} object
     *
     * @param beaconData beacon data advertised by the mesh beacon
     * @throws IllegalArgumentException if advertisement data provide is empty or null
     */
    UnprovisionedBeacon(@NonNull final byte[] beaconData) {
        super(beaconData);
        if(beaconData.length < UnprovisionedBeacon.BEACON_DATA_LENGTH)
            throw new IllegalArgumentException("Invalid unprovisioned beacon data");

        final ByteBuffer buffer = ByteBuffer.wrap(beaconData);
        buffer.position(1);
        final long msb = buffer.getLong();
        final long lsb = buffer.getLong();
        uuid = new UUID(msb, lsb);
        buffer.get(oobInformation, 0, 2);
        if(buffer.remaining() == 4) {
            buffer.get(uriHash, 0, 4);
        }

    }

    @Override
    public int getBeaconType() {
        return beaconType;
    }

    /**
     * Returns the Device UUID advertised by an unprovisioned beacon
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Returns the oob information advertised by an unprovisioned beacon
     */
    public byte[] getOobInformation() {
        return oobInformation;
    }

    /**
     * Returns the uri hash advertised by an unprovisioned beacon
     */
    public byte[] getUriHash() {
        return uriHash;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeByteArray(beaconData);
    }

    public static final Parcelable.Creator<UnprovisionedBeacon> CREATOR = new Parcelable.Creator<UnprovisionedBeacon>() {
        @SuppressWarnings("ConstantConditions")
        @Override
        public UnprovisionedBeacon createFromParcel(final Parcel source) {
            return new UnprovisionedBeacon(source.createByteArray());
        }

        @Override
        public UnprovisionedBeacon[] newArray(final int size) {
            return new UnprovisionedBeacon[size];
        }
    };
}
