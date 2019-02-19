package no.nordicsemi.android.meshprovisioner;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Class definition for allocating group range for provisioners.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@Entity(tableName = "allocated_group_range",
        foreignKeys = @ForeignKey(entity = Provisioner.class,
                parentColumns = "provisioner_uuid",
                childColumns = "provisioner_uuid",
                onUpdate = CASCADE,
                onDelete = CASCADE),
        indices = @Index("provisioner_uuid"))
public class AllocatedGroupRange {

    @PrimaryKey(autoGenerate = true)
    int id;

    @ColumnInfo(name = "provisioner_uuid")
    private String provisionerUuid;

    @ColumnInfo(name = "high_address")
    @Expose
    private byte[] highAddress;

    @ColumnInfo(name = "low_address")
    @Expose
    private byte[] lowAddress;

    @Ignore
    public AllocatedGroupRange() {

    }

    /**
     * Constructs {@link AllocatedGroupRange} for provisioner
     *
     * @param lowAddress  low address of group range
     * @param highAddress high address of group range
     */
    public AllocatedGroupRange(final byte[] lowAddress, final byte[] highAddress) {
        this.lowAddress = lowAddress;
        this.highAddress = highAddress;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Returns the provisionerUuid of the Mesh network
     * @return String provisionerUuid
     */
    public String getProvisionerUuid() {
        return provisionerUuid;
    }

    /**
     * Sets the provisionerUuid of the mesh network to this application key
     * @param provisionerUuid mesh network provisionerUuid
     */
    public void setProvisionerUuid(final String provisionerUuid) {
        this.provisionerUuid = provisionerUuid;
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
