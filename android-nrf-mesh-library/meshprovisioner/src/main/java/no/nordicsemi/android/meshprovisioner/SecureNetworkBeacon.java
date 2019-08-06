package no.nordicsemi.android.meshprovisioner;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;

/**
 * Contains the information related to a secure network beacon.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class SecureNetworkBeacon extends MeshBeacon {
    public static final int BEACON_DATA_LENGTH = 22;
    private final int flags;
    private final byte[] networkId = new byte[8];
    private final int ivIndex;
    private final byte[] authenticationValue = new byte[8];

    /**
     * Constructs a {@link SecureNetworkBeacon} object
     *
     * @param beaconData beacon data advertised by the mesh beacon
     * @throws IllegalArgumentException if service data provide is invalid
     */
    public SecureNetworkBeacon(@NonNull final byte[] beaconData) {
        super(beaconData);
        if (beaconData.length != SecureNetworkBeacon.BEACON_DATA_LENGTH)
            throw new IllegalArgumentException("Invalid secure network beacon data");

        final ByteBuffer byteBuffer = ByteBuffer.wrap(beaconData);
        byteBuffer.position(1);
        flags = byteBuffer.get();
        byteBuffer.get(networkId, 0, 8);
        ivIndex = byteBuffer.getInt();
        byteBuffer.get(authenticationValue, 0, 8);
    }

    @Override
    public int getBeaconType() {
        return beaconType;
    }

    /**
     * Returns the flags of the secure network beacon
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Returns the network id of the beacon or the node
     */
    public byte[] getNetworkId() {
        return networkId;
    }

    /**
     * Returns the iv index of the beacon or the node
     */
    public int getIvIndex() {
        return ivIndex;
    }

    /**
     * Returns the authentication value of the beacon
     */
    public byte[] getAuthenticationValue() {
        return authenticationValue;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeByteArray(beaconData);
    }

    public static final Parcelable.Creator<SecureNetworkBeacon> CREATOR = new Parcelable.Creator<SecureNetworkBeacon>() {
        @SuppressWarnings("ConstantConditions")
        @Override
        public SecureNetworkBeacon createFromParcel(final Parcel source) {
            return new SecureNetworkBeacon(source.createByteArray());
        }

        @Override
        public SecureNetworkBeacon[] newArray(final int size) {
            return new SecureNetworkBeacon[size];
        }
    };

}
