package no.nordicsemi.android.meshprovisioner.utils;

/**
 * Wrapper class for addresses
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class AddressArray {

    final byte[] address = new byte[2];

    /**
     * Constructs the AddressArray
     *
     * @param b1 address byte1
     * @param b2 address byte2
     */
    AddressArray(final byte b1, final byte b2){
        address[0] = b1;
        address[1] = b2;
    }

    /**
     * Returns address used in the filter message
     */
    public byte[] getAddress() {
        return address;
    }
}
