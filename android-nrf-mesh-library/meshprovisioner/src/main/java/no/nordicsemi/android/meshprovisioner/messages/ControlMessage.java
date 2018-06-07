package no.nordicsemi.android.meshprovisioner.messages;

import java.util.HashMap;
import java.util.Map;

import no.nordicsemi.android.meshprovisioner.control.TransportControlMessage;

public class ControlMessage extends Message {

    private byte[] transportControlPdu;
    private TransportControlMessage transportControlMessage;

    public ControlMessage() {
        this.ctl = 1;
    }

    @Override
    public Map<Integer, byte[]> getNetworkPdu() {
        return networkPdu;
    }

    @Override
    public void setNetworkPdu(final HashMap<Integer, byte[]> pdu) {
        networkPdu = pdu;
    }

    public byte[] getTransportControlPdu() {
        return transportControlPdu;
    }

    public void setTransportControlPdu(final byte[] transportControlPdu) {
        this.transportControlPdu = transportControlPdu;
    }

    public HashMap<Integer, byte[]> getLowerTransportControlPdu() {
        return super.getLowerTransportControlPdu();
    }

    public void setLowerTransportControlPdu(final HashMap<Integer, byte[]> segmentedAccessMessages) {
        super.setLowerTransportControlPdu(segmentedAccessMessages);
    }

    public TransportControlMessage getTransportControlMessage() {
        return transportControlMessage;
    }

    public void setTransportControlMessage(final TransportControlMessage transportControlMessage) {
        this.transportControlMessage = transportControlMessage;
    }
}
