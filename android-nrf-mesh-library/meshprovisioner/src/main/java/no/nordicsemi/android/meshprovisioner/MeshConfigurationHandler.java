/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.meshprovisioner;

import android.content.Context;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.configuration.ConfigAppKeyAdd;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigAppKeyStatus;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigCompositionDataGet;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigCompositionDataStatus;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigMessage;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigModelAppBind;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigModelAppStatus;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigModelAppUnbind;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigModelPublicationSet;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigModelPublicationStatus;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigModelSubscriptionAdd;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigModelSubscriptionDelete;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigModelSubscriptionStatus;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigNodeReset;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigNodeResetStatus;
import no.nordicsemi.android.meshprovisioner.configuration.GenericOnOffGet;
import no.nordicsemi.android.meshprovisioner.configuration.GenericOnOffSet;
import no.nordicsemi.android.meshprovisioner.configuration.GenericOnOffSetUnacknowledged;
import no.nordicsemi.android.meshprovisioner.configuration.GenericOnOffStatus;
import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.opcodes.ConfigMessageOpCodes;

class MeshConfigurationHandler {

    private static final String TAG = MeshConfigurationHandler.class.getSimpleName();

    private final Context mContext;
    private final InternalTransportCallbacks mInternalTransportCallbacks;
    private final InternalMeshManagerCallbacks mInternalMeshManagerCallbacks;
    private MeshConfigurationStatusCallbacks mStatusCallbacks;
    private ConfigMessage configMessage;

    MeshConfigurationHandler(final Context context, final InternalTransportCallbacks internalTransportCallbacks, final InternalMeshManagerCallbacks internalMeshManagerCallbacks) {
        this.mContext = context;
        this.mInternalTransportCallbacks = internalTransportCallbacks;
        this.mInternalMeshManagerCallbacks = internalMeshManagerCallbacks;
    }

    public void setConfigurationCallbacks(final MeshConfigurationStatusCallbacks statusCallbacks) {
        this.mStatusCallbacks = statusCallbacks;
    }

