package no.nordicsemi.android.meshprovisioner;

import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;

public interface InternalMeshManagerCallbacks {

    /**
     * Internal callback to notify the {@link MeshManagerApi} of provisioned nodes
     *
     * @param meshNode node that was provisioned
     */
    void onNodeProvisioned(final ProvisionedMeshNode meshNode);

    /**
     * Internal callback to notify the {@link MeshManagerApi} of incremented unicast address.
     * <p>This is called after the composition data status is received so that we can increment address according to the number of elements on a node</p>
     *
     * @param unicastAddress updated unicast address
     */
    void onUnicastAddressChanged(final int unicastAddress);
}
