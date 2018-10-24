package no.nordicsemi.android.meshprovisioner;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import no.nordicsemi.android.meshprovisioner.transport.GenericLevelGet;
import no.nordicsemi.android.meshprovisioner.transport.GenericLevelSet;
import no.nordicsemi.android.meshprovisioner.transport.GenericLevelSetUnacknowledged;
import no.nordicsemi.android.meshprovisioner.transport.GenericOnOffGet;
import no.nordicsemi.android.meshprovisioner.transport.GenericOnOffSet;
import no.nordicsemi.android.meshprovisioner.transport.GenericOnOffSetUnacknowledged;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.transport.VendorModelMessageAcked;
import no.nordicsemi.android.meshprovisioner.transport.VendorModelMessageUnacked;
import no.nordicsemi.android.meshprovisioner.provisionerstates.UnprovisionedMeshNode;

@SuppressWarnings("unused")
interface MeshMngrApi {

    /**
     * Identifies the node that is to be provisioned.
     * <p>
     * This method will send a provisioning invite to the connected peripheral. This will help users to identify a particular node before starting the provisioning process.
     * This method must be invoked before calling {@link #startProvisioning(UnprovisionedMeshNode)}
     * </p
     *
     * @param address  Bluetooth address of the node
     * @param nodeName Friendly node name
     */
    void identifyNode(@NonNull final String address, @Nullable final String nodeName) throws IllegalArgumentException;

    /**
     * Starts provisioning an unprovisioned mesh node
     * <p>
     * This method will continue the provisioning process that was started by invoking {@link #identifyNode(String, String)}.
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
     * Get composition data of the node
     *
     * @param meshNode corresponding mesh node
     */
    void getCompositionData(@NonNull final ProvisionedMeshNode meshNode);

    /**
     * adds the given the app key to the global app key list on the node
     *
     * @param meshNode    corresponding mesh node
     * @param appKeyIndex index of the app key in the global app key list
     * @param appKey      application key
     */
    void addAppKey(@NonNull final ProvisionedMeshNode meshNode, final int appKeyIndex, @NonNull final String appKey);

    /**
     * binding the app key
     *
     * @param meshNode       corresponding mesh node
     * @param elementAddress elementAddress
     * @param model          16-bit SIG Model Identifier or 32-bit Vendor Model identifier
     * @param appKeyIndex    index of the app key
     */
    void bindAppKey(@NonNull final ProvisionedMeshNode meshNode, @NonNull final byte[] elementAddress, @NonNull final MeshModel model, final int appKeyIndex);

    /**
     * Unbinds a previously bound the app key.
     *
     * @param meshNode       corresponding mesh node
     * @param elementAddress elementAddress
     * @param model          16-bit SIG Model Identifier or 32-bit Vendor Model identifier
     * @param appKeyIndex    index of the app key
     */
    void unbindAppKey(@NonNull final ProvisionedMeshNode meshNode, @NonNull final byte[] elementAddress, @NonNull final MeshModel model, final int appKeyIndex);

    /**
     * Set a subscription address for configuration model
     *
     * @param meshNode            Mesh node containing the model
     * @param elementAddress      Address of the element containing the model
     * @param subscriptionAddress Address to which the model must subscribe
     * @param modelIdentifier     Identifier of the model. This could be 16-bit SIG Model or a 32-bit Vendor model identifier
     */
    void addSubscriptionAddress(@NonNull final ProvisionedMeshNode meshNode, @NonNull final byte[] elementAddress, @NonNull final byte[] subscriptionAddress,
                                       final int modelIdentifier);

    /**
     * Delete a subscription address for configuration model
     *
     * @param meshNode            Mesh node containing the model
     * @param elementAddress      Address of the element containing the model
     * @param subscriptionAddress Address to which the model must subscribe
     * @param modelIdentifier     Identifier of the model. This could be 16-bit SIG Model or a 32-bit Vendor model identifier
     */
    void deleteSubscriptionAddress(@NonNull final ProvisionedMeshNode meshNode, @NonNull final byte[] elementAddress, @NonNull final byte[] subscriptionAddress,
                                          final int modelIdentifier);

    /**
     * Resets the specific mesh node
     *
     * @param provisionedMeshNode mesh node to be reset
     */
    void resetMeshNode(@NonNull final ProvisionedMeshNode provisionedMeshNode);

    /**
     * Send generic on off get to mesh node
     *
     * @param node        mesh node to send generic on off get
     * @param model       model to control
     * @param appKeyIndex application key index
     */
    void getGenericOnOff(@NonNull final ProvisionedMeshNode node, @NonNull final MeshModel model, @NonNull final byte[] dstAddress, final int appKeyIndex);

    /**
     * Send generic on off get to mesh node, this message is an acknowledged message.
     *
     * @param dstAddress
     * @param genericOnOffGet {@link GenericOnOffGet} containing the generic on off get message opcode and parameters
     */
    void getGenericOnOff(final byte[] dstAddress, @NonNull final GenericOnOffGet genericOnOffGet);