    /**
     * Handle configuration states on write call back complete
     * <p>
     * This method will jump to the current state and switch the current state according to the message that has been sent.
     * </p>
     *
     * @param meshNode Corresponding mesh node
     * @param pdu      mesh pdu that was sent
     */
    protected void handleConfigurationWriteCallbacks(final ProvisionedMeshNode meshNode, final byte[] pdu) {
        switch (configMessage.getState()) {
            case COMPOSITION_DATA_GET:
                //Composition data get complete,
                //We directly switch to next state because there is no acknowledgements involved
                //configMessage = new ConfigCompositionDataStatus(mContext, mMeshNode, mInternalTransportCallbacks, mStatusCallbacks);
                break;
            case APP_KEY_ADD:
                final ConfigAppKeyAdd configAppKeyAdd = ((ConfigAppKeyAdd) configMessage);
                //Create the next corresponding status state
                final ConfigAppKeyStatus configAppKeyStatus = new ConfigAppKeyStatus(mContext, meshNode, configAppKeyAdd.getSrc(),
                        configAppKeyAdd.getAppKey(), mInternalTransportCallbacks, mStatusCallbacks);
                //Switch states
                switchStateOnWriteComplete(configAppKeyStatus);
                break;
            case CONFIG_MODEL_APP_BIND:
                final ConfigModelAppBind configModelAppBind = ((ConfigModelAppBind) configMessage);
                //Create the next corresponding status state
                final ConfigModelAppStatus configModelAppBindStatus = new ConfigModelAppStatus(mContext, meshNode, configModelAppBind.getState().getState(),
                        mInternalTransportCallbacks, mStatusCallbacks);
                //Switch states
                switchStateOnWriteComplete(configModelAppBindStatus);
                break;
            case CONFIG_MODEL_APP_UNBIND:
                final ConfigModelAppUnbind configModelAppUnbind = ((ConfigModelAppUnbind) configMessage);
                //Create the next corresponding status state
                final ConfigModelAppStatus configModelAppUnbindStatus = new ConfigModelAppStatus(mContext, meshNode, configModelAppUnbind.getState().getState(),
                        mInternalTransportCallbacks, mStatusCallbacks);
                //Switch states
                switchStateOnWriteComplete(configModelAppUnbindStatus);
                break;
            case CONFIG_MODEL_PUBLICATION_SET:
                // Create the corresponding status state
                final ConfigModelPublicationStatus configModelPublicationStatus = new ConfigModelPublicationStatus(mContext, meshNode, mInternalTransportCallbacks, mStatusCallbacks);
                //Switch states
                switchStateOnWriteComplete(configModelPublicationStatus);
                break;
            case CONFIG_MODEL_SUBSCRIPTION_ADD:
                // Create the corresponding status state
                final ConfigModelSubscriptionStatus subscriptionAddStatus = new ConfigModelSubscriptionStatus(mContext, meshNode,
                        ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_ADD, mInternalTransportCallbacks, mStatusCallbacks);
                //Switch states
                switchStateOnWriteComplete(subscriptionAddStatus);
                break;
            case CONFIG_MODEL_SUBSCRIPTION_DELETE:
                //Create the next corresponding status state
                final ConfigModelSubscriptionStatus subscriptionDeleteStatus = new ConfigModelSubscriptionStatus(mContext, meshNode,
                        ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_DELETE, mInternalTransportCallbacks, mStatusCallbacks);
                //Switch states
                switchStateOnWriteComplete(subscriptionDeleteStatus);
                break;
            case GENERIC_ON_OFF_GET:
                //Create the next corresponding status state
                final GenericOnOffStatus genericOnOffGetStatus = new GenericOnOffStatus(mContext, configMessage.getMeshNode(), configMessage.getMeshModel(),
                        configMessage.getAppKeyIndex(), mInternalTransportCallbacks, mStatusCallbacks);
                //Switch states
                switchStateOnWriteComplete(genericOnOffGetStatus);
                break;
            case GENERIC_ON_OFF_SET:
                //Create the next corresponding status state
                final GenericOnOffStatus genericOnOffSetStatus = new GenericOnOffStatus(mContext, configMessage.getMeshNode(), configMessage.getMeshModel(),
                        configMessage.getAppKeyIndex(), mInternalTransportCallbacks, mStatusCallbacks);
                //Switch states
                switchStateOnWriteComplete(genericOnOffSetStatus);
                break;
            case GENERIC_ON_OFF_SET_UNACKNOWLEDGED:
                //We don't expect a generic on off status as this is an unacknowledged message so we don't have to switch states here
                break;
            case CONFIG_NODE_RESET:
                //Create the next corresponding status state
                final ConfigNodeResetStatus configNodeResetStatus = new ConfigNodeResetStatus(mContext, configMessage.getMeshNode(), mInternalTransportCallbacks, mStatusCallbacks);
                //Switch states
                switchStateOnWriteComplete(configNodeResetStatus);
                break;
        }
    }

