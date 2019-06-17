package no.nordicsemi.android.meshprovisioner;

import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Ignore;

@SuppressWarnings("WeakerAccess")
public abstract class Range implements Parcelable {

    @Ignore
    protected int lowerBound;

    @Ignore
    protected int upperBound;

    /**
     * Returns the lower bound of the Range
     */
    public abstract int getLowerBound();

    /**
     * Returns the upper bound of the range
     */
    public abstract int getUpperBound();

    /**
     * Checks if two ranges overlaps
     *
     * @param otherRange other range
     * @return true if overlaps or false otherwise
     */
    public abstract boolean overlaps(@NonNull final Range otherRange);

    /*
    public boolean overlaps(@NonNull final Range range, @NonNull final Range otherRange) {
        if (range instanceof AddressRange) {
            final AddressRange addressRange = (AddressRange) range;
            final AddressRange otherAddressRange = (AddressRange) otherRange;
            return overlaps(addressRange.getLowAddress(), addressRange.getHighAddress(), otherAddressRange.getLowAddress(), otherAddressRange.getHighAddress());
        } else {
            final AllocatedSceneRange sceneRange = (AllocatedSceneRange) range;
            final AllocatedSceneRange otherSceneRange = (AllocatedSceneRange) otherRange;
            return overlaps(sceneRange.getFirstScene(), sceneRange.getLastScene(), otherSceneRange.getFirstScene(), otherSceneRange.getLastScene());
        }
    }

    private boolean overlaps1(final int rLowAddress, final int rHighAddress, final int oLowAddress, final int oHighAddress) {
        if (rLowAddress >= oLowAddress && rHighAddress <= oHighAddress) {
            return true;
        } else if (rLowAddress <= oLowAddress && rHighAddress >= oLowAddress && rHighAddress <= oHighAddress) {
            return true;
        } else return rLowAddress >= oLowAddress && rLowAddress <= oHighAddress;
    }*/

    protected boolean overlaps(final int rLowAddress, final int rHighAddress, final int oLowAddress, final int oHighAddress) {
        // Are the ranges are equal
        if (rLowAddress == oLowAddress && rHighAddress == oHighAddress) {
            return true;
        }
        // Is the range greater than the other range?
        else if (rLowAddress < oLowAddress && rHighAddress > oHighAddress) {
            return true;
        }
        // Is the range within the other range?
        else if (rLowAddress > oLowAddress && rHighAddress < oHighAddress) {
            return true;
        }
        // Is the range's lower address lower than the other range's low address
        else if (rLowAddress <= oLowAddress &&
                rHighAddress >= oLowAddress && rHighAddress <= oHighAddress) {
            return true;
        }
        // Is the range's higher address greater than the other range's high address
        else return rHighAddress >= oHighAddress &&
                    rLowAddress >= oLowAddress && rLowAddress <= oHighAddress;
    }
}
