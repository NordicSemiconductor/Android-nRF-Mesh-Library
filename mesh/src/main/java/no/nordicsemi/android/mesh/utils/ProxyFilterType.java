package no.nordicsemi.android.mesh.utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * Wrapper class for proxy filter types
 */
@SuppressWarnings({"WeakerAccess"})
public class ProxyFilterType implements Parcelable {

    @SuppressWarnings("WeakerAccess")
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({INCLUSION_LIST_FILTER, EXCLUSION_LIST_FILTER})
    public @interface FilterTypes {
    }

    /**
     * A inclusion list filter has an associated inclusion list, which is a list of destination addresses
     * that are of interest for the Proxy Client. The inclusion list filter blocks all destination addresses
     * except those that have been added to the inclusion list.
     */
    public static final int INCLUSION_LIST_FILTER = 0x00;   //inclusion list filter type

    /**
     * A exclusion list filter has an associated exclusion list, which is a list of destination addresses
     * that the Proxy Client does not want to receive. The exclusion list filter accepts all destination addresses
     * except those that have been added to the exclusion list.
     */
    public static final int EXCLUSION_LIST_FILTER = 0x01;   //The node supports Relay feature that is enabled

    /**
     * Filter type
     */
    private final int filterType;

    /**
     * Constructs the filter type to bet set to a proxy
     *
     * @param filterType {@link FilterTypes} supported by the proxy
     */
    public ProxyFilterType(@FilterTypes final int filterType) {
        this.filterType = filterType;
    }

    /**
     * Returns the filter type
     */
    @FilterTypes
    public int getType() {
        return filterType;
    }

    /**
     * Returns the filter type name
     */
    public String getFilterTypeName() {
        if (filterType == INCLUSION_LIST_FILTER) {
            return "Inclusion List";
        } else {
            return "Exclusion List";
        }
    }

    public static final Creator<ProxyFilterType> CREATOR = new Creator<ProxyFilterType>() {
        @Override
        public ProxyFilterType createFromParcel(Parcel in) {
            return new ProxyFilterType(in.readInt());
        }

        @Override
        public ProxyFilterType[] newArray(int size) {
            return new ProxyFilterType[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(filterType);
    }
}
