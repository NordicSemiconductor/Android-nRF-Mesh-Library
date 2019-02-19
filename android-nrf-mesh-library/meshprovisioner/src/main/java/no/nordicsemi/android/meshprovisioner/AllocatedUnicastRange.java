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
 * Class definition for allocating unicast range for provisioners.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@Entity(tableName = "allocated_unicast_range",
        foreignKeys = @ForeignKey(entity = Provisioner.class,
                parentColumns = "provisioner_uuid",
                childColumns = "provisioner_uuid",
                onUpdate = CASCADE,
                onDelete = CASCADE),
        indices = @Index("provisioner_uuid"))
public class AllocatedUnicastRange {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    int id;

    @ColumnInfo(name = "provisioner_uuid")
    String provisionerUuid;

    @ColumnInfo(name = "low_address")
    @Expose
    private int lowAddress;

    @ColumnInfo(name = "high_address")
    @Expose
    private int highAddress;

    @Ignore
    public AllocatedUnicastRange() {

    }

    /**
     * Constructs {@link AllocatedUnicastRange} for provisioner
     *
     * @param lowAddress  low address of unicast range
     * @param highAddress high address of unicast range
     */
    @Deprecated
    @Ignore
    public AllocatedUnicastRange(final byte[] lowAddress, final byte[] highAddress) {
        this.lowAddress = MeshParserUtils.unsignedBytesToInt(lowAddress[1], lowAddress[0]);
        this.highAddress = MeshParserUtils.unsignedBytesToInt(highAddress[1], highAddress[0]);
    }

    /**
     * Constructs {@link AllocatedUnicastRange} for provisioner
     *
     * @param lowAddress  low address of unicast range
     * @param highAddress high address of unicast range
     */
    public AllocatedUnicastRange(final int lowAddress, final int highAddress) {
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
     * Returns the low address of the allocated unicast address
     *
     * @return low address
     */
    public int getLowAddress() {
        return lowAddress;
    }

    /**
     * Sets the low address of the allocated unicast address
     *
     * @param lowAddress of the unicast range
     */
    public void setLowAddress(final int lowAddress) {
        this.lowAddress = lowAddress;
    }

    /**
     * Returns the high address of the allocated unicast range
     *
     * @return highAddress of the group range
     */
    public int getHighAddress() {
        return highAddress;
    }

    /**
     * Sets the high address of the allocated unicast address
     *
     * @param highAddress of the group range
     */
    public void setHighAddress(final int highAddress) {
        this.highAddress = highAddress;
    }
}
