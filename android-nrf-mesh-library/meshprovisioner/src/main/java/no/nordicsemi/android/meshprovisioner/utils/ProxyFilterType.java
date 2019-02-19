package no.nordicsemi.android.meshprovisioner.utils;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Wrapper class for proxy filter types
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ProxyFilterType implements Parcelable {

    @SuppressWarnings("WeakerAccess")
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({WHITE_LIST_FILTER, BLACK_LIST_FILTER})
    public @interface FilterTypes {
    }

    /**
     * A white list filter has an associated white list, which is a list of destination addresses
     * that are of interest for the Proxy Client. The white list filter blocks all destination addresses
     * except those that have been added to the white list.
     */
    public static final int WHITE_LIST_FILTER = 0x00;   //White list filter type

    /**
     * A black list filter has an associated black list, which is a list of destination addresses
     * that the Proxy Client does not want to receive. The black list filter accepts all destination addresses
     * except those that have been added to the black list.
     */
    public static final int BLACK_LIST_FILTER = 0x01;   //The node supports Relay feature that is enabled

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
    public String getFilterTypeName(){
        if(filterType == WHITE_LIST_FILTER){
            return "Whitelist Filter";
        } else {
            return "Blacklist Filter";
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
