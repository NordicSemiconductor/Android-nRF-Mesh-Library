package no.nordicsemi.android.mesh;

import android.os.Parcel;

import androidx.room.Ignore;
import no.nordicsemi.android.mesh.utils.MeshAddress;

/**
 * Class definition for allocating unicast range for provisioners.
 */
@SuppressWarnings({"unused"})
public class AllocatedUnicastRange extends AddressRange {

    /**
     * Constructs {@link AllocatedUnicastRange} for provisioner
     *
     * @param lowAddress  low address of unicast range
     * @param highAddress high address of unicast range
     */
    public AllocatedUnicastRange(final int lowAddress, final int highAddress) {
        lowerBound = MeshAddress.START_UNICAST_ADDRESS;
        upperBound = MeshAddress.END_UNICAST_ADDRESS;
        if (!MeshAddress.isValidUnicastAddress(lowAddress))
            throw new IllegalArgumentException("Low address must range from 0x0001 to 0x7FFF");

        if (!MeshAddress.isValidUnicastAddress(highAddress))
            throw new IllegalArgumentException("High address must range from 0x0001 to 0x7FFF");

        /*if(lowAddress > highAddress)
            throw new IllegalArgumentException("low address must be lower than the high address");*/

        this.lowAddress = lowAddress;
        this.highAddress = highAddress;
    }

    @Ignore
    AllocatedUnicastRange() {
    }

    @Override
    public final int getLowerBound() {
        return lowAddress;
    }

    @Override
    public final int getUpperBound() {
        return upperBound;
    }

    protected AllocatedUnicastRange(Parcel in) {
        lowerBound = in.readInt();
        upperBound = in.readInt();
        lowAddress = in.readInt();
        highAddress = in.readInt();
    }

    public static final Creator<AllocatedUnicastRange> CREATOR = new Creator<AllocatedUnicastRange>() {
        @Override
        public AllocatedUnicastRange createFromParcel(Parcel in) {
            return new AllocatedUnicastRange(in);
        }

        @Override
        public AllocatedUnicastRange[] newArray(int size) {
            return new AllocatedUnicastRange[size];
        }
    };

    @Override
    public int getLowAddress() {
        return lowAddress;
    }

    /**
     * Sets the low address of the allocated unicast address
     *
     * @param lowAddress of the unicast range
     */
    public void setLowAddress(final int lowAddress) {
        if (!MeshAddress.isValidUnicastAddress(lowAddress))
            throw new IllegalArgumentException("Low address must range from 0x0000 to 0x7FFF");
        this.lowAddress = lowAddress;
    }

    @Override
    public int getHighAddress() {
        return highAddress;
    }

    /**
     * Sets the high address of the allocated unicast address
     *
     * @param highAddress of the group range
     */
    public void setHighAddress(final int highAddress) {
        if (!MeshAddress.isValidUnicastAddress(lowAddress))
            throw new IllegalArgumentException("High address must range from 0x0000 to 0x7FFF");
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
        dest.writeInt(lowAddress);
        dest.writeInt(highAddress);
    }
}