    /**
     * Send generic on off set to mesh node
     *
     * @param node                 mesh node to send generic on off get
     * @param model                model to control
     * @param dstAddress           address of the element the mesh model belongs to
     * @param appKeyIndex          application key index
     * @param transitionSteps      the number of steps
     * @param transitionResolution the resolution for the number of steps
     * @param delay                message execution delay in 5ms steps. After this delay milliseconds the model will execute the required behaviour.
     * @param state                on off state
     */
    void setGenericOnOff(final ProvisionedMeshNode node, final MeshModel model, final byte[] dstAddress, final int appKeyIndex, @Nullable final Integer transitionSteps,
                                @Nullable final Integer transitionResolution, @Nullable final Integer delay, final boolean state);

    /**
     * Send generic on off set unacknowledged message to mesh node
     *
     * @param node                 mesh node to send generic on off get
     * @param model                model to control
     * @param dstAddress           address of the element the mesh model belongs to
     * @param appKeyIndex          application key index
     * @param transitionSteps      the number of steps
     * @param transitionResolution the resolution for the number of steps
     * @param delay                message execution delay in 5ms steps. After this delay milliseconds the model will execute the required behaviour.
     * @param state                on off state
     */
    void setGenericOnOffUnacknowledged(final ProvisionedMeshNode node, final MeshModel model, final byte[] dstAddress, final int appKeyIndex, @Nullable final Integer transitionSteps,
                                              @Nullable final Integer transitionResolution, @Nullable final Integer delay, final boolean state);

    /**
     * Send generic level get to mesh node
     *
     * @param node        mesh node to send generic on off get
     * @param model       model to control
     * @param appKeyIndex application key index
     */
    void getGenericLevel(final ProvisionedMeshNode node, final MeshModel model, final byte[] dstAddress, final int appKeyIndex);

    /**
     * Send generic level set to mesh node
     *
     * @param node                 mesh node to send generic on off get
     * @param model                model to control
     * @param dstAddress           address of the element the mesh model belongs to
     * @param appKeyIndex          application key index
     * @param transitionSteps      the number of steps
     * @param transitionResolution the resolution for the number of steps
     * @param delay                message execution delay in 5ms steps. After this delay milliseconds the model will execute the required behaviour.
     * @param level                level state
     */
    void setGenericLevel(final ProvisionedMeshNode node, final MeshModel model, final byte[] dstAddress, final int appKeyIndex, @Nullable final Integer transitionSteps,
                                @Nullable final Integer transitionResolution, @Nullable final Integer delay, final int level);

    /**
     * Send generic level set unacknowledged message to mesh node
     *
     * @param node                 mesh node to send generic on off get
     * @param model                model to control
     * @param dstAddress           address of the element the mesh model belongs to
     * @param appKeyIndex          application key index
     * @param transitionSteps      the number of steps
     * @param transitionResolution the resolution for the number of steps
     * @param delay                message execution delay in 5ms steps. After this delay milliseconds the model will execute the required behaviour.
     * @param level                level state
     */
    void setGenericLevelUnacknowledged(final ProvisionedMeshNode node, final MeshModel model, final byte[] dstAddress, final int appKeyIndex, @Nullable final Integer transitionSteps,
                                       @Nullable final Integer transitionResolution, @Nullable final Integer delay, final int level);

    /**
     * Send unacknowledged vendor model specific message to a node
     *
     * @param node        target mesh node to send to
     * @param model       Mesh model to control
     * @param address     this address could be the unicast address of the element or the subscribe address
     * @param appKeyIndex index of the app key to encrypt the message with
     * @param opcode      opcode of the message
     * @param parameters  parameters of the message
     */

    void sendVendorModelUnacknowledgedMessage(final ProvisionedMeshNode node, final MeshModel model, final byte[] address, final int appKeyIndex, final int opcode, final byte[] parameters);

    /**
     * Send acknowledged vendor model specific message to a node
     *
     * @param node        target mesh node to send to
     * @param model       Mesh model to control
     * @param address     this address could be the unicast address of the element or the subscribe address
     * @param appKeyIndex index of the app key to encrypt the message with
     * @param opcode      opcode of the message
     * @param parameters  parameters of the message
     */
    void sendVendorModelAcknowledgedMessage(final ProvisionedMeshNode node, final MeshModel model, final byte[] address, final int appKeyIndex, final int opcode, final byte[] parameters);

    /**
     * Sends the specified  mesh message specified within the {@link MeshMessage} object
     *
     * @param configurationMessage {@link MeshMessage} Mesh message containing the message opcode and message parameters
     */
    void sendMeshConfigurationMessage(@NonNull final MeshMessage configurationMessage);

    /**
     * Sends the specified mesh message specified within the {@link MeshMessage} class
     * <p> This method can be used specifically when sending an application message with a unicast address or a group address.
     * Application messages currently supported in the library are {@link GenericOnOffGet},{@link GenericOnOffSet}, {@link GenericOnOffSetUnacknowledged},
     * {@link GenericLevelGet},  {@link GenericLevelSet},  {@link GenericLevelSetUnacknowledged},
     * {@link VendorModelMessageAcked} and {@link VendorModelMessageUnacked}</p>
     *  @param dstAddress  Destination to which the message must be sent to, this could be a unicast address or a group address.
     * @param genericMessage Mesh message containing the message opcode and message parameters.
     */
    void sendMeshApplicationMessage(@NonNull final byte[] dstAddress, @NonNull final MeshMessage genericMessage);
}
