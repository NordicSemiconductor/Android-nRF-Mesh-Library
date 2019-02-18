package no.nordicsemi.android.meshprovisioner.utils;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Locale;

/**
 * Abstract class for bluetooth mesh addresses
 */
@SuppressWarnings({"WeakerAccess", "BooleanMethodIsAlwaysInverted"})
public abstract class Address implements Parcelable {

    protected int address;

    /**
     * Returns the address
     */
    public int getAddress() {
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
        return address == (address & 0xFFFF);
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

    public static String formatAddress(final int address, final boolean add0x) {
        return add0x ? "0x" + String.format(Locale.US, "%04X", address) : String.format(Locale.US, "%04X", address);
    }

}