    /**
     * Handle configuration states on receiving mesh message notifications
     * <p>
     * This method will jump to the current state and switch the state depending on the expected and the next message received.
     * </p>
     *
     * @param meshNode Corresponding mesh node
     * @param pdu      mesh pdu that was sent
     */
    protected void parseConfigurationNotifications(final ProvisionedMeshNode meshNode, final byte[] pdu) {
        switch (configMessage.getState()) {
            case COMPOSITION_DATA_STATUS:
                final ConfigCompositionDataStatus compositionDataStatus = (ConfigCompositionDataStatus) configMessage;
                compositionDataStatus.parseData(pdu);
                mInternalMeshManagerCallbacks.onUnicastAddressChanged(compositionDataStatus.getUnicastAddress());
                break;
            case APP_KEY_ADD:
                final ConfigAppKeyAdd configAppKeyAdd = ((ConfigAppKeyAdd) configMessage);
                //Create the next corresponding status state
                final ConfigAppKeyStatus configAppKeyStatus = new ConfigAppKeyStatus(mContext, meshNode, configAppKeyAdd.getSrc(),
                        configAppKeyAdd.getAppKey(), mInternalTransportCallbacks, mStatusCallbacks);
                switchStateOnNotificationReceived(configAppKeyStatus, pdu);
                break;
            case APP_KEY_STATUS:
                ((ConfigAppKeyStatus) configMessage).parseData(pdu);
                break;
            case CONFIG_MODEL_APP_BIND:
                final ConfigModelAppBind configModelAppBind = ((ConfigModelAppBind) configMessage);
                //Create the next corresponding status state
                final ConfigModelAppStatus configModelAppStatus = new ConfigModelAppStatus(mContext, meshNode, configModelAppBind.getState().getState(),
                        mInternalTransportCallbacks, mStatusCallbacks);
                //Switch states
                switchStateOnNotificationReceived(configModelAppStatus, pdu);
                break;
            case CONFIG_MODEL_APP_UNBIND:
                final ConfigModelAppUnbind configModelAppUnbind = ((ConfigModelAppUnbind) configMessage);
                //Create the next corresponding status state
                final ConfigModelAppStatus configModelAppStatus1 = new ConfigModelAppStatus(mContext, meshNode, configModelAppUnbind.getState().getState(),
                        mInternalTransportCallbacks, mStatusCallbacks);
                //Switch states
                switchStateOnNotificationReceived(configModelAppStatus1, pdu);
                break;
            case CONFIG_MODEL_APP_STATUS:
                ((ConfigModelAppStatus) configMessage).parseData(pdu);
                break;
            case CONFIG_MODEL_PUBLICATION_SET:
                // Create the corresponding status state
                final ConfigModelPublicationStatus configModelPublicationStatus = new ConfigModelPublicationStatus(mContext, meshNode, mInternalTransportCallbacks, mStatusCallbacks);
                //Switch states
                switchStateOnNotificationReceived(configModelPublicationStatus, pdu);
                break;
            case CONFIG_MODEL_PUBLICATION_STATUS:
                ((ConfigModelPublicationStatus) configMessage).parseData(pdu);
                break;
            case CONFIG_MODEL_SUBSCRIPTION_ADD:
                // Create the corresponding status state
                final ConfigModelSubscriptionStatus configModelSubscriptionStatus = new ConfigModelSubscriptionStatus(mContext, meshNode,
                        ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_ADD, mInternalTransportCallbacks, mStatusCallbacks);
                //Switch states
                switchStateOnNotificationReceived(configModelSubscriptionStatus, pdu);
                break;
            case CONFIG_MODEL_SUBSCRIPTION_DELETE:
                //Create the next corresponding status state
                final ConfigModelSubscriptionStatus configModelSubscriptionStatus1 = new ConfigModelSubscriptionStatus(mContext, meshNode,
                        ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_DELETE, mInternalTransportCallbacks, mStatusCallbacks);
                //Switch states
                switchStateOnNotificationReceived(configModelSubscriptionStatus1, pdu);
                break;
            case CONFIG_MODEL_SUBSCRIPTION_STATUS:
                ((ConfigModelSubscriptionStatus) configMessage).parseData(pdu);
                break;
            case GENERIC_ON_OFF_GET:
                //Create the next corresponding status state
                final GenericOnOffStatus genericOnOffGetStatus = new GenericOnOffStatus(mContext, configMessage.getMeshNode(), configMessage.getMeshModel(),
                        configMessage.getAppKeyIndex(), mInternalTransportCallbacks, mStatusCallbacks);
                //Switch states
                switchStateOnNotificationReceived(genericOnOffGetStatus, pdu);
                break;
            case GENERIC_ON_OFF_SET_UNACKNOWLEDGED:
                //We do nothing here since there is no status involved for unacknowledged messages
                break;
            case GENERIC_ON_OFF_SET:
                //Create the next corresponding status state
                final GenericOnOffStatus genericOnOffSetStatus = new GenericOnOffStatus(mContext, configMessage.getMeshNode(), configMessage.getMeshModel(),
                        configMessage.getAppKeyIndex(), mInternalTransportCallbacks, mStatusCallbacks);
                //Switch states
                switchStateOnNotificationReceived(genericOnOffSetStatus, pdu);
                break;
            case GENERIC_ON_OFF_STATUS:
                ((GenericOnOffStatus) configMessage).parseData(pdu);
                break;
            case CONFIG_NODE_RESET:
                //Create the next corresponding status state
                final ConfigNodeResetStatus configNodeResetStatus = new ConfigNodeResetStatus(mContext, configMessage.getMeshNode(), mInternalTransportCallbacks, mStatusCallbacks);
                //Switch states
                switchStateOnNotificationReceived(configNodeResetStatus, pdu);
                break;
            case CONFIG_NODE_RESET_STATUS:
                ((ConfigNodeResetStatus) configMessage).parseData(pdu);
                break;
        }
    }

