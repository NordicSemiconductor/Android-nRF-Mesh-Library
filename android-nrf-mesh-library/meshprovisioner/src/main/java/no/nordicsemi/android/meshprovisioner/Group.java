package no.nordicsemi.android.meshprovisioner;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import no.nordicsemi.android.meshprovisioner.utils.GroupAddress;

import static android.arch.persistence.room.ForeignKey.CASCADE;

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
    public Group(final int groupAddress, @NonNull final String meshUuid) {
        if (!GroupAddress.isValidGroupAddress(groupAddress)) {
            throw new IllegalArgumentException("Address cannot be an unassigned address, unicast address, all-nodes address or virtual address");
        }
        this.groupAddress = groupAddress;
        this.meshUuid = meshUuid;
    }

    protected Group(Parcel in) {
        groupAddress = in.readInt();
        parentAddress = in.readInt();
        meshUuid = in.readString();
        name = in.readString();
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
     * Sets a group address
     *
     * @param groupAddress 2 byte group address
     */
    public void setGroupAddress(final int groupAddress) {
        this.groupAddress = groupAddress;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(groupAddress);
        dest.writeInt(parentAddress);
        dest.writeString(meshUuid);
        dest.writeString(name);
    }
}
