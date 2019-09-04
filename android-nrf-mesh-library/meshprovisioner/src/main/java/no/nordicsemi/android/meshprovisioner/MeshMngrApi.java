package no.nordicsemi.android.meshprovisioner;

import android.net.Uri;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.meshprovisioner.provisionerstates.UnprovisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.InputOOBAction;
import no.nordicsemi.android.meshprovisioner.utils.OutputOOBAction;

@SuppressWarnings("unused")
interface MeshMngrApi {

    /**
     * Sets the {@link MeshManagerCallbacks} listener
     *
     * @param callbacks callbacks
     */
    void setMeshManagerCallbacks(@NonNull final MeshManagerCallbacks callbacks);

    /**
     * Sets the {@link MeshProvisioningStatusCallbacks} listener to return provisioning status callbacks.
     *
     * @param callbacks callbacks
     */
    void setProvisioningStatusCallbacks(@NonNull final MeshProvisioningStatusCallbacks callbacks);

    /**
     * Sets the {@link MeshManagerCallbacks} listener to return mesh status callbacks.
     *
     * @param callbacks callbacks
     */
    void setMeshStatusCallbacks(@NonNull final MeshStatusCallbacks callbacks);

    /**
     * Handles notifications received by the client.
     * <p>
     * This method will check if the library should wait for more data in case of a gatt layer segmentation.
     * If its required the method will remove the segmentation bytes and reassemble the pdu together.
     * </p>
     *
     * @param mtuSize GATT MTU size
     * @param data    PDU received by the client
     */
    void handleNotifications(final int mtuSize, @NonNull final byte[] data);

    /**
     * Must be called to handle provisioning states
     *
     * @param mtuSize GATT MTU size
     * @param data    PDU received by the client
     */
    void handleWriteCallbacks(final int mtuSize, @NonNull final byte[] data);

    /**
     * Identifies the node that is to be provisioned.
     * <p>
     * This method will send a provisioning invite to the connected peripheral. This will help users to identify a particular node before starting the provisioning process.
     * This method must be invoked before calling {@link #startProvisioning(UnprovisionedMeshNode)}
     * </p
     *
     * @param deviceUUID Device uuid of the unprovisioned mesh node. This could be obtain by calling {{@link #getMeshBeacon(byte[])}}
     */
    void identifyNode(@NonNull final UUID deviceUUID) throws IllegalArgumentException;

    /**
     * Identifies the node that is to be provisioned.
     * <p>
     * This method will send a provisioning invite to the connected peripheral. This will help users to identify a particular node before starting the provisioning process.
     * This method must be invoked before calling {@link #startProvisioning(UnprovisionedMeshNode)}
     * </p
     *
     * @param deviceUUID     Device uuid of the unprovisioned mesh node. This could be obtain by calling {{@link #getMeshBeacon(byte[])}}
     * @param attentionTimer Attention timer in seconds
     */
    void identifyNode(@NonNull final UUID deviceUUID, final int attentionTimer) throws IllegalArgumentException;

    /**
     * Starts provisioning an unprovisioned mesh node
     * <p>
     * This method will continue the provisioning process that was started by invoking {@link #identifyNode(UUID, int)}.
     * </p>
     *
     * @param unprovisionedMeshNode {@link UnprovisionedMeshNode} node
     */
    void startProvisioning(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode) throws IllegalArgumentException;

    /**
     * Starts provisioning an unprovisioned mesh node with static oob
     * <p>
     * This method will continue the provisioning process that was started by invoking {@link #identifyNode(UUID, int)}.
     * </p>
     *
     * @param unprovisionedMeshNode {@link UnprovisionedMeshNode} node
     */
    void startProvisioningWithStaticOOB(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode) throws IllegalArgumentException;

    /**
     * Starts provisioning an unprovisioned mesh node output oob
     * <p>
     * This method will continue the provisioning process that was started by invoking {@link #identifyNode(UUID, int)}.
     * </p>
     *
     * @param unprovisionedMeshNode {@link UnprovisionedMeshNode} node
     * @param oobAction             selected {@link OutputOOBAction}
     */
    void startProvisioningWithOutputOOB(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode, final OutputOOBAction oobAction) throws IllegalArgumentException;

