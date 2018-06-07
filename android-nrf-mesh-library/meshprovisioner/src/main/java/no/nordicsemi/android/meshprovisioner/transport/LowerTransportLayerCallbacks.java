package no.nordicsemi.android.meshprovisioner.transport;

import no.nordicsemi.android.meshprovisioner.messages.ControlMessage;

public interface LowerTransportLayerCallbacks {

    /**
     * Sends the transport layer acknowledgement to node
     *
     * @param controlMessage
     */
    void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage);

}
