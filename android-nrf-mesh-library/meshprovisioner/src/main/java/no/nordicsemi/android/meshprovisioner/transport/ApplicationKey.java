package no.nordicsemi.android.meshprovisioner.transport;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

/**
 * Wrapper class for application key
 */
@SuppressWarnings("WeakerAccess")
public final class ApplicationKey implements Parcelable {

    @Expose
    private String name = "Application Key";
    @Expose
    private final int keyIndex;
    @Expose
    final byte[] key;
    @Expose
    private int boundNetKeyIndex = 0;

    /**
     * Constructs a ApplicationKey object with a given key index and network key
     *
     * @param keyIndex 12-bit app key index
     * @param key      16-byte app key
     */
    public ApplicationKey(final int keyIndex, final byte[] key) {
        this.key = key;
        this.keyIndex = keyIndex;
    }

    protected ApplicationKey(Parcel in) {
        name = in.readString();
        keyIndex = in.readInt();
        key = in.createByteArray();
        boundNetKeyIndex = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(keyIndex);
        dest.writeByteArray(key);
        dest.writeInt(boundNetKeyIndex);
    }

    @Override
    public int describeContents() {
        return 0;
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
     * @return 16 byte application key
     */
    public byte[] getKey() {
        return key;
    }

    /**
     * Returns the application key index
     * @return key index
     */
    public int getKeyIndex() {
        return keyIndex;
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

}
