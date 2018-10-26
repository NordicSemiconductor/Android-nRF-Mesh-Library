package no.nordicsemi.android.meshprovisioner.transport;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import no.nordicsemi.android.meshprovisioner.models.VendorModel;
import no.nordicsemi.android.meshprovisioner.utils.ConfigModelPublicationSetParams;

/**
 * Mesh message handler api
 */
@SuppressWarnings("unused")
interface MeshMessageHandlerApi {

    /**
     * Sends a composition data get message to the node
     *
     * @param meshNode mMeshNode to configure
     * @param aszmic   1 or 0 where 1 will create a message with a transport mic length of 8 and 4 if zero
     */
    void sendCompositionDataGet(@NonNull final ProvisionedMeshNode meshNode, final int aszmic);

    /**
     * Send App key add message to the node.
     *
     * @param appKey      application key
     * @param appKeyIndex application key index
     * @param aszmic      application size, if 0 uses 32-bit encryption and 64-bit otherwise
     */
    void sendAppKeyAdd(@NonNull final ProvisionedMeshNode meshNode, final int appKeyIndex, @NonNull final String appKey, final int aszmic);

    /**
     * Binds app key to a specified model
     *
     * @param meshNode        mesh node containing the model
     * @param aszmic          application size, if 0 uses 32-bit encryption and 64-bit otherwise
     * @param elementAddress  address of the element containing the model
     * @param modelIdentifier identifier of the model. This could be 16-bit SIG Model or a 32-bit Vendor model identifier
     * @param appKeyIndex     application key index
     */
    void bindAppKey(@NonNull final ProvisionedMeshNode meshNode, final int aszmic,
                    @NonNull final byte[] elementAddress, final int modelIdentifier, final int appKeyIndex);

    /**
     * Unbinds a previously bound app key from a specified model
     *
     * @param meshNode        mesh node containing the model
     * @param aszmic          application mic size, if 0 uses 32-bit encryption and 64-bit otherwise
     * @param elementAddress  address of the element containing the model
     * @param modelIdentifier identifier of the model. This could be 16-bit SIG Model or a 32-bit Vendor model identifier
     * @param appKeyIndex     application key index
     */
    void unbindAppKey(@NonNull final ProvisionedMeshNode meshNode, final int aszmic,
                      @NonNull final byte[] elementAddress, final int modelIdentifier, final int appKeyIndex);

    /**
     * Set a publish address for configuration model
     *
     * @param configModelPublicationSetParams contains the parameters for configmodel publication set
     */
    void sendConfigModelPublicationSet(@NonNull final ConfigModelPublicationSetParams configModelPublicationSetParams);

    /**
     * Send App key add message to the node.
     *
     * @param meshNode            Mesh node containing the model
     * @param aszmic              Application mic size, if 0 uses 32-bit encryption and 64-bit otherwise
     * @param elementAddress      Address of the element containing the model
     * @param subscriptionAddress Address to which the model must subscribe to
     * @param modelIdentifier     Identifier of the model. This could be 16-bit SIG Model or a 32-bit Vendor model identifier
     */
    void addSubscriptionAddress(@NonNull final ProvisionedMeshNode meshNode, final int aszmic,
                                @NonNull final byte[] elementAddress, @NonNull final byte[] subscriptionAddress, final int modelIdentifier);

    /**
     * Send App key add message to the node.
     *
     * @param meshNode            Mesh node containing the model
     * @param aszmic              Application mic size, if 0 uses 32-bit encryption and 64-bit otherwise
     * @param elementAddress      Address of the element containing the model
     * @param subscriptionAddress Address to which the model must unsubscribe from
     * @param modelIdentifier     Identifier of the model. This could be 16-bit SIG Model or a 32-bit Vendor model identifier
     */
    void deleteSubscriptionAddress(@NonNull final ProvisionedMeshNode meshNode, final int aszmic,
                                   @NonNull final byte[] elementAddress, @NonNull final byte[] subscriptionAddress, final int modelIdentifier);

    /**
     * Resets the specific mesh node
     *
     * @param provisionedMeshNode mesh node to be reset
     */
    void resetMeshNode(@NonNull final ProvisionedMeshNode provisionedMeshNode);

    /**
     * Send generic on off get to mesh node, this message sent is an acknowledged message.
     *
     * @param node        Mesh node to send to
     * @param model       Mesh model to control
     * @param dstAddress  Destination address where the message is sent to
     * @param aszmic      If aszmic set to 1 the messages are encrypted with 64bit encryption otherwise 32 bit
     * @param appKeyIndex Index of the app key to encrypt the message with
     */
    void getGenericOnOff(@NonNull final ProvisionedMeshNode node, @NonNull final MeshModel model,
                         @NonNull final byte[] dstAddress, final boolean aszmic, final int appKeyIndex);