    /**
     * Switch the current state of the mesh configuration handler
     * <p>
     * This method will switch the current state to the corresponding next state if the message sent was not a segmented message.
     * </p>
     *
     * @param newState new state that is to be switched to
     * @return true if the state was switched successfully
     */
    private boolean switchStateOnWriteComplete(final ConfigMessage newState) {
        if (!configMessage.isSegmented()) {
            Log.v(TAG, "Switching current state " + configMessage.getState().name() + " to " + newState.getState().name());
            configMessage = newState;
            return true;
        }
        return false;
    }

    /**
     * Switch the current state of the mesh configuration handler
     * <p>
     * This method will switch the current state of the configuration handler based on the corresponding block acknowledgement pdu received.
     * The block acknowledgement pdu explains if certain segments were lost on flight, based on this we retransmit the segments that were lost on flight.
     * If there were no segments lost and the message that was sent was an acknowledged message, we switch the state to the corresponding message state.
     * </p>
     *
     * @param newState new state that is to be switched to
     * @param pdu      pdu received
     * @return true if the state was switched successfully
     */
    private boolean switchStateOnNotificationReceived(final ConfigMessage newState, final byte[] pdu) {
        if (configMessage.isSegmented() && configMessage.isRetransmissionRequired(pdu)) {
            configMessage.executeResend();
            return false;
        } else {
            Log.v(TAG, "Switching current state " + configMessage.getState().name() + " to " + newState.getState().name());
            configMessage = newState;
            return true;
        }
    }

    public ConfigMessage.MessageState getConfigurationState() {
        return configMessage.getState();
    }

    /**
     * Sends a composition data get message to the node
     *
     * @param meshNode mMeshNode to configure
     * @param aszmic   1 or 0 where 1 will create a message with a transport mic length of 8 and 4 if zero
     */
    public void sendCompositionDataGet(final ProvisionedMeshNode meshNode, final int aszmic) {
        final ConfigCompositionDataGet compositionDataGet = new ConfigCompositionDataGet(mContext,
                meshNode, aszmic, mInternalTransportCallbacks, mStatusCallbacks);
        configMessage = compositionDataGet;
        configMessage = new ConfigCompositionDataStatus(mContext, meshNode, mInternalTransportCallbacks, mStatusCallbacks);
        compositionDataGet.executeSend();
    }

    /**
     * Send App key add message to the node.
     */
    public void sendAppKeyAdd(final ProvisionedMeshNode meshNode, final int appKeyIndex, final String appKey, final int aszmic) {
        final ConfigAppKeyAdd configAppKeyAdd = new ConfigAppKeyAdd(mContext, meshNode, aszmic, appKey, appKeyIndex);
        configAppKeyAdd.setTransportCallbacks(mInternalTransportCallbacks);
        configAppKeyAdd.setConfigurationStatusCallbacks(mStatusCallbacks);
        configMessage = configAppKeyAdd;
        configAppKeyAdd.executeSend();
    }

    /**
     * Binds app key to a specified model
     *
     * @param meshNode        mesh node containing the model
     * @param aszmic          application size, if 0 uses 32-bit encryption and 64-bit otherwise
     * @param elementAddress  address of the element containing the model
     * @param modelIdentifier identifier of the model. This could be 16-bit SIG Model or a 32-bit Vendor model identifier
     * @param appKeyIndex     application key index
     */
    public void bindAppKey(final ProvisionedMeshNode meshNode, final int aszmic,
                           final byte[] elementAddress, final int modelIdentifier, final int appKeyIndex) {
        final ConfigModelAppBind configModelAppBind = new ConfigModelAppBind(mContext, meshNode, aszmic,
                elementAddress, modelIdentifier, appKeyIndex);
        configModelAppBind.setTransportCallbacks(mInternalTransportCallbacks);
        configModelAppBind.setConfigurationStatusCallbacks(mStatusCallbacks);
        configMessage = configModelAppBind;
        configModelAppBind.executeSend();
    }

