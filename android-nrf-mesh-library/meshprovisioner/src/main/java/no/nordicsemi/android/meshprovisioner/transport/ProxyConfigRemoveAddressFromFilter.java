package no.nordicsemi.android.meshprovisioner.transport;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.opcodes.ProxyConfigMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.AddressArray;

/**
 * To be used as a wrapper class to create the ProxyConfigSetFilterType message.
 */
@SuppressWarnings("unused")
public class ProxyConfigRemoveAddressFromFilter extends ProxyConfigMessage {

    private final List<AddressArray> addresses;

    /**
     * Sets the proxy filter
     *
     * @param addresses List of addresses to be added to the filter
     */
    ProxyConfigRemoveAddressFromFilter(final List<AddressArray> addresses) {
        this.addresses = addresses;
    }

    @Override
    void assembleMessageParameters() {
        final int length = addresses.size() * 2;
        mParameters = new byte[length];
        int count = 0;
        for(AddressArray addressArray : addresses) {
            mParameters[count] = addressArray.getAddress()[0];
            mParameters[count + 1] = addressArray.getAddress()[1];
            count += 1;
        }
    }

    @Override
    int getOpCode() {
        return ProxyConfigMessageOpCodes.REMOVE_ADDRESS;
    }

    @Override
    byte[] getParameters() {
        return mParameters;
    }
}
