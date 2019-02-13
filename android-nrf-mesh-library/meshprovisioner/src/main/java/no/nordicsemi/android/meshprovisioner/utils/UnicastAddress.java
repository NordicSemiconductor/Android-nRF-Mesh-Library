package no.nordicsemi.android.meshprovisioner.utils;

import android.os.Parcel;
import android.support.annotation.NonNull;

/**
 * Class for unicast addresses in a mesh network
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class UnicastAddress extends Address {

    public static final int START_ADDRESS = 0x0001;
    public static final int END_ADDRESS = 0x7FFF;

    /**
     * Constructs a UnicastAddress
     *
     * @param address 16-bit unicast address
     */
    public UnicastAddress(@NonNull final byte[] address) throws IllegalArgumentException {
        if (!isValidAddress(address)) {
            throw new IllegalArgumentException("Invalid unicast address, unicast address must be a 16-bit value, and must range range from 0x0001 to 0x7FFF");
        }
        this.address = MeshParserUtils.bytesToInt(address);
    }

    /**
     * Constructs a UnicastAddress
     *
     * @param address 16-bit unicast address
     */
    public UnicastAddress(final int address) throws IllegalArgumentException {
        if (!isValidAddress(address)) {
            throw new IllegalArgumentException("Invalid unicast address, unicast address must be a 16-bit value, and must range from 0x0001 to 0x7FFF");
        }
        this.address = address;
    }

    @Override
    protected boolean isValidAddress(@NonNull final byte[] address) {
        return isAddressInRange(address) && isValidAddress(MeshParserUtils.bytesToInt(address));
    }

    @Override
    protected boolean isValidAddress(final int address) {
        return isAddressInRange(address) && isValidUnicastAddress(address);
    }

    /**
     * Validates a unicast address
     *
     * @param address 16-bit address
     * @return true if the address is valid and false otherwise
     */
    private boolean isValidUnicastAddress(final int address) {
        return address >= START_ADDRESS && address <= END_ADDRESS;
    }

    public static final Creator<UnicastAddress> CREATOR = new Creator<UnicastAddress>() {
        @SuppressWarnings("ConstantConditions")
        @Override
        public UnicastAddress createFromParcel(Parcel in) {
            return new UnicastAddress(in.readInt());
        }

        @Override
        public UnicastAddress[] newArray(int size) {
            return new UnicastAddress[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(address);
    }
}
