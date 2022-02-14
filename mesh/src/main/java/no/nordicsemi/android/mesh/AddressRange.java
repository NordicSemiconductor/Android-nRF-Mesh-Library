package no.nordicsemi.android.mesh;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public abstract class AddressRange extends Range {

    @Expose
    int lowAddress;

    @Expose
    int highAddress;

    /**
     * Returns the low address of the allocated group address
     *
     * @return low address
     */
    public abstract int getLowAddress();

    /**
     * Returns the high address of the allocated group range
     *
     * @return highAddress of the group range
     */
    public abstract int getHighAddress();

    @Override
    public int range() {
        return highAddress - lowAddress;
    }

    @Override
    public boolean overlaps(@NonNull final Range otherRange) {
        if (otherRange instanceof AddressRange) {
            final AddressRange otherAddressRange = (AddressRange) otherRange;
            return overlaps(lowAddress, highAddress, otherAddressRange.getLowAddress(), otherAddressRange.getHighAddress());
        }
        return false;
    }

    /**
     * Subtracts a range from a list of ranges
     *
     * @param ranges ranges to be subtracted
     * @param other  {@link AllocatedGroupRange} range
     * @return a resulting {@link AllocatedGroupRange} or null otherwise
     */
    @NonNull
    public static List<AllocatedGroupRange> minus(@NonNull final List<AllocatedGroupRange> ranges, @NonNull final AllocatedGroupRange other) {
        List<AllocatedGroupRange> results = new ArrayList<>();
        for (AllocatedGroupRange range : ranges) {
            results.addAll(range.minus(other));
            results = mergeGroupRanges(results);
        }
        return results;
    }

    /**
     * Deducts a range from another
     *
     * @param other right {@link AllocatedGroupRange}
     * @return a resulting {@link AllocatedGroupRange} or null otherwise
     */
    List<AllocatedGroupRange> minus(final AllocatedGroupRange other) {
        final List<AllocatedGroupRange> results = new ArrayList<>();
        // Left:   |------------|                    |-----------|                 |---------|
        //                  -                              -                            -
        // Right:      |-----------------|   or                     |---|   or        |----|
        //                  =                              =                            =
        // Result: |---|                             |-----------|                 |--|
        if (other.lowAddress > lowAddress) {
            final AllocatedGroupRange leftSlice = new AllocatedGroupRange(lowAddress, (Math.min(highAddress, other.lowAddress - 1)));
            results.add(leftSlice);
        }

        // Left:                |----------|             |-----------|                     |--------|
        //                         -                          -                             -
        // Right:      |----------------|           or       |----|          or     |---|
        //                         =                          =                             =
        // Result:                      |--|                      |--|                     |--------|
        if (other.highAddress < highAddress) {
            final AllocatedGroupRange rightSlice = new AllocatedGroupRange(Math.max(other.highAddress + 1, lowAddress), highAddress);
            results.add(rightSlice);
        }
        return results;
    }

    /**
     * Subtracts a range from a list of ranges
     *
     * @param ranges ranges to be subtracted
     * @param other  {@link AllocatedUnicastRange} range
     * @return a resulting {@link AllocatedUnicastRange} or null otherwise
     */
    @NonNull
    public static List<AllocatedUnicastRange> minus(@NonNull final List<AllocatedUnicastRange> ranges, @NonNull final AllocatedUnicastRange other) {
        List<AllocatedUnicastRange> results = new ArrayList<>();
        for (AllocatedUnicastRange range : ranges) {
            results.addAll(range.minus(other));
            results = mergeUnicastRanges(results);
        }
        return results;
    }

    /**
     * Deducts a range from another
     *
     * @param other right {@link AllocatedUnicastRange}
     * @return a resulting {@link AllocatedUnicastRange} or null otherwise
     */
    List<AllocatedUnicastRange> minus(final AllocatedUnicastRange other) {
        final List<AllocatedUnicastRange> results = new ArrayList<>();
        // Left:   |------------|                    |-----------|                 |---------|
        //                  -                              -                            -
        // Right:      |-----------------|   or                     |---|   or        |----|
        //                  =                              =                            =
        // Result: |---|                             |-----------|                 |--|
        if (other.lowAddress > lowAddress) {
            final AllocatedUnicastRange leftSlice = new AllocatedUnicastRange(lowAddress, (Math.min(highAddress, other.lowAddress - 1)));
            results.add(leftSlice);
        }

        // Left:                |----------|             |-----------|                     |--------|
        //                         -                          -                             -
        // Right:      |----------------|           or       |----|          or     |---|
        //                         =                          =                             =
        // Result:                      |--|                      |--|                     |--------|
        if (other.highAddress < highAddress) {
            final AllocatedUnicastRange rightSlice = new AllocatedUnicastRange(Math.max(other.highAddress + 1, lowAddress), highAddress);
            results.add(rightSlice);
        }
        return results;
    }

    /**
     * Checks if the address is in range.
     *
     * @param address address to be verified.
     * @return true if the address in range or false otherwise.
     */
    public Boolean isInRange(int address) {
        return address >= lowAddress && address <= highAddress;
    }

    /**
     * Checks a given address is within any of the address range in a given list of address ranges.
     *
     * @param ranges  List of ranges.
     * @param address address to be verified.
     * @return true if the address is in range or false otherwise.
     */
    public static Boolean isAddressInAnyRanges(List<? extends AddressRange> ranges, int address) {
        for (AddressRange range : ranges) {
            if (range.isInRange(address)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(final Object o) {
        if(this == o)
            return true;
        final AddressRange range = (AddressRange) o;
        return lowAddress == range.lowAddress && highAddress == range.highAddress;
    }

    @Override
    public int hashCode() {
        int result = lowAddress;
        result = 31 * result + highAddress;
        return result;
    }
}
