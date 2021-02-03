package no.nordicsemi.android.mesh.transport;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.opcodes.ProxyConfigMessageOpCodes;
import no.nordicsemi.android.mesh.utils.AddressArray;

/**
 * To be used as a wrapper class to create the ProxyConfigSetFilterType message.
 */
public class ProxyConfigAddAddressToFilter extends ProxyConfigMessage {

    private final List<AddressArray> addresses;

    /**
     * Sets the proxy filter
     *
     * @param addresses List of addresses to be added to the filter
     */
    public ProxyConfigAddAddressToFilter(@NonNull final List<AddressArray> addresses) throws IllegalArgumentException {
        this.addresses = new ArrayList<>();
        this.addresses.addAll(addresses);
        assembleMessageParameters();
    }

    @Override
    void assembleMessageParameters() throws IllegalArgumentException {
        if(addresses.isEmpty())
            throw new IllegalArgumentException("Address list cannot be empty!");
        final int length = (int) Math.pow(2 , addresses.size());
        mParameters = new byte[length];
        int count = 0;
        for (AddressArray addressArray : addresses) {
            mParameters[count] = addressArray.getAddress()[0];
            mParameters[count + 1] = addressArray.getAddress()[1];
            count += 2;
        }
    }

    @Override
    public int getOpCode() {
        return ProxyConfigMessageOpCodes.ADD_ADDRESS;
    }

    @Override
    byte[] getParameters() {
        return mParameters;
    }

    /**
     * Returns the addresses that were added to the proxy filter
     */
    public List<AddressArray> getAddresses(){
        return addresses;
    }
}
