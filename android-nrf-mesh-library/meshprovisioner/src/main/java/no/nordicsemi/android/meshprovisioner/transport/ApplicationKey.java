package no.nordicsemi.android.meshprovisioner.transport;

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

import no.nordicsemi.android.meshprovisioner.MeshNetwork;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Wrapper class for application key
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@Entity(tableName = "application_key",
        foreignKeys = @ForeignKey(entity = MeshNetwork.class,
                parentColumns = "mesh_uuid",
                childColumns = "mesh_uuid",
                onUpdate = CASCADE,
                onDelete = CASCADE),
        indices = @Index("mesh_uuid"))
public final class ApplicationKey implements Parcelable {

    public static final Creator<ApplicationKey> CREATOR = new Creator<ApplicationKey>() {
        @Override
        public ApplicationKey createFromParcel(Parcel in) {
            return new ApplicationKey(in);
        }

        @Override
        public ApplicationKey[] newArray(int size) {
            return new ApplicationKey[size];
        }
    };
    @PrimaryKey(autoGenerate = true)
    int id;
    @ColumnInfo(name = "mesh_uuid")
    @Expose
    String meshUuid;
    @ColumnInfo(name = "index")
    @Expose
    private int keyIndex;
    @ColumnInfo(name = "name")
    @Expose
    private String name = "Application Key";
    @ColumnInfo(name = "bound_key_index")
    @Expose
    private int boundNetKeyIndex = 0;
    @ColumnInfo(name = "key")
    @Expose
    private byte[] key;
    @ColumnInfo(name = "old_key")
    @Expose
    private byte[] oldKey;

    /**
     * Constructs a ApplicationKey object with a given key index and network key
     *
     * @param keyIndex 12-bit app key index
     * @param key      16-byte app key
     */
    public ApplicationKey(final int keyIndex, @NonNull final byte[] key) {
        this.key = key;
        this.keyIndex = keyIndex;
    }

    protected ApplicationKey(Parcel in) {
        meshUuid = in.readString();
        keyIndex = in.readInt();
        name = in.readString();
        boundNetKeyIndex = in.readInt();
        key = in.createByteArray();
        oldKey = in.createByteArray();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(meshUuid);
        dest.writeInt(keyIndex);
        dest.writeString(name);
        dest.writeInt(boundNetKeyIndex);
        dest.writeByteArray(key);
        dest.writeByteArray(oldKey);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public int getId() {
        return id;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Returns the meshUuid of the Mesh network
     *
     * @return String meshUuid
     */
    public String getMeshUuid() {
        return meshUuid;
    }

    /**
     * Sets the meshUuid of the mesh network to this application key
     *
     * @param meshUuid mesh network meshUuid
     */
    public void setMeshUuid(final String meshUuid) {
        this.meshUuid = meshUuid;
    }

    /**
     * Returns a friendly name of the application key
     *
     * @return string containing the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets a friendly name of the application key
     *
     * @param name friendly name for the application key
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the application key
     *
     * @return 16 byte application key
     */
    public byte[] getKey() {
        return key;
    }

    /**
     * Sets a network key
     *
     * @param key 16-byte network key
     */
    public void setKey(@NonNull final byte[] key) {
        this.key = key;
    }

    /**
     * Returns the application key index
     *
     * @return key index
     */
    public int getKeyIndex() {
        return keyIndex;
    }

    /**
     * Sets the key index of network key
     *
     * @param keyIndex index
     */
    public void setKeyIndex(final int keyIndex) {
        this.keyIndex = keyIndex;
    }

    /**
     * Returns the index of the associated netkey
     *
     * @return network key index
     */
    public int getBoundNetKeyIndex() {
        return boundNetKeyIndex;
    }

    /**
     * Set the net key index to which the app key is associated with
     *
     * @param boundNetKeyIndex network key index
     */
    public void setBoundNetKeyIndex(final int boundNetKeyIndex) {
        this.boundNetKeyIndex = boundNetKeyIndex;
    }

    /**
     * Returns the old app key
     *
     * @return old key
     */
    public byte[] getOldKey() {
        return oldKey;
    }

    /**
     * Set the old key
     *
     * @param oldKey old app key
     */
    public void setOldKey(final byte[] oldKey) {
        this.oldKey = oldKey;
    }
}