    /**
     * Unbinds a previously bound app key from a specified model
     *
     * @param meshNode        mesh node containing the model
     * @param aszmic          application mic size, if 0 uses 32-bit encryption and 64-bit otherwise
     * @param elementAddress  address of the element containing the model
     * @param modelIdentifier identifier of the model. This could be 16-bit SIG Model or a 32-bit Vendor model identifier
     * @param appKeyIndex     application key index
     */
    public void unbindAppKey(final ProvisionedMeshNode meshNode, final int aszmic,
                             final byte[] elementAddress, final int modelIdentifier, final int appKeyIndex) {
        final ConfigModelAppUnbind configModelAppBind = new ConfigModelAppUnbind(mContext, meshNode, aszmic,
                elementAddress, modelIdentifier, appKeyIndex);
        configModelAppBind.setTransportCallbacks(mInternalTransportCallbacks);
        configModelAppBind.setConfigurationStatusCallbacks(mStatusCallbacks);
        configMessage = configModelAppBind;
        configModelAppBind.executeSend();
    }

    /**
     * Set a publish address for configuration model
     *
     * @param meshNode                       Mesh node containing the model
     * @param elementAddress                 Address of the element containing the model
     * @param publishAddress                 Address to which the model must publish
     * @param appKeyIndex                    Application key index
     * @param modelIdentifier                Identifier of the model. This could be 16-bit SIG Model or a 32-bit Vendor model identifier
     * @param credentialFlag                 Credential flag, set 0 to use master credentials and 1 for friendship credentials. If there is not friendship credentials master key material will be used by default
     * @param publishTtl                     Default ttl value for outgoing messages
     * @param publishPeriod                  Period for periodic status publishing
     * @param publishRetransmitCount         Number of retransmissions for each published message
     * @param publishRetransmitIntervalSteps Number of 50-millisecond steps between retransmissions
     */
    public void setConfigModelPublishAddress(final ProvisionedMeshNode meshNode, final int aszmic,
                                             final byte[] elementAddress, final byte[] publishAddress,
                                             final int appKeyIndex, final int modelIdentifier, final int credentialFlag, final int publishTtl,
                                             final int publishPeriod, final int publishRetransmitCount, final int publishRetransmitIntervalSteps) {
        final ConfigModelPublicationSet configModelPublicationSet = new ConfigModelPublicationSet.
                Builder(mContext, meshNode, mInternalTransportCallbacks, mStatusCallbacks).
                withAszmic(aszmic).
                withElementAddress(elementAddress).
                withPublishAddress(publishAddress).
                withAppKeyIndex(appKeyIndex).
                withModelIdentifier(modelIdentifier).
                withCredentialFlag(credentialFlag).
                withPublishTtl(publishTtl).
                withPublishPeriod(publishPeriod).
                withPublishRetransmitCount(publishRetransmitCount).
                withPublishRetransmitIntervalSteps(publishRetransmitIntervalSteps).
                build();
        configMessage = configModelPublicationSet;
        configModelPublicationSet.executeSend();
    }

    /**
     * Send App key add message to the node.
     */
    public void addSubscriptionAddress(final ProvisionedMeshNode meshNode, final int aszmic, final byte[] elementAddress, final byte[] subscriptionAddress,
                                       final int modelIdentifier) {
        final ConfigModelSubscriptionAdd configModelSubscriptionAdd = new ConfigModelSubscriptionAdd(mContext, meshNode, aszmic, elementAddress, subscriptionAddress, modelIdentifier);
        configModelSubscriptionAdd.setTransportCallbacks(mInternalTransportCallbacks);
        configModelSubscriptionAdd.setConfigurationStatusCallbacks(mStatusCallbacks);
        configMessage = configModelSubscriptionAdd;//new ConfigModelSubscriptionStatus(mContext, meshNode, ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_ADD, mInternalTransportCallbacks, mStatusCallbacks);
        configModelSubscriptionAdd.executeSend();
    }

    /**
     * Send App key add message to the node.
     */
    public void deleteSubscriptionAddress(final ProvisionedMeshNode meshNode, final int aszmic, final byte[] elementAddress, final byte[] subscriptionAddress,
                                          final int modelIdentifier) {
        final ConfigModelSubscriptionDelete configModelSubscriptionDelete = new ConfigModelSubscriptionDelete(mContext, meshNode, aszmic, elementAddress, subscriptionAddress, modelIdentifier);
        configModelSubscriptionDelete.setTransportCallbacks(mInternalTransportCallbacks);
        configModelSubscriptionDelete.setConfigurationStatusCallbacks(mStatusCallbacks);
        configMessage = configModelSubscriptionDelete;//new ConfigModelSubscriptionStatus(mContext, meshNode, ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_DELETE, mInternalTransportCallbacks, mStatusCallbacks);
        configModelSubscriptionDelete.executeSend();
    }