    /**
     * Send generic on off set to mesh node, this message sent is an acknowledged message.
     *
     * @param node                 mesh node to send to
     * @param model                Mesh model to control
     * @param dstAddress           Destination address where the message is sent to
     * @param aszmic               if aszmic set to 1 the messages are encrypted with 64bit encryption otherwise 32 bit
     * @param appKeyIndex          index of the app key to encrypt the message with
     * @param transitionSteps      the number of steps
     * @param transitionResolution the resolution for the number of steps
     * @param delay                message execution delay in 5ms steps. After this delay milliseconds the model will execute the required behaviour.
     * @param state                on off state
     */
    void setGenericOnOff(@NonNull final ProvisionedMeshNode node, @NonNull final MeshModel model,
                         @NonNull final byte[] dstAddress, final boolean aszmic, final int appKeyIndex,
                         @Nullable final Integer transitionSteps, @Nullable final Integer transitionResolution, @Nullable final Integer delay, final boolean state);

    /**
     * Send generic on off set to mesh node, this message is an acknowledged message.
     *
     * @param dstAddress      Destination address where the message is sent to
     * @param genericOnOffSet {@link GenericOnOffSet} containing the generic on off get message opcode and parameters
     */
    void setGenericOnOff(final byte[] dstAddress, final GenericOnOffSet genericOnOffSet);

    /**
     * Send generic on off to mesh node
     *
     * @param node                 mesh node to send to
     * @param model                Mesh model to control
     * @param dstAddress           Destination address where the message is sent to
     * @param aszmic               if aszmic set to 1 the messages are encrypted with 64bit encryption otherwise 32 bit
     * @param appKeyIndex          index of the app key to encrypt the message with
     * @param transitionSteps      the number of steps
     * @param transitionResolution the resolution for the number of steps
     * @param delay                message execution delay in 5ms steps. After this delay milliseconds the model will execute the required behaviour.
     * @param state                on off state
     */
    void setGenericOnOffUnacknowledged(@NonNull final ProvisionedMeshNode node, @NonNull final MeshModel model,
                                       @NonNull final byte[] dstAddress, final boolean aszmic, final int appKeyIndex,
                                       @NonNull final Integer transitionSteps, @NonNull final Integer transitionResolution, @NonNull final Integer delay, final boolean state);

    /**
     * Send generic on off set to mesh node, this message is an unacknowledged message.
     *
     * @param dstAddress             Destination address where the message is sent to
     * @param genericOnOffSetUnacked {@link GenericOnOffSetUnacknowledged} containing the generic on off get message opcode and parameters
     */
    void setGenericOnOffUnacknowledged(@NonNull final byte[] dstAddress, @NonNull final GenericOnOffSetUnacknowledged genericOnOffSetUnacked);

    /**
     * Send generic level get to mesh node, this message sent is an acknowledged message.
     *
     * @param node        mesh node to send to
     * @param model       Mesh model to control
     * @param dstAddress  Destination address where the message is sent to
     * @param aszmic      if aszmic set to 1 the messages are encrypted with 64bit encryption otherwise 32 bit
     * @param appKeyIndex index of the app key to encrypt the message with
     */
    void getGenericLevel(@NonNull final ProvisionedMeshNode node, @NonNull final MeshModel model, @NonNull final byte[] dstAddress, final boolean aszmic, final int appKeyIndex);

    /**
     * Send generic level get to mesh node, this message sent is an acknowledged message.
     *
     * @param dstAddress      Destination address where the message is sent to
     * @param genericLevelGet {@link GenericLevelGet} containing the generic level set message opcode and parameters
     */
    void getGenericLevel(@NonNull final byte[] dstAddress, @NonNull final GenericLevelGet genericLevelGet);

    /**
     * Send generic level set to mesh node, this message sent is an acknowledged message.
     *
     * @param node                 mesh node to send to
     * @param model                Mesh model to control
     * @param dstAddress           Destination address where the message is sent to
     * @param aszmic               If aszmic set to 1 the messages are encrypted with 64bit encryption otherwise 32 bit
     * @param appKeyIndex          Index of the app key to encrypt the message with
     * @param transitionSteps      The number of steps
     * @param transitionResolution The resolution for the number of steps
     * @param delay                Message execution delay in 5ms steps. After this delay milliseconds the model will execute the required behaviour.
     * @param level                Level
     */
    void setGenericLevel(@NonNull final ProvisionedMeshNode node, @NonNull final MeshModel model, @NonNull final byte[] dstAddress,
                         final boolean aszmic, final int appKeyIndex, @Nullable final Integer transitionSteps,
                         @Nullable final Integer transitionResolution, @Nullable final Integer delay, final int level) throws IllegalArgumentException;

    /**
     * Send generic level set to mesh node, this message sent is an acknowledged message.
     *
     * @param dstAddress      Destination address where the message is sent to
     * @param genericLevelSet {@link GenericLevelSet} containing the generic level set message opcode and parameters
     */
    void setGenericLevel(@NonNull final byte[] dstAddress, @NonNull final GenericLevelSet genericLevelSet);

