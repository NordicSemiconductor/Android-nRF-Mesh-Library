package no.nordicsemi.android.meshprovisioner.messages;

import java.util.HashMap;
import java.util.Map;

import no.nordicsemi.android.meshprovisioner.configuration.ConfigMessage;

public class AccessMessage extends Message {

    private byte[] accessPdu;
    private byte[] transportPdu;
    private ConfigMessage configMessage;

    public AccessMessage() {
        this.ctl = 0;
    }

    @Override
    public Map<Integer, byte[]> getNetworkPdu() {
        return networkPdu;
    }

    @Override
    public void setNetworkPdu(final HashMap<Integer, byte[]> pdu) {
        networkPdu = pdu;
    }

    public byte[] getAccessPdu() {
        return accessPdu;
    }

    public void setAccessPdu(final byte[] accessPdu) {
        this.accessPdu = accessPdu;
    }

    public byte[] getUpperTransportPdu() {
        return transportPdu;
    }

    public void setUpperTransportPdu(final byte[] transportPdu) {
        this.transportPdu = transportPdu;
    }

    public HashMap<Integer, byte[]> getLowerTransportAccessPdu() {
        return super.getLowerTransportAccessPdu();
    }

    public void setLowerTransportAccessPdu(final HashMap<Integer, byte[]> lowerTransportAccessPdu) {
        super.setLowerTransportAccessPdu(lowerTransportAccessPdu);
    }

    public ConfigMessage getConfigMessage() {
        return configMessage;
    }

    public void setConfigMessage(final ConfigMessage configMessage) {
        this.configMessage = configMessage;
    }
}