    /**
     * Starts provisioning an unprovisioned mesh node input OOB
     * <p>
     * This method will continue the provisioning process that was started by invoking {@link #identifyNode(UUID, int)}.
     * </p>
     *
     * @param unprovisionedMeshNode {@link UnprovisionedMeshNode} node
     * @param oobAction             selected {@link InputOOBAction}
     */
    void startProvisioningWithInputOOB(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode, @NonNull final InputOOBAction oobAction) throws IllegalArgumentException;

    /**
     * Set the provisioning confirmation
     *
     * @param authentication confirmation pin
     */
    void setProvisioningAuthentication(@NonNull final String authentication);

    /**
     * Returns the device uuid of an unprovisioned node
     *
     * @param serviceData service data in the adv packet
     */
    @NonNull
    UUID getDeviceUuid(@NonNull final byte[] serviceData) throws IllegalArgumentException;

    /**
     * Checks if the advertisement packet is a mesh beacon packet
     *
     * @param advertisementData data advertised by the mesh beacon
     * @return true if its a mesh beacon packet or false otherwise
     */
    boolean isMeshBeacon(@NonNull final byte[] advertisementData) throws IllegalArgumentException;

    /**
     * Returns the beacon information advertised by a mesh beaco packet
     *
     * @param advertisementData data advertised by the mesh beacon
     * @return the data advertised by a beacon packet or null otherwise
     */
    @Nullable
    byte[] getMeshBeaconData(final byte[] advertisementData) throws IllegalArgumentException;

    /**
     * Returns a {@link UnprovisionedBeacon}, {@link SecureNetworkBeacon} based on the advertised service data
     *
     * @param beaconData beacon data advertised by the mesh beacon
     */
    @Nullable
    MeshBeacon getMeshBeacon(final byte[] beaconData);

    /**
     * Generate network id
     *
     * @return network id
     */
    String generateNetworkId(@NonNull final byte[] networkKey);

    /**
     * Checks if the node identity matches
     *
     * @param meshNode    mesh node to match with
     * @param serviceData advertised service data
     * @return true if the hashes match or false otherwise
     */
    boolean nodeIdentityMatches(@NonNull final ProvisionedMeshNode meshNode, @NonNull final byte[] serviceData);

    /**
     * Checks if the node is advertising with Node Identity
     *
     * @param serviceData advertised service data
     * @return returns true if the node is advertising with Node Identity or false otherwise
     */
    boolean isAdvertisedWithNodeIdentity(@NonNull final byte[] serviceData);

    /**
     * Checks if the network ids match
     *
     * @param networkId   network id of the mesh
     * @param serviceData advertised service data
     * @return returns true if the network ids match or false otherwise
     */
    boolean networkIdMatches(@NonNull final String networkId, @NonNull final byte[] serviceData);

    /**
     * Returns the advertised hash
     *
     * @param serviceData advertised service data
     * @return returns the advertised hash
     */
    boolean isAdvertisingWithNetworkIdentity(@NonNull final byte[] serviceData);

    /**
     * Sends the specified  mesh message specified within the {@link MeshMessage} object
     *
     * @param dst         destination address
     * @param meshMessage {@link MeshMessage} Mesh message containing the message opcode and message parameters
     */
    void createMeshPdu(final int dst, @NonNull final MeshMessage meshMessage) throws IllegalArgumentException;

    /**
     * Loads the mesh network from the local database.
     * <p>
     * This will start an AsyncTask that will load the network from the database.
     * {@link MeshManagerCallbacks#onNetworkLoaded(MeshNetwork) will return the mesh network
     * </p>
     */
    void loadMeshNetwork();

    /**
     * Returns an already loaded mesh network, make sure to call {@link #loadMeshNetwork()} before calling this
     *
     * @return {@link MeshNetwork}
     */
    @Nullable
    MeshNetwork getMeshNetwork();

    /**
     * Exports mesh network to a json String
     */
    @Nullable
    String exportMeshNetwork();

    /**
     * Starts an asynchronous task that imports a network from the mesh configuration db json
     *
     * @param uri path to the mesh configuration database json file.
     */
    void importMeshNetwork(@NonNull final Uri uri);

    /**
     * Starts an asynchronous task that imports a network from the mesh configuration db json
     *
     * @param networkJson configuration database json.
     */
    void importMeshNetworkJson(@NonNull final String networkJson);

    /**
     * Generates a random virtual address
     */
    default UUID generateVirtualAddress() {
        return UUID.randomUUID();
    }
}
