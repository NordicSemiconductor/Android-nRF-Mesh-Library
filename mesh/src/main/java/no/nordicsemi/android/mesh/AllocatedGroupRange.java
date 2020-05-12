package no.nordicsemi.android.mesh;

import android.os.Parcel;

import androidx.room.Ignore;
import no.nordicsemi.android.mesh.utils.MeshAddress;


/**
 * Class definition for allocating group range for provisioners.
 */
@SuppressWarnings({"unused"})
public class AllocatedGroupRange extends AddressRange {

    @Override
    public final int getLowerBound() {
        return MeshAddress.START_GROUP_ADDRESS;
    }

    @Override
    public final int getUpperBound() {
        return MeshAddress.END_GROUP_ADDRESS;
    }

    /**
     * Constructs {@link AllocatedGroupRange} for provisioner
     *
     * @param lowAddress  low address of group range
     * @param highAddress high address of group range
     */
    public AllocatedGroupRange(final int lowAddress, final int highAddress) {
        lowerBound = MeshAddress.START_GROUP_ADDRESS;
        upperBound = MeshAddress.END_GROUP_ADDRESS;
        if (!MeshAddress.isValidGroupAddress(lowAddress))
            throw new IllegalArgumentException("Low address must range from 0xC000 to 0xFEFF");

        if (!MeshAddress.isValidGroupAddress(highAddress))
            throw new IllegalArgumentException("High address must range from 0xC000 to 0xFEFF");

        /*if(lowAddress > highAddress)
            throw new IllegalArgumentException("low address must be lower than the high address");*/

        this.lowAddress = lowAddress;
        this.highAddress = highAddress;
    }

    @Ignore
    AllocatedGroupRange() {
    }

    protected AllocatedGroupRange(Parcel in) {
        lowerBound = in.readInt();
        upperBound = in.readInt();
        highAddress = in.readInt();
        lowAddress = in.readInt();
    }

    public static final Creator<AllocatedGroupRange> CREATOR = new Creator<AllocatedGroupRange>() {
        @Override
        public AllocatedGroupRange createFromParcel(Parcel in) {
            return new AllocatedGroupRange(in);
        }

        @Override
        public AllocatedGroupRange[] newArray(int size) {
            return new AllocatedGroupRange[size];
        }
    };

    @Override
    public int getLowAddress() {
        return lowAddress;
    }

    /**
     * Sets the low address of the allocated group address
     *
     * @param lowAddress of the group range
     */
    public void setLowAddress(final int lowAddress) {
        this.lowAddress = lowAddress;
    }

    @Override
    public int getHighAddress() {
        return highAddress;
    }

    /**
     * Sets the high address of the group address
     *
     * @param highAddress of the group range
     */
    public void setHighAddress(final int highAddress) {
        this.highAddress = highAddress;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(lowerBound);
        dest.writeInt(upperBound);
        dest.writeInt(highAddress);
        dest.writeInt(lowAddress);
    }
}
