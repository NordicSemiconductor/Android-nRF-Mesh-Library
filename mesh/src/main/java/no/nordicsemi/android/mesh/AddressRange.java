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
        /*ranges.clear();
        ranges.addAll(results);*/
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
}
