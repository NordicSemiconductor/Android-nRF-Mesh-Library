package no.nordicsemi.android.meshprovisioner;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.meshprovisioner.utils.MeshTypeConverters;

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
public class Group implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    public int id = 0;

    @ColumnInfo(name = "name")
    @Expose
    @SerializedName("name")
    private String name = "Mesh Group";

    @ColumnInfo(name = "group_address")
    @Expose
    @SerializedName("address")
    private int groupAddress;

    @TypeConverters(MeshTypeConverters.class)
    @ColumnInfo(name = "group_address_label")
    @Expose(serialize = false, deserialize = false)
    private UUID groupAddressLabel;

    @ColumnInfo(name = "parent_address")
    @Expose
    @SerializedName("parentAddress")
    private int parentAddress;

    @ColumnInfo(name = "mesh_uuid")
    @Expose
    @SerializedName("meshUuid")
    private String meshUuid;

    /**
     * Constructs a mesh group
     *
     * @param groupAddress groupAddress of the group
     * @param meshUuid     uuid of the mesh network
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public Group(final int id, final int groupAddress, @NonNull final String meshUuid) {
        this.id = id;
        if (!MeshAddress.isValidGroupAddress(groupAddress) && !MeshAddress.isValidVirtualAddress(groupAddress)) {
            throw new IllegalArgumentException("Address cannot be an unassigned address, " +
                    "unicast address or an all-nodes address");
        }
        this.groupAddress = groupAddress;
        this.parentAddress = 0x0000;
        this.meshUuid = meshUuid;
    }

    /**
     * Constructs a mesh group
     *
     * @param groupAddress groupAddress of the group
     * @param meshUuid     uuid of the mesh network
     */
    @Ignore
    public Group(final int groupAddress, @NonNull final String meshUuid) {
        if (!MeshAddress.isValidGroupAddress(groupAddress)) {
            throw new IllegalArgumentException("Address cannot be an unassigned address, " +
                    "unicast address, all-nodes address or virtual address");
        }
        this.groupAddress = groupAddress;
        this.parentAddress = 0x0000;
        this.groupAddressLabel = null;
        this.meshUuid = meshUuid;
    }

    /**
     * Constructs a mesh group
     *
     * @param groupAddressLabel UUID label of the group the group address
     * @param meshUuid          uuid of the mesh network
     */
    @Ignore
    public Group(@NonNull final UUID groupAddressLabel, @NonNull final String meshUuid) {
        this.groupAddressLabel = groupAddressLabel;
        this.groupAddress = MeshAddress.generateVirtualAddress(groupAddressLabel);
        this.parentAddress = 0x0000;
        this.meshUuid = meshUuid;
    }

    protected Group(Parcel in) {
        id = in.readInt();
        name = in.readString();
        groupAddress = in.readInt();
        parentAddress = in.readInt();
        meshUuid = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(groupAddress);
        dest.writeInt(parentAddress);
        dest.writeString(meshUuid);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

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
    public int getGroupAddress() {
        return groupAddress;
    }

    /**
     * Returns the label UUID of the group address
     *
     * @return Label UUID of the group address if the group address is a virtual address or null otherwise
     */
    @Nullable
    public UUID getGroupAddressLabel() {
        return groupAddressLabel;
    }

    /**
     * Sets the group address label
     *
     * @param uuidLabel UUID label of the address
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setGroupAddressLabel(@Nullable final UUID uuidLabel) {
        groupAddressLabel = uuidLabel;
    }

    /**
     * Returns address of the parent group if the group has one
     *
     * @return parent address
     */
    public int getParentAddress() {
        return parentAddress;
    }

    /**
     * Sets the parent address, if this group belongs to a parent group
     *
     * @param parentAddress address of the parent group
     */
    public void setParentAddress(final int parentAddress) {
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
    public void setName(@NonNull final String name) {
        this.name = name;
    }

}