    /**
     * Send generic level to mesh node
     *
     * @param node                 Mesh node to send to
     * @param model                Mesh model to control
     * @param dstAddress           Destination address where the message is sent to
     * @param aszmic               If aszmic set to 1 the messages are encrypted with 64bit encryption otherwise 32 bit
     * @param appKeyIndex          Index of the app key to encrypt the message with
     * @param transitionSteps      The number of steps
     * @param transitionResolution The resolution for the number of steps
     * @param delay                Message execution delay in 5ms steps. After this delay milliseconds the model will execute the required behaviour.
     * @param level                level
     */
    void setGenericLevelUnacknowledged(@NonNull final ProvisionedMeshNode node, @NonNull final MeshModel model, @NonNull final byte[] dstAddress,
                                       final boolean aszmic, final int appKeyIndex, @Nullable final Integer transitionSteps,
                                       @Nullable final Integer transitionResolution, @Nullable final Integer delay, final int level) throws IllegalArgumentException;

    /**
     * Send generic level set to mesh node, this message sent is an acknowledged message.
     *
     * @param dstAddress             Destination address where the message is sent to
     * @param genericLevelSetUnacked {@link GenericLevelSetUnacknowledged} containing the generic level set message opcode and parameters
     */
    void setGenericLevelUnacknowledged(@NonNull final byte[] dstAddress, @NonNull final GenericLevelSetUnacknowledged genericLevelSetUnacked);

    /**
     * Send vendor model specific message to a node
     *
     * @param node        Target mesh nmesh node to send to
     * @param model       Mesh model to control
     * @param dstAddress  Destination address where the message is sent to
     * @param aszmic      If aszmic set to 1 the messages are encrypted with 64bit encryption otherwise 32 bit
     * @param appKeyIndex Index of the app key to encrypt the message with
     * @param opcode      Opcode of the message
     * @param parameters  Parameters of the message
     */
    void sendVendorModelUnacknowledgedMessage(@NonNull final ProvisionedMeshNode node, @NonNull final VendorModel model,
                                              @NonNull final byte[] dstAddress, final boolean aszmic, final int appKeyIndex,
                                              final int opcode, @Nullable final byte[] parameters);

    /**
     * Sends a raw unacknowledged vendor model message
     *
     * @param dstAddress                Destination address where the message is sent to
     * @param vendorModelMessageUnacked {@link VendorModelMessageUnacked} containing the unacknowledged vendor model message opcode and parameters
     */
    void sendVendorModelUnacknowledgedMessage(@NonNull final byte[] dstAddress, @NonNull final VendorModelMessageUnacked vendorModelMessageUnacked);

    /**
     * Send vendor model specific message to a node
     *
     * @param node        target mesh nmesh node to send to
     * @param model       Mesh model to control
     * @param dstAddress  Destination address where the message is sent to
     * @param aszmic      if aszmic set to 1 the messages are encrypted with 64bit encryption otherwise 32 bit
     * @param appKeyIndex index of the app key to encrypt the message with
     * @param opcode      opcode of the message
     * @param parameters  parameters of the message
     */
    void sendVendorModelAcknowledgedMessage(@NonNull final ProvisionedMeshNode node, @NonNull final VendorModel model,
                                            @NonNull final byte[] dstAddress, final boolean aszmic, final int appKeyIndex,
                                            final int opcode, @Nullable final byte[] parameters);

    /**
     * Sends a raw acknowledged vendor model message
     *
     * @param dstAddress              Destination address where the message is sent to
     * @param vendorModelMessageAcked {@link VendorModelMessageAcked} containing the unacknowledged vendor model message opcode and parameters
     */
    void sendVendorModelAcknowledgedMessage(@NonNull final byte[] dstAddress, @NonNull final VendorModelMessageAcked vendorModelMessageAcked);

    /**
     * Sends a mesh message specified within the {@link MeshMessage} object
     *
     * @param configurationMessage {@link ConfigMessage} Mesh message containing the message opcode and message parameters
     */
    void sendMeshMessage(@NonNull final MeshMessage configurationMessage);

    /**
     * Sends a mesh message specified within the {@link GenericMessage} object
     * <p> This method can be used specifically when sending an application message with a unicast address or a group address.
     * Application messages currently supported in the library are {@link GenericOnOffGet},{@link GenericOnOffSet}, {@link GenericOnOffSetUnacknowledged},
     * {@link GenericLevelGet},  {@link GenericLevelSet},  {@link GenericLevelSetUnacknowledged},
     * {@link VendorModelMessageAcked} and {@link VendorModelMessageUnacked}</p>
     *  @param dstAddress  Destination to which the message must be sent to, this could be a unicast address or a group address.
     * @param genericMessage Mesh message containing the message opcode and message parameters.
     */
    void sendMeshMessage(@NonNull final byte[] dstAddress, @NonNull final MeshMessage genericMessage);
}
