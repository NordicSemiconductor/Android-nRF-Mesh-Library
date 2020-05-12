package no.nordicsemi.android.mesh;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.annotation.RestrictTo;

/**
 * Holds the key index and the updated state for that key
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class NodeKey implements Parcelable {
    @Expose
    @SerializedName("index")
    private final int index;
    @Expose
    @SerializedName("updated")
    private boolean updated;

    /**
     * Constructs a NodeKey
     *
     * @param index Index of the key
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public NodeKey(final int index) {
        this(index, false);
    }

    /**
     * Constructs a NodeKey
     *
     * @param index   Index of the key
     * @param updated If the key has been updated
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public NodeKey(final int index, final boolean updated) {
        this.index = index;
        this.updated = updated;
    }

    private NodeKey(Parcel in) {
        index = in.readInt();
        updated = in.readByte() != 0;
    }

    public static final Creator<NodeKey> CREATOR = new Creator<NodeKey>() {
        @Override
        public NodeKey createFromParcel(Parcel in) {
            return new NodeKey(in);
        }

        @Override
        public NodeKey[] newArray(int size) {
            return new NodeKey[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(index);
        dest.writeByte((byte) (updated ? 1 : 0));
    }

    /**
     * Returns the index of the added key
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns true if the key has been updated
     */
    public boolean isUpdated() {
        return updated;
    }

    /**
     * Sets the updated state of the network/application key
     *
     * @param updated true if updated and false otherwise
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setUpdated(final boolean updated) {
        this.updated = updated;
    }
}
