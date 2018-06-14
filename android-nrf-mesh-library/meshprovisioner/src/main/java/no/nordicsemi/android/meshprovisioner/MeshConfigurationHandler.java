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

import no.nordicsemi.android.meshprovisioner.configuration.ConfigAppKeyAdd;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigAppKeyStatus;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigCompositionDataGet;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigCompositionDataStatus;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigMessage;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigModelAppBind;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigModelAppStatus;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigModelPublicationSet;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigModelPublicationStatus;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigModelSubscriptionAdd;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigModelSubscriptionDelete;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigModelSubscriptionStatus;
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

    protected void handleConfigurationWriteCallbacks(final ProvisionedMeshNode meshNode, final byte[] pdu) {
        switch (configMessage.getState()) {
            case COMPOSITION_DATA_GET:
                //Composition data get complete,
                //We directly switch to next state because there is no acknowledgements involved
                //configMessage = new ConfigCompositionDataStatus(mContext, mMeshNode, mInternalTransportCallbacks, mStatusCallbacks);
                break;
            case APP_KEY_ADD:
                break;
            case APP_KEY_STATUS:
                //TODO check app key status block ack
                //Block ack for app key status sent
                break;
            case CONFIG_MODEL_APP_BIND:
                final ConfigModelAppBind appBind = (ConfigModelAppBind) configMessage;
                configMessage = new ConfigModelAppStatus(mContext, meshNode, mInternalTransportCallbacks, mStatusCallbacks);
                break;
            case CONFIG_MODEL_APP_STATUS:
                break;
            case CONFIG_MODEL_PUBLICATION_SET:
                break;
            case CONFIG_MODEL_SUBSCRIPTION_ADD:
                break;
        }
    }

    protected void parseConfigurationNotifications(final ProvisionedMeshNode meshNode, final byte[] pdu) {
        switch (configMessage.getState()) {
            case COMPOSITION_DATA_STATUS:
                final ConfigCompositionDataStatus compositionDataStatus = (ConfigCompositionDataStatus) configMessage;
                compositionDataStatus.parseData(pdu);
                mInternalMeshManagerCallbacks.onUnicastAddressChanged(compositionDataStatus.getUnicastAddress());
                break;
            case APP_KEY_ADD:
                //TODO check for acknowledged message if the peer has received everything.
                // Since we don't check right now the app may try to decrypt the appkey status message in the app key add message causing the decryption to fail
                // but that does not matter here because when the node retransmits we have changed the status to appkeystatus and it will parse the status message
                final ConfigAppKeyAdd configAppKeyAdd = ((ConfigAppKeyAdd) configMessage);
                configAppKeyAdd.parseData(pdu);

                //App key add block ack received, switch to next app key add status state.
                configMessage = new ConfigAppKeyStatus(mContext, meshNode, configAppKeyAdd.getSrc(), configAppKeyAdd.getAppKey(), mInternalTransportCallbacks, mStatusCallbacks);
                break;
            case APP_KEY_STATUS:
                ((ConfigAppKeyStatus) configMessage).parseData(pdu);
                break;
            case CONFIG_MODEL_APP_BIND:
                final ConfigModelAppBind configModelAppBind = ((ConfigModelAppBind) configMessage);
                configModelAppBind.parseData(pdu);
                //publication set block ack received, switch to next state.
                configMessage = new ConfigModelAppStatus(mContext, meshNode, mInternalTransportCallbacks, mStatusCallbacks);
            case CONFIG_MODEL_APP_STATUS:
                ((ConfigModelAppStatus) configMessage).parseData(pdu);
                break;
            case CONFIG_MODEL_PUBLICATION_SET:
                final ConfigModelPublicationSet configModelPublicationSet = ((ConfigModelPublicationSet) configMessage);
                configModelPublicationSet.parseData(pdu);
                //TODO check for acknowledged message if the peer has received everything
                //publication set block ack received, switch to next state.
                configMessage = new ConfigModelPublicationStatus(mContext, meshNode, mInternalTransportCallbacks, mStatusCallbacks);
                break;
            case CONFIG_MODEL_PUBLICATION_STATUS:
                final ConfigModelPublicationStatus configModelPublicationStatus = (ConfigModelPublicationStatus) configMessage;
                configModelPublicationStatus.parseData(pdu);
                break;
            case CONFIG_MODEL_SUBSCRIPTION_ADD:
                //TODO check for acknowledged message if the peer has received everything
                //subscription add block ack received, switch to next state.
                //configMessage = new ConfigModelSubscriptionStatus(mContext, meshNode, ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_ADD, mInternalTransportCallbacks, mStatusCallbacks);
                break;
            case CONFIG_MODEL_SUBSCRIPTION_DELETE:
                break;
            case CONFIG_MODEL_SUBSCRIPTION_STATUS:
                final ConfigModelSubscriptionStatus configModelSubscriptionStatus = (ConfigModelSubscriptionStatus) configMessage;
                configModelSubscriptionStatus.parseData(pdu);
                break;
        }
    }

    public ConfigMessage.ConfigMessageState getConfigurationState() {
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
        compositionDataGet.executeSend();
        configMessage = new ConfigCompositionDataStatus(mContext, meshNode, mInternalTransportCallbacks, mStatusCallbacks);
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
     *  @param meshNode        mesh node containing the model
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
     * Set a publish address for configuration model
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
        configModelSubscriptionAdd.executeSend();
        configMessage = new ConfigModelSubscriptionStatus(mContext, meshNode, ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_ADD, mInternalTransportCallbacks, mStatusCallbacks);
    }

    /**
     * Send App key add message to the node.
     */
    public void deleteSubscriptionAddress(final ProvisionedMeshNode meshNode, final int aszmic, final byte[] elementAddress, final byte[] subscriptionAddress,
                                          final int modelIdentifier) {
        final ConfigModelSubscriptionDelete configModelSubscriptionDelete = new ConfigModelSubscriptionDelete(mContext, meshNode, aszmic, elementAddress, subscriptionAddress, modelIdentifier);
        configModelSubscriptionDelete.setTransportCallbacks(mInternalTransportCallbacks);
        configModelSubscriptionDelete.setConfigurationStatusCallbacks(mStatusCallbacks);
        configModelSubscriptionDelete.executeSend();
        configMessage = new ConfigModelSubscriptionStatus(mContext, meshNode, ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_DELETE, mInternalTransportCallbacks, mStatusCallbacks);
    }
}
