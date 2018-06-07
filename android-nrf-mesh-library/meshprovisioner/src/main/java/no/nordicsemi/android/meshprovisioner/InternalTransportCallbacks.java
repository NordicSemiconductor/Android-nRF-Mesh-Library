package no.nordicsemi.android.meshprovisioner;

public interface InternalTransportCallbacks {

    void sendPdu(final BaseMeshNode meshnode, final byte[] pdu);

}
