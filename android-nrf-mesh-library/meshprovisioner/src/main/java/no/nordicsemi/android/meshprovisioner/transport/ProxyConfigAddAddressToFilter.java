package no.nordicsemi.android.meshprovisioner.transport;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.opcodes.ProxyConfigMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.AddressArray;

/**
 * To be used as a wrapper class to create the ProxyConfigSetFilterType message.
 */
@SuppressWarnings("unused")
public class ProxyConfigAddAddressToFilter extends ProxyConfigMessage {

    private final List<AddressArray> addresses;

    /**
     * Sets the proxy filter
     *
     * @param addresses List of addresses to be added to the filter
     */
    public ProxyConfigAddAddressToFilter(final List<AddressArray> addresses) {
        this.addresses = addresses;
        assembleMessageParameters();
    }

    @Override
    void assembleMessageParameters() {
        final int length = addresses.size() * 2;
        mParameters = new byte[length];
        int count = 0;
        for (AddressArray addressArray : addresses) {
            mParameters[count] = addressArray.getAddress()[1];
            mParameters[count + 1] = addressArray.getAddress()[0];
            count += 1;
        }
    }

    @Override
    int getOpCode() {
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
