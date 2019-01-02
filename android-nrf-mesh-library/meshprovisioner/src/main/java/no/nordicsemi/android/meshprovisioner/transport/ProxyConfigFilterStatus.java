package no.nordicsemi.android.meshprovisioner.transport;

import android.support.annotation.NonNull;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.opcodes.ProxyConfigMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.AddressArray;

/**
 * To be used as a wrapper class to create the ProxyConfigSetFilterType message.
 */
@SuppressWarnings("unused")
public class ProxyConfigFilterStatus extends ProxyConfigStatusMessage {


    public ProxyConfigFilterStatus(@NonNull final ControlMessage controlMessage) {
        super(controlMessage);
        this.mParameters = controlMessage.getParameters();
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {

    }

    @Override
    int getOpCode() {
        return ProxyConfigMessageOpCodes.FILTER_STATUS;
    }
}
