package no.nordicsemi.android.meshprovisioner;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Class definition for allocating group range for provisioners.
 */
@SuppressWarnings("unused")
@Entity(tableName = "allocated_group_range")
public class AllocatedGroupRange {

    @PrimaryKey(autoGenerate = true)
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

    public AllocatedGroupRange(){

    }
    /**
     * Constructs {@link AllocatedGroupRange} for provisioner
     *
     * @param lowAddress low address of group range
     * @param highAddress high address of group range
     */
    @Ignore
    AllocatedGroupRange(final byte[] lowAddress, final byte[] highAddress) {
        this.lowAddress = lowAddress;
        this.highAddress = highAddress;
    }

    /**
     * Returns the low address of the allocated group address
     *
     * @return low address
     */
    public byte[] getLowAddress() {
        return lowAddress;
    }

    /**
     * Sets the low address of the allocated group address
     *
     * @param lowAddress of the group range
     */
    public void setLowAddress(final byte[] lowAddress) {
        this.lowAddress = lowAddress;
    }

    /**
     * Returns the high address of the allocated group range
     *
     * @return highAddress of the group range
     */
    public byte[] getHighAddress() {
        return highAddress;
    }

    /**
     * Sets the high address of the group address
     *
     * @param highAddress of the group range
     */
    public void setHighAddress(final byte[] highAddress) {
        this.highAddress = highAddress;
    }
}
