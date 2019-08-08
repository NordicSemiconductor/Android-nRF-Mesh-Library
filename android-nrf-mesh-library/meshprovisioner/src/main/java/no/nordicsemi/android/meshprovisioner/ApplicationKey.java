package no.nordicsemi.android.meshprovisioner;

import android.os.Parcel;

import com.google.gson.annotations.Expose;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Wrapper class for application key
 */
@SuppressWarnings({"unused"})
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
    }

    protected ApplicationKey(Parcel in) {
        super();
        meshUuid = in.readString();
        keyIndex = in.readInt();
        name = in.readString();
        boundNetKeyIndex = in.readInt();
        key = in.createByteArray();
        oldKey = in.createByteArray();
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
        dest.writeByteArray(oldKey);
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
    public final boolean equals(@Nullable final Object obj) {
        if (super.equals(obj)) {
            final ApplicationKey appKey = (ApplicationKey) obj;
            return boundNetKeyIndex == ((ApplicationKey) obj).boundNetKeyIndex;
        }
        return false;
    }

    @NonNull
    @Override
    public ApplicationKey clone() throws CloneNotSupportedException {
        return (ApplicationKey) super.clone();
    }
}
