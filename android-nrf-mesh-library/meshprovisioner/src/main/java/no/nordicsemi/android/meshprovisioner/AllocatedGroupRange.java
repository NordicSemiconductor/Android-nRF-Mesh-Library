package no.nordicsemi.android.meshprovisioner;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

import static android.arch.persistence.room.ForeignKey.CASCADE;


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
    @Expose
    private String provisionerUuid;

    @Expose
    private int highAddress;

    @ColumnInfo(name = "low_address")
    @Expose
    private int lowAddress;

    @Ignore
    public AllocatedGroupRange() {

    }

    /**
     * Constructs {@link AllocatedGroupRange} for provisioner
     *
     * @param lowAddress  low address of group range
     * @param highAddress high address of group range
     */
    @Deprecated
    @Ignore
    public AllocatedGroupRange(final byte[] lowAddress, final byte[] highAddress) {
        this.lowAddress = MeshParserUtils.unsignedBytesToInt(lowAddress[1], lowAddress[0]);
        this.highAddress = MeshParserUtils.unsignedBytesToInt(highAddress[1], highAddress[0]);
    }

    /**
     * Constructs {@link AllocatedGroupRange} for provisioner
     *
     * @param lowAddress  low address of group range
     * @param highAddress high address of group range
     */
    public AllocatedGroupRange(final int lowAddress, final int highAddress) {
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

    /**
     * Returns the high address of the allocated group range
     *
     * @return highAddress of the group range
     */
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
}
