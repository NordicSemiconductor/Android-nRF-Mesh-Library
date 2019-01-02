package no.nordicsemi.android.meshprovisioner.transport;

import android.support.annotation.NonNull;

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
    byte[] getParameters() {
        return new byte[0];
    }
}
