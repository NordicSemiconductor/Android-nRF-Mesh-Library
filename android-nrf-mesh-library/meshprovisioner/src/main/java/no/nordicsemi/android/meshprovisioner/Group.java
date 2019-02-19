package no.nordicsemi.android.meshprovisioner;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Defines a group in a mesh network
 */
@SuppressWarnings("unused")
@Entity(tableName = "groups",
        foreignKeys = @ForeignKey(entity = MeshNetwork.class,
                parentColumns = "mesh_uuid",
                childColumns = "mesh_uuid",
                onUpdate = CASCADE, onDelete = CASCADE),
        indices = {@Index("mesh_uuid")})
public class Group {

    @ColumnInfo(name = "mesh_uuid")
    @Expose
    private String meshUuid;

    @SerializedName("name")
    @ColumnInfo(name = "name")
    @Expose
    private String name = "Mesh Group";

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "group_address")
    @Expose
    @SerializedName("address")
    private byte[] groupAddress;

    @ColumnInfo(name = "parent_address")
    @Expose
    @SerializedName("parentAddress")
    private byte[] parentAddress;

    /**
     * Constructs a mesh group
     *
     * @param groupAddress  groupAddress of the group
     * @param parentAddress parent address
     */
    public Group(@NonNull final byte[] groupAddress, @Nullable final byte[] parentAddress, @NonNull final String meshUuid) {
        this.groupAddress = groupAddress;
        if (Arrays.equals(groupAddress, parentAddress)) {
            throw new IllegalArgumentException("Address cannot match parent adddress");
        }
        this.parentAddress = parentAddress;
        this.meshUuid = meshUuid;
    }

    /**
     * Returns the provisionerUuid of the network
     *
     * @return provisionerUuid
     */
    public String getMeshUuid() {
        return meshUuid;
    }

    /**
     * Sets the provisionerUuid of the network
     *
     * @param meshUuid network provisionerUuid
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setMeshUuid(final String meshUuid) {
        this.meshUuid = meshUuid;
    }

    /**
     * Returns the group address
     *
     * @return 2 byte group address
     */
    public byte[] getGroupAddress() {
        return groupAddress;
    }

    /**
     * Sets a group address
     *
     * @param groupAddress 2 byte group address
     */
    public void setGroupAddress(@NonNull final byte[] groupAddress) {
        this.groupAddress = groupAddress;
    }

    /**
     * Returns address of the parent group if the group has one
     *
     * @return parent address
     */
    public byte[] getParentAddress() {
        return parentAddress;
    }

    /**
     * Sets the parent address, if this group belongs to a parent group
     *
     * @param parentAddress address of the parent group
     */
    public void setParentAddress(final byte[] parentAddress) {
        this.parentAddress = parentAddress;
    }

    /**
     * Returns the group name of a mesh network
     */
    public String getName() {
        return name;
    }

    /**
     * Sets a name to a mesh group
     */
    public void setName(final String name) {
        this.name = name;
    }
}
