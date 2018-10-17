package no.nordicsemi.android.meshprovisioner;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import no.nordicsemi.android.meshprovisioner.message.ConfigAppKeyAdd;
import no.nordicsemi.android.meshprovisioner.message.ConfigCompositionDataGet;
import no.nordicsemi.android.meshprovisioner.message.ConfigModelAppBind;
import no.nordicsemi.android.meshprovisioner.message.ConfigModelAppUnbind;
import no.nordicsemi.android.meshprovisioner.message.ConfigModelPublicationSet;
import no.nordicsemi.android.meshprovisioner.message.ConfigModelSubscriptionAdd;
import no.nordicsemi.android.meshprovisioner.message.ConfigModelSubscriptionDelete;
import no.nordicsemi.android.meshprovisioner.message.ConfigNodeReset;
import no.nordicsemi.android.meshprovisioner.message.GenericLevelGet;
import no.nordicsemi.android.meshprovisioner.message.GenericLevelSet;
import no.nordicsemi.android.meshprovisioner.message.GenericLevelSetUnacknowledged;
import no.nordicsemi.android.meshprovisioner.message.GenericOnOffGet;
import no.nordicsemi.android.meshprovisioner.message.GenericOnOffSet;
import no.nordicsemi.android.meshprovisioner.message.GenericOnOffSetUnacknowledged;
import no.nordicsemi.android.meshprovisioner.message.MeshModel;
import no.nordicsemi.android.meshprovisioner.message.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.message.VendorModelMessageAcked;
import no.nordicsemi.android.meshprovisioner.message.VendorModelMessageUnacked;
import no.nordicsemi.android.meshprovisioner.models.VendorModel;
import no.nordicsemi.android.meshprovisioner.provisionerstates.UnprovisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.ConfigModelPublicationSetParams;

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
     * Get composition data of the node
     *
     * @param compositionDataGet {@link ConfigCompositionDataGet} containing the config composition data get message opcode and parameters.
     */
    void getCompositionData(@NonNull final ConfigCompositionDataGet compositionDataGet);

    /**
     * adds the given the app key to the global app key list on the node
     *
     * @param meshNode    corresponding mesh node
     * @param appKeyIndex index of the app key in the global app key list
     * @param appKey      application key
     */
    void addAppKey(@NonNull final ProvisionedMeshNode meshNode, final int appKeyIndex, @NonNull final String appKey);


    /**
     * adds the given the app key to the global app key list on the node.
     *
     * @param configAppKeyAdd {@link ConfigAppKeyAdd} containing the config app key add message opcode and parameters.
     */
    void addAppKey(@NonNull final ConfigAppKeyAdd configAppKeyAdd);

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
     * Binds app key to a specified model
     *
     * @param configModelAppBind {@link ConfigModelAppBind} containing the config model app bind message opcode and parameters.
     */
    void bindAppKey(@NonNull final ConfigModelAppBind configModelAppBind);

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
     * Unbinds app key to a specified model
     *
     * @param configModelAppUnbind {@link ConfigModelAppUnbind} containing the config model app unbind message opcode and parameters.
     */
    void unbindAppKey(@NonNull final ConfigModelAppUnbind configModelAppUnbind);

    /**
     * Set a publish address for configuration model
     *
     * @param configModelPublicationSetParams contains the parameters for config model publication set
     */
    void sendConfigModelPublicationSet(@NonNull ConfigModelPublicationSetParams configModelPublicationSetParams);

    /**
     * Set a publish address for configuration model
     *
     * @param configModelPublicationSet {@link ConfigModelPublicationSet} containing config model publication set message opcode and parameters
     */
    void setPublication(@NonNull final ConfigModelPublicationSet configModelPublicationSet);

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
     * Adds subscription address to a specific model.
     *
     * @param configModelSubscriptionAdd {@link ConfigModelSubscriptionAdd} containing the config model subscription add
     */
    void addSubscriptionAddress(@NonNull final ConfigModelSubscriptionAdd configModelSubscriptionAdd);

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
     * Send App key add message to the node.
     *
     * @param configModelSubscriptionDelete {@link ConfigModelSubscriptionDelete} containing the Config model model subscription delete opcode and parameters
     */
    void deleteSubscriptionAddress(@NonNull final ConfigModelSubscriptionDelete configModelSubscriptionDelete);

    /**
     * Resets the specific mesh node
     *
     * @param provisionedMeshNode mesh node to be reset
     */
    void resetMeshNode(@NonNull final ProvisionedMeshNode provisionedMeshNode);

    /**
     * Resets the specific mesh node
     *
     * @param configNodeReset config reset message.
     */
    void resetMeshNode(@NonNull final ConfigNodeReset configNodeReset);

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
     * Send generic on off set to mesh node, this message is an acknowledged message.
     *
     * @param dstAddress
     * @param genericOnOffSet {@link GenericOnOffSet} containing the generic on off get message opcode and parameters
     */
    void setGenericOnOff(final byte[] dstAddress, @NonNull final GenericOnOffSet genericOnOffSet);

    /**
     * Send generic on off set to mesh node, this message is an unacknowledged message.
     *
     * @param dstAddress
     * @param genericOnOffSet {@link GenericOnOffSet} containing the generic on off get message opcode and parameters
     */
    void setGenericOnOffUnacknowledged(final byte[] dstAddress, @NonNull final GenericOnOffSetUnacknowledged genericOnOffSet);

    /**
     * Send generic level get to mesh node, this message sent is an acknowledged message.
     *
     * @param dstAddress
     * @param genericLevelGet {@link GenericLevelGet} containing the generic level set message opcode and parameters
     */
    void getGenericLevel(@NonNull final byte[] dstAddress, @NonNull final GenericLevelGet genericLevelGet);

    /**
     * Send generic level set to mesh node, this message sent is an acknowledged message.
     *
     * @param dstAddress
     * @param genericLevelSet {@link GenericLevelSet} containing the generic level set message opcode and parameters
     */
    void setGenericLevel(@NonNull final byte[] dstAddress, @NonNull final GenericLevelSet genericLevelSet);

    /**
     * Send generic level set to mesh node, this message sent is an acknowledged message.
     *
     * @param dstAddress
     * @param genericLevelSet {@link GenericLevelSetUnacknowledged} containing the generic level set message opcode and parameters
     */
    void setGenericLevelUnacknowledged(@NonNull final byte[] dstAddress, @NonNull final GenericLevelSetUnacknowledged genericLevelSet);

    /**
     * Sends a raw unacknowledged vendor model message
     *
     * @param dstAddress
     * @param vendorModelMessageUnacked {@link VendorModelMessageUnacked} containing the unacknowledged vendor model message opcode and parameters
     */
    void sendVendorModelUnacknowledgedMessage(@NonNull final byte[] dstAddress, @NonNull final VendorModelMessageUnacked vendorModelMessageUnacked);

    /**
     * Sends a raw acknowledged vendor model message
     *
     * @param dstAddress
     * @param vendorModelMessageAcked {@link VendorModelMessageAcked} containing the unacknowledged vendor model message opcode and parameters
     */
    void sendVendorModelAcknowledgedMessage(@NonNull final byte[] dstAddress, @NonNull final VendorModelMessageAcked vendorModelMessageAcked);

}
