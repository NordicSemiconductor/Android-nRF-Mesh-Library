package no.nordicsemi.android.meshprovisioner;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Class definition for allocating unicast range for provisioners.
 */
@SuppressWarnings("unused")
@Entity(tableName = "allocated_unicast_range")
public class AllocatedUnicastRange {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    int id;

    @ForeignKey(entity = Provisioner.class, parentColumns = "uuid", childColumns = "provisioner_uuid", onUpdate = CASCADE, onDelete = CASCADE)
    @ColumnInfo(name = "provisioner_uuid")
    String uuid;

    @ColumnInfo(name = "high_address")
    @Expose
    private byte[] highAddress;

    @ColumnInfo(name = "low_address")
    @Expose
    private byte[] lowAddress;

    public AllocatedUnicastRange(){

    }

    /**
     * Constructs {@link AllocatedUnicastRange} for provisioner
     *  @param lowAddress low address of unicast range
     * @param highAddress high address of unicast range
     */
    @Ignore
    AllocatedUnicastRange(final byte[] lowAddress, final byte[] highAddress) {
        this.lowAddress = lowAddress;
        this.highAddress = highAddress;
    }

    /**
     * Returns the low address of the allocated unicast address
     *
     * @return low address
     */
    public byte[] getLowAddress() {
        return lowAddress;
    }

    /**
     * Sets the low address of the allocated unicast address
     *
     * @param lowAddress of the unicast range
     */
    public void setLowAddress(final byte[] lowAddress) {
        this.lowAddress = lowAddress;
    }

    /**
     * Returns the high address of the allocated unicast range
     *
     * @return highAddress of the group range
     */
    public byte[] getHighAddress() {
        return highAddress;
    }

    /**
     * Sets the high address of the allocated unicast address
     *
     * @param highAddress of the group range
     */
    public void setHighAddress(final byte[] highAddress) {
        this.highAddress = highAddress;
    }
}
