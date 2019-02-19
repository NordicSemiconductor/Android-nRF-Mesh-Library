package no.nordicsemi.android.meshprovisioner;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

import no.nordicsemi.android.meshprovisioner.provisionerstates.UnprovisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;

@SuppressWarnings("unused")
interface MeshMngrApi {

    /**
     * Identifies the node that is to be provisioned.
     * <p>
     * This method will send a provisioning invite to the connected peripheral. This will help users to identify a particular node before starting the provisioning process.
     * This method must be invoked before calling {@link #startProvisioning(UnprovisionedMeshNode)}
     * </p
     *
     * @param deviceUUID Device uuid of the unprovisioned mesh node. This could be obtain by calling {{@link #getMeshBeacon(byte[])}}
     * @param nodeName   Friendly node name
     */
    void identifyNode(@NonNull final UUID deviceUUID, @Nullable final String nodeName) throws IllegalArgumentException;

    /**
     * Starts provisioning an unprovisioned mesh node
     * <p>
     * This method will continue the provisioning process that was started by invoking {@link #identifyNode(UUID, String)}.
     * </p>
     *
     * @param unprovisionedMeshNode Bluetooth address of the node
     */
    void startProvisioning(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode) throws IllegalArgumentException;

    /**
     * Set the provisioning confirmation
     *
     * @param pin confirmation pin
     */
    void setProvisioningConfirmation(@NonNull final String pin);

    /**
     * Returns the device uuid of an unprovisioned node
     * @param serviceData service data in the adv packet
     */
    @NonNull
    UUID getDeviceUuid(@NonNull final byte[] serviceData) throws IllegalArgumentException;

    /**
     * Checks if the advertisement packet is a mesh beacon packet
     * @param advertisementData data advertised by the mesh beacon
     * @return true if its a mesh beacon packet or false otherwise
     */
    boolean isMeshBeacon(@NonNull final byte[] advertisementData) throws IllegalArgumentException;

    /**
     * Returns the beacon information advertised by a mesh beaco packet
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
     * Checks if the hashes match
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
     * @param dst
     * @param meshMessage {@link MeshMessage} Mesh message containing the message opcode and message parameters
     */
    void sendMeshMessage(@NonNull final byte[] dst, @NonNull final MeshMessage meshMessage);

    /**
     * Exports mesh network to a json file
     */
    void exportMeshNetwork(@NonNull final String path);


    /**
     * Starts an asynchronous task that imports a network from the mesh configuration db json
     * <p>Af</p>
     * @param uri path to the mesh configuration database json file.
     */
    void importMeshNetwork(@NonNull final Uri uri);
}
