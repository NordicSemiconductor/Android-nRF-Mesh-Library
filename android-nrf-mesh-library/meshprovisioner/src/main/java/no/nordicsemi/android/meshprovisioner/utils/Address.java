package no.nordicsemi.android.meshprovisioner.utils;

import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Abstract class for bluetooth mesh addresses
 */
@SuppressWarnings({"WeakerAccess", "BooleanMethodIsAlwaysInverted"})
public abstract class Address implements Parcelable {

    protected byte[] address;

    /**
     * Returns the address
     */
    public byte[] getAddress() {
        return address;
    }

    /**
     * Checks if the address is in range
     *
     * @param address address
     * @return true if is in range or false otherwise
     */
    public static boolean isAddressInRange(@NonNull final byte[] address) {
        return address.length == 2;
    }

    /**
     * Checks if the address is in range
     *
     * @param address address
     * @return true if is in range or false otherwise
     */
    public static boolean isAddressInRange(final int address) {
        return address >= Short.MIN_VALUE && address <= Short.MAX_VALUE;
    }

    /**
     * Checks if the address is valid
     *
     * @param address 16-bit mesh address
     * @return true if valid and false otherwise
     */
    protected abstract boolean isValidAddress(@NonNull final byte[] address);

    /**
     * Checks if the address is valid
     *
     * @param address 16-bit mesh address
     * @return true if valid and false otherwise
     */
    protected abstract boolean isValidAddress(final int address);
}
