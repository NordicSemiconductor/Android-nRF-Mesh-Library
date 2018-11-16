package no.nordicsemi.android.meshprovisioner;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.nio.ByteBuffer;

/**
 * Contains the information related to a secure network beacon.
 */
@SuppressWarnings("unused")
public class SecureNetworkBeacon extends MeshBeacon {
    private static final int SERVICE_DATA_LENGTH = 22;
    private final int flags;
    private final byte[] networkId = new byte[8];
    private final int ivIndex;
    private final long authenticationValue;

    /**
     * Constructs a {@link SecureNetworkBeacon} object
     * @param serviceData service data advertised by a provisioned node
     * @throws IllegalArgumentException if service data provide is invalid
     */
    SecureNetworkBeacon(@NonNull final byte[] serviceData) {
        super(serviceData);
        if(serviceData.length != SERVICE_DATA_LENGTH){
            throw new IllegalArgumentException("Invalid service data");
        }
        final ByteBuffer byteBuffer = ByteBuffer.wrap(serviceData);
        byteBuffer.position(1);
        flags = byteBuffer.get();
        byteBuffer.get(networkId, byteBuffer.position(), 8);
        ivIndex = byteBuffer.getInt();
        authenticationValue = byteBuffer.getLong();
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
    public long getAuthenticationValue() {
        return authenticationValue;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeByteArray(serviceData);
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