    /**
     * Send generic on off get to mesh node, this message sent is an acknowledged message.
     *
     * @param node        mesh node to send to
     * @param model       Mesh model to control
     * @param address     this address could be the unicast address of the element or the subscribe address
     * @param aszmic      if aszmic set to 1 the messages are encrypted with 64bit encryption otherwise 32 bit
     * @param appKeyIndex index of the app key to encrypt the message with
     */
    public void getGenericOnOff(final ProvisionedMeshNode node, final MeshModel model, final byte[] address, final boolean aszmic, final int appKeyIndex) {
        final GenericOnOffGet genericOnOffGet = new GenericOnOffGet(mContext, node, model, aszmic, address, appKeyIndex);
        genericOnOffGet.setTransportCallbacks(mInternalTransportCallbacks);
        genericOnOffGet.setConfigurationStatusCallbacks(mStatusCallbacks);
        configMessage = genericOnOffGet;
        genericOnOffGet.executeSend();
    }

    /**
     * Send generic on off set to mesh node, this message sent is an acknowledged message.
     *
     * @param node                 mesh node to send to
     * @param model                Mesh model to control
     * @param address              this address could be the unicast address of the element or the subscribe address
     * @param aszmic               if aszmic set to 1 the messages are encrypted with 64bit encryption otherwise 32 bit
     * @param appKeyIndex          index of the app key to encrypt the message with
     * @param transitionSteps      the number of steps
     * @param transitionResolution the resolution for the number of steps
     * @param delay                message execution delay in 5ms steps. After this delay milliseconds the model will execute the required behaviour.
     * @param state                on off state
     */
    public void setGenericOnOff(final ProvisionedMeshNode node, final MeshModel model, final byte[] address, final boolean aszmic, final int appKeyIndex, final Integer transitionSteps, final Integer transitionResolution, final Integer delay, final boolean state) {
        final GenericOnOffSet genericOnOffSet = new GenericOnOffSet(mContext, node, model, aszmic, address, appKeyIndex, transitionSteps, transitionResolution, delay, state);
        genericOnOffSet.setTransportCallbacks(mInternalTransportCallbacks);
        genericOnOffSet.setConfigurationStatusCallbacks(mStatusCallbacks);
        configMessage = genericOnOffSet;
        genericOnOffSet.executeSend();
    }

    /**
     * Send generic on off to mesh node
     *
     * @param node                 mesh node to send to
     * @param model                Mesh model to control
     * @param address              this address could be the unicast address of the element or the subscribe address
     * @param aszmic               if aszmic set to 1 the messages are encrypted with 64bit encryption otherwise 32 bit
     * @param appKeyIndex          index of the app key to encrypt the message with
     * @param transitionSteps      the number of steps
     * @param transitionResolution the resolution for the number of steps
     * @param delay                message execution delay in 5ms steps. After this delay milliseconds the model will execute the required behaviour.
     * @param state                on off state
     */
    public void setGenericOnOffUnacknowledged(final ProvisionedMeshNode node, final MeshModel model, final byte[] address, final boolean aszmic, final int appKeyIndex, final Integer transitionSteps, final Integer transitionResolution, final Integer delay, final boolean state) {
        final GenericOnOffSetUnacknowledged genericOnOffSetUnAcked = new GenericOnOffSetUnacknowledged(mContext, node, model, aszmic, address, appKeyIndex, transitionSteps, transitionResolution, delay, state);
        genericOnOffSetUnAcked.setTransportCallbacks(mInternalTransportCallbacks);
        genericOnOffSetUnAcked.setConfigurationStatusCallbacks(mStatusCallbacks);
        configMessage = genericOnOffSetUnAcked;
        genericOnOffSetUnAcked.executeSend();
    }


    /**
     * Resets the specific mesh node
     *
     * @param provisionedMeshNode mesh node to be reset
     */
    public void resetMeshNode(final ProvisionedMeshNode provisionedMeshNode) {
        final ConfigNodeReset configNodeReset = new ConfigNodeReset(mContext, provisionedMeshNode, false, mInternalTransportCallbacks, mStatusCallbacks);
        configMessage = configNodeReset;
        configNodeReset.executeSend();
    }
}
