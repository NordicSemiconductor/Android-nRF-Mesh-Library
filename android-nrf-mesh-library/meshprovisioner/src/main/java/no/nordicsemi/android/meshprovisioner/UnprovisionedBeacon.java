package no.nordicsemi.android.meshprovisioner;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Contains the information related to a secure network beacon.
 */
@SuppressWarnings("unused")
public class UnprovisionedBeacon extends MeshBeacon {
    private static final int SERVICE_DATA_LENGTH = 18;
    private static final int OOB_INDEX = 17;
    private static final int URI_HASH_INDEX = 19;
    private final UUID uuid;
    private final byte[] oobInformation = new byte[2];
    private final byte[] uriHash = new byte[4];

    /**
     * Constructs a {@link UnprovisionedBeacon} object
     *
     * @param serviceData service data advertised by aa unprovisioned node
     * @throws IllegalArgumentException if service data provide is invalid
     */
    UnprovisionedBeacon(@NonNull final byte[] serviceData) {
        super(serviceData);
        if (serviceData.length != SERVICE_DATA_LENGTH) {
            throw new IllegalArgumentException("Invalid service data");
        }
        final ByteBuffer buffer = ByteBuffer.wrap(serviceData);
        uuid = new UUID(buffer.getLong(1), buffer.getLong(9));
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
        dest.writeByteArray(serviceData);
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
