package no.nordicsemi.android.mesh.utils;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Contains the proxy filter configuration set to a node
 */
@SuppressWarnings("unused")
public class ProxyFilter implements Parcelable {

    private final ProxyFilterType filterType;
    private final List<AddressArray> addresses = new ArrayList<>();

    /**
     * Constructs the proxy filter
     *
     * @param filterType Filter type based on {@link ProxyFilterType.FilterTypes}
     */
    public ProxyFilter(final ProxyFilterType filterType) {
        this.filterType = filterType;
    }

    private ProxyFilter(Parcel in) {
        filterType = in.readParcelable(ProxyFilterType.class.getClassLoader());
        in.readList(addresses, AddressArray.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(filterType, flags);
        dest.writeList(addresses);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProxyFilter> CREATOR = new Creator<ProxyFilter>() {
        @Override
        public ProxyFilter createFromParcel(Parcel in) {
            return new ProxyFilter(in);
        }

        @Override
        public ProxyFilter[] newArray(int size) {
            return new ProxyFilter[size];
        }
    };

    /**
     * Returns the {@link ProxyFilterType} that was set
     */
    public ProxyFilterType getFilterType() {
        return filterType;
    }

    /**
     * Returns the list of addresses containing {@link AddressArray} added to the proxy filter
     */
    public List<AddressArray> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    /**
     * Adds an address to the ProxyFilter
     *
     * @param addressArray address to be added
     */
    public void addAddress(final AddressArray addressArray) {
        if (!contains(addressArray)) {
            addresses.add(addressArray);
        }
    }

    /**
     * Checks is the address exists within the list of proxy filter addresses.
     *
     * @param addressArray address
     */
    private boolean contains(@NonNull final AddressArray addressArray) {
        for (AddressArray arr : addresses) {
            if (Arrays.equals(addressArray.getAddress(), arr.getAddress())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks is the address exists within the list of proxy filter addresses.
     * <p>
     * Currently does not support virtual addresses.
     * </p>
     *
     * @param address Unicast, Group address
     */
    public final boolean contains(@NonNull final byte[] address) {
        if (MeshAddress.isValidUnicastAddress(address) || MeshAddress.isValidSubscriptionAddress(address)) {
            final AddressArray addressArray = new AddressArray(address[0], address[1]);
            for (AddressArray arr : addresses) {
                if (Arrays.equals(addressArray.getAddress(), arr.getAddress())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Removes an address from the ProxyFilter
     *
     * @param addressArray address to be removed
     */
    public void removeAddress(final AddressArray addressArray) {
        addresses.remove(addressArray);
    }

}
