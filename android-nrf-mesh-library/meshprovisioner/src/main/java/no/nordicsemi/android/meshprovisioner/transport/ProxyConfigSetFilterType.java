package no.nordicsemi.android.meshprovisioner.transport;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.meshprovisioner.opcodes.ProxyConfigMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.ProxyFilterType;

/**
 * To be used as a wrapper class to create the ProxyConfigSetFilterType message.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ProxyConfigSetFilterType extends ProxyConfigMessage {

    private final ProxyFilterType filterType;

    /**
     * Sets the proxy filter
     *
     * @param filterType Filter type set by the proxy configuration
     */
    ProxyConfigSetFilterType(final ProxyFilterType filterType) {
        super(0);
        this.filterType = filterType;
    }

    @Override
    void assembleMessageParameters() {
        mParameters = ByteBuffer.allocate(4)
                .order(ByteOrder.BIG_ENDIAN)
                .putInt(filterType.getFilterType())
                .array();
    }

    @Override
    int getOpCode() {
        return ProxyConfigMessageOpCodes.SET_FILTER_TYPE;
    }

    @Override
    byte[] getParameters() {
        return mParameters;
    }
}
