package no.nordicsemi.android.mesh;

import android.os.Parcel;

import com.google.gson.annotations.Expose;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import no.nordicsemi.android.mesh.utils.SecureUtils;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Wrapper class for application key
 */
@Entity(tableName = "application_key",
        foreignKeys = @ForeignKey(entity = MeshNetwork.class,
                parentColumns = "mesh_uuid",
                childColumns = "mesh_uuid",
                onUpdate = CASCADE,
                onDelete = CASCADE),
        indices = @Index("mesh_uuid"))
public final class ApplicationKey extends MeshKey {

    @ColumnInfo(name = "bound_key_index")
    @Expose
    private int boundNetKeyIndex = 0;
    @Ignore
    private int aid;
    @Ignore
    private int oldAid;

    /**
     * Constructs a ApplicationKey object with a given key index and network key
     *
     * @param keyIndex 12-bit app key index
     * @param key      16-byte app key
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public ApplicationKey(final int keyIndex, @NonNull final byte[] key) {
        super(keyIndex, key);
        name = "Application Key " + (keyIndex + 1);
        aid = SecureUtils.calculateK4(key);
    }

    protected ApplicationKey(Parcel in) {
        super();
        meshUuid = in.readString();
        keyIndex = in.readInt();
        name = in.readString();
        boundNetKeyIndex = in.readInt();
        key = in.createByteArray();
        aid = in.readInt();
        oldKey = in.createByteArray();
        oldAid = in.readInt();
    }


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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(meshUuid);
        dest.writeInt(keyIndex);
        dest.writeString(name);
        dest.writeInt(boundNetKeyIndex);
        dest.writeByteArray(key);
        dest.writeInt(aid);
        dest.writeByteArray(oldKey);
        dest.writeInt(oldAid);
    }

    @Override
    public int describeContents() {
        return 0;
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

    @Override
    public void setKey(@NonNull final byte[] key) {
        super.setKey(key);
        aid = SecureUtils.calculateK4(key);
    }

    @Override
    public void setOldKey(final byte[] oldKey) {
        super.setOldKey(oldKey);
        if (oldKey != null)
            oldAid = SecureUtils.calculateK4(oldKey);
    }

    public int getAid() {
        return aid;
    }

    public int getOldAid() {
        return oldAid;
    }

    @NonNull
    @Override
    public ApplicationKey clone() throws CloneNotSupportedException {
        return (ApplicationKey) super.clone();
    }
}
