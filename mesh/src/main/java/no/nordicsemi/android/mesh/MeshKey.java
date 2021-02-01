package no.nordicsemi.android.mesh;

import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

abstract class MeshKey implements Parcelable, Cloneable {

    @PrimaryKey(autoGenerate = true)
    protected int id;
    @ColumnInfo(name = "mesh_uuid")
    @Expose
    protected String meshUuid;
    @ColumnInfo(name = "index")
    @Expose
    protected int keyIndex;
    @ColumnInfo(name = "name")
    @Expose
    protected String name;
    @ColumnInfo(name = "key")
    @Expose
    protected byte[] key;
    @ColumnInfo(name = "old_key")
    @Expose
    protected byte[] oldKey;

    @Ignore
    MeshKey() {
    }

    @Ignore
    MeshKey(final int keyIndex, @NonNull final byte[] key) {
        this.keyIndex = keyIndex;
        if (key.length != 16) {
            throw new IllegalArgumentException("Application key must be 16-bytes");
        }
        this.key = key;
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
    public void setName(@NonNull final String name) throws IllegalArgumentException {
        if (TextUtils.isEmpty(name))
            throw new IllegalArgumentException("Name cannot be empty!");
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
     * Sets a network key.
     *
     * <p>
     * In order to change the key call {@link BaseMeshNetwork#updateNetKey(NetworkKey, String)}  or {@link BaseMeshNetwork#updateAppKey(ApplicationKey, String)})}}
     * </p>
     *
     * @param key 16-byte network key
     */
    public void setKey(@NonNull final byte[] key) {
        if (valid(key)) {
            this.key = key;
        }
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
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setKeyIndex(final int keyIndex) {
        this.keyIndex = keyIndex;
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
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setOldKey(final byte[] oldKey) {
        this.oldKey = oldKey;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ApplicationKey) {
            final ApplicationKey appKey = (ApplicationKey) obj;

            return Arrays.equals(key, appKey.key) && keyIndex == appKey.keyIndex;
        } else if (obj instanceof NetworkKey) {
            final NetworkKey netKey = (NetworkKey) obj;

            return Arrays.equals(key, netKey.key) && keyIndex == netKey.keyIndex;
        }
        return false;
    }

    @NonNull
    @Override
    public MeshKey clone() throws CloneNotSupportedException {
        return (MeshKey) super.clone();
    }

    protected boolean valid(@NonNull final byte[] key) {
        if (key.length != 16)
            throw new IllegalArgumentException("Key must be of length 16!");
        return true;
    }

    protected boolean distributeKey(@NonNull final byte[] key) {
        if (!Arrays.equals(this.key, oldKey))
            setOldKey(this.key);
        setKey(key);
        return true;
    }
}
