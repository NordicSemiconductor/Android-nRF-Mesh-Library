package no.nordicsemi.android.meshprovisioner;

/**
 * Implement this class in order to get the transport callbacks from the {@link MeshManagerApi}
 */
public interface MeshManagerTransportCallbacks {
    /**
     * Passes the pdu to the ble module of the app
     *  @param meshNode peripheral mesh node to send to
     * @param pdu      mesh pdu to be sent out to the node
     */
    void sendPdu(final BaseMeshNode meshNode, final byte[] pdu);

    /**
     * Get mtu size supported by the peripheral node
     * <p>
     * This is used to get the supported mtu size from the ble module, so that the messages
     * that are larger than the supported mtu size could be segmented
     * </p>
     *
     * @return mtu size
     */
    int getMtu();
}
