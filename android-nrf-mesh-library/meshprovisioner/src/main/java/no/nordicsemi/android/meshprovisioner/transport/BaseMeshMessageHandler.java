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

package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.models.VendorModel;
import no.nordicsemi.android.meshprovisioner.utils.ConfigModelPublicationSetParams;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

public abstract class BaseMeshMessageHandler implements MeshMessageHandlerApi, InternalMeshMsgHandlerCallbacks {

    private static final String TAG = BaseMeshMessageHandler.class.getSimpleName();

    protected final Context mContext;
    protected final MeshTransport mMeshTransport;
    protected final InternalTransportCallbacks mInternalTransportCallbacks;
    protected MeshStatusCallbacks mStatusCallbacks;
    private MeshMessageState mMeshMessageState;

    protected BaseMeshMessageHandler(final Context context, final InternalTransportCallbacks internalTransportCallbacks) {
        this.mContext = context;
        this.mMeshTransport = new MeshTransport(context);
        this.mInternalTransportCallbacks = internalTransportCallbacks;
    }

    protected abstract MeshTransport getMeshTransport();

    /**
     * Handle mesh message States on write callback complete
     * <p>
     * This method will jump to the current state and switch the current state according to the message that has been sent.
     * </p>
     *
     * @param pdu mesh pdu that was sent
     */
    public final void handleMeshMsgWriteCallbacks(final byte[] pdu) {
        if (mMeshMessageState instanceof ConfigMessageState) {
            if (mMeshMessageState.getState() == null)
                return;

            switch (mMeshMessageState.getState()) {
                case COMPOSITION_DATA_GET_STATE:
                    final ConfigCompositionDataGetState compositionDataGet = (ConfigCompositionDataGetState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, compositionDataGet.getMeshMessage(), mMeshTransport, this));
                    break;
                case APP_KEY_ADD_STATE:
                    final ConfigAppKeyAddState appKeyAddState = (ConfigAppKeyAddState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, appKeyAddState.getMeshMessage(), mMeshTransport, this));
                    break;
                case CONFIG_MODEL_APP_BIND_STATE:
                    final ConfigModelAppBindState appBindState = (ConfigModelAppBindState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, appBindState.getMeshMessage(), mMeshTransport, this));
                    break;
                case CONFIG_MODEL_APP_UNBIND_STATE:
                    final ConfigModelAppUnbindState appUnbindState = (ConfigModelAppUnbindState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, appUnbindState.getMeshMessage(), mMeshTransport, this));
                    break;
                case CONFIG_MODEL_PUBLICATION_SET_STATE:
                    final ConfigModelPublicationSetState publicationSetState = (ConfigModelPublicationSetState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, publicationSetState.getMeshMessage(), mMeshTransport, this));
                    break;
                case CONFIG_MODEL_SUBSCRIPTION_ADD_STATE:
                    final ConfigModelSubscriptionAddState subscriptionAdd = (ConfigModelSubscriptionAddState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, subscriptionAdd.getMeshMessage(), mMeshTransport, this));
                    break;
                case CONFIG_MODEL_SUBSCRIPTION_DELETE_STATE:
                    final ConfigModelSubscriptionDeleteState subscriptionDelete = (ConfigModelSubscriptionDeleteState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, subscriptionDelete.getMeshMessage(), mMeshTransport, this));
                    break;
                case CONFIG_NODE_RESET_STATE:
                    final ConfigNodeResetState resetState = (ConfigNodeResetState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, resetState.getMeshMessage(), mMeshTransport, this));
                    break;
            }
        } else if (mMeshMessageState instanceof GenericMessageState) {
            switch (mMeshMessageState.getState()) {
                case GENERIC_ON_OFF_GET_STATE:
                    final GenericOnOffGetState onOffGetState = (GenericOnOffGetState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, onOffGetState.getMeshMessage(), mMeshTransport, this));
                    break;
                case GENERIC_ON_OFF_SET_STATE:
                    final GenericOnOffSetState onOffSetState = (GenericOnOffSetState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, onOffSetState.getMeshMessage(), mMeshTransport, this));
                    break;
                case GENERIC_ON_OFF_SET_UNACKNOWLEDGED_STATE:
                    final GenericOnOffSetUnacknowledgedState onOffSetUnacknowledgedState = (GenericOnOffSetUnacknowledgedState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, onOffSetUnacknowledgedState.getMeshMessage(), mMeshTransport, this));
                    break;
                case GENERIC_LEVEL_GET_STATE:
                    final GenericLevelGetState levelGetState = (GenericLevelGetState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, levelGetState.getMeshMessage(), mMeshTransport, this));
                    break;
                case GENERIC_LEVEL_SET_STATE:
                    final GenericLevelSetState levelSetState = (GenericLevelSetState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, levelSetState.getMeshMessage(), mMeshTransport, this));
                    break;
                case GENERIC_LEVEL_SET_UNACKNOWLEDGED_STATE:
                    final GenericLevelSetUnacknowledgedState levelSetUnacknowledgedState = (GenericLevelSetUnacknowledgedState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, levelSetUnacknowledgedState.getMeshMessage(), mMeshTransport, this));
                    break;
                case VENDOR_MODEL_ACKNOWLEDGED_STATE:
                    final VendorModelMessageAckedState vendorModelMessageAckedState = (VendorModelMessageAckedState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, vendorModelMessageAckedState.getMeshMessage(), mMeshTransport, this));
                    break;
                case VENDOR_MODEL_UNACKNOWLEDGED_STATE:
                    final VendorModelMessageUnackedState vendorModelMessageUnackedState = (VendorModelMessageUnackedState) mMeshMessageState;
                    switchToNoOperationState(new DefaultNoOperationMessageState(mContext, vendorModelMessageUnackedState.getMeshMessage(), mMeshTransport, this));
                    break;
            }
        }
    }

    /**
     * Handle mesh States on receiving mesh message notifications
     * <p>
     * This method will jump to the current state and switch the state depending on the expected and the next message received.
     * </p>
     *
     * @param pdu mesh pdu that was sent
     */
    public final void parseMeshMsgNotifications(final byte[] pdu) {
        if (mMeshMessageState instanceof DefaultNoOperationMessageState) {
            ((DefaultNoOperationMessageState) mMeshMessageState).parseMeshPdu(pdu);
        }
    }

    @Override
    public final void onIncompleteTimerExpired(final boolean incompleteTimerExpired) {
        //We switch no operation state if the incomplete timer has expired so that we don't wait on the same state if a particular message fails.
        final MeshMessage meshMessage = mMeshMessageState.getMeshMessage();
        switchToNoOperationState(new DefaultNoOperationMessageState(mContext, meshMessage, mMeshTransport, this));
    }

    /**
     * Switch the current state of the mesh message handler
     * <p>
     * This method will switch the current state of the mesh message handler
     * </p>
     *
     * @param newState new state that is to be switched to
     * @return true if the state was switched successfully
     */
    private void switchToNoOperationState(final MeshMessageState newState) {
        //Switching to unknown message state here for messages that are not
        if (mMeshMessageState.getState() != null && newState.getState() != null) {
            Log.v(TAG, "Switching current state " + mMeshMessageState.getState().name() + " to No operation state");
        } else {
            Log.v(TAG, "Switched to No operation state");
        }
        newState.setTransportCallbacks(mInternalTransportCallbacks);
        newState.setStatusCallbacks(mStatusCallbacks);
        mMeshMessageState = newState;
    }
    /**
     * @deprecated Use {@link MeshMessageHandlerApi#sendMeshMessage(MeshMessage)} instead.
     */
    @Deprecated
    @Override
    public final void sendCompositionDataGet(@NonNull final ProvisionedMeshNode meshNode, final int aszmic) {
        final ConfigCompositionDataGet compositionDataGet = new ConfigCompositionDataGet(meshNode, aszmic);
        sendMeshMessage(compositionDataGet);
    }

    /**
     * @deprecated Use {@link MeshMessageHandlerApi#sendMeshMessage(MeshMessage)} instead.
     */
    @Deprecated
    @Override
    public final void sendAppKeyAdd(@NonNull final ProvisionedMeshNode meshNode, final int appKeyIndex, @NonNull final String appKey, final int aszmic) {
        final ConfigAppKeyAdd configAppKeyAdd = new ConfigAppKeyAdd(meshNode, MeshParserUtils.toByteArray(appKey), appKeyIndex, aszmic);
        sendMeshMessage(configAppKeyAdd);
    }

    /**
     * @deprecated Use {@link MeshMessageHandlerApi#sendMeshMessage(MeshMessage)} instead.
     */
    @Deprecated
    @Override
    public final void bindAppKey(@NonNull final ProvisionedMeshNode meshNode, final int aszmic,
                                 @NonNull final byte[] elementAddress, final int modelIdentifier, final int appKeyIndex) {
        final ConfigModelAppBind configModelAppBind = new ConfigModelAppBind(meshNode, elementAddress, modelIdentifier, appKeyIndex, aszmic);
        sendMeshMessage(configModelAppBind);
    }

    /**
     * @deprecated Use {@link MeshMessageHandlerApi#sendMeshMessage(MeshMessage)} instead.
     */
    @Deprecated
    @Override
    public void unbindAppKey(@NonNull final ProvisionedMeshNode meshNode, final int aszmic,
                             @NonNull final byte[] elementAddress, final int modelIdentifier, final int appKeyIndex) {
        final ConfigModelAppUnbind configModelAppUnbind = new ConfigModelAppUnbind(meshNode, elementAddress, modelIdentifier, appKeyIndex, aszmic);
        sendMeshMessage(configModelAppUnbind);
    }

    /**
     * @deprecated Use {@link MeshMessageHandlerApi#sendMeshMessage(MeshMessage)} instead.
     */
    @Deprecated
    @Override
    public final void sendConfigModelPublicationSet(@NonNull final ConfigModelPublicationSetParams params) {
        final ConfigModelPublicationSet configModelPublicationSet = new ConfigModelPublicationSet(params.getMeshNode(),
                params.getElementAddress(), params.getPublishAddress(), params.getAppKeyIndex(), params.getCredentialFlag(),
                params.getPublishTtl(), params.getPublicationSteps(), params.getPublicationResolution(),
                params.getPublishRetransmitCount(), params.getPublishRetransmitIntervalSteps(), params.getModelIdentifier(), params.getAszmic());

        sendMeshMessage(configModelPublicationSet);
    }

    /**
     * @deprecated Use {@link MeshMessageHandlerApi#sendMeshMessage(MeshMessage)} instead.
     */
    @Deprecated
    @Override
    public final void addSubscriptionAddress(@NonNull final ProvisionedMeshNode meshNode, final int aszmic, @NonNull final byte[] elementAddress, @NonNull final byte[] subscriptionAddress,
                                             final int modelIdentifier) {
        final ConfigModelSubscriptionAdd configModelSubscriptionAdd = new ConfigModelSubscriptionAdd(meshNode, elementAddress, subscriptionAddress, modelIdentifier, aszmic);
        sendMeshMessage(configModelSubscriptionAdd);
    }

    /**
     * @deprecated Use {@link MeshMessageHandlerApi#sendMeshMessage(MeshMessage)} instead.
     */
    @Deprecated
    @Override
    public final void deleteSubscriptionAddress(@NonNull final ProvisionedMeshNode meshNode, final int aszmic, @NonNull final byte[] elementAddress, @NonNull final byte[] subscriptionAddress,
                                                final int modelIdentifier) {
        final ConfigModelSubscriptionDelete configModelSubscriptionDelete = new ConfigModelSubscriptionDelete(meshNode, elementAddress, subscriptionAddress, modelIdentifier, aszmic);
        sendMeshMessage(configModelSubscriptionDelete);
    }

    @Deprecated
    @Override
    public final void resetMeshNode(@NonNull final ProvisionedMeshNode provisionedMeshNode) {
        final ConfigNodeReset configNodeReset = new ConfigNodeReset(provisionedMeshNode, 0);
        sendMeshMessage(configNodeReset);
    }

    @Deprecated
    @Override
    public final void getGenericOnOff(@NonNull final ProvisionedMeshNode node, @NonNull final MeshModel model, @NonNull final byte[] dstAddress, final boolean aszmic, final int appKeyIndex) throws IllegalArgumentException {
        if (model.getBoundAppkeys().isEmpty())
            throw new IllegalArgumentException("There are no app keys bound to this model");

        final byte[] appKey = MeshParserUtils.toByteArray(model.getBoundAppKey(appKeyIndex));
        final GenericOnOffGet genericOnOffGet = new GenericOnOffGet(node, appKey, aszmic ? 1 : 0);
        final GenericOnOffGetState genericOnOffGetState = new GenericOnOffGetState(mContext, dstAddress, genericOnOffGet, mMeshTransport, this);
        genericOnOffGetState.setTransportCallbacks(mInternalTransportCallbacks);
        genericOnOffGetState.setStatusCallbacks(mStatusCallbacks);
        mMeshMessageState = genericOnOffGetState;
        genericOnOffGetState.executeSend();
    }

    @Deprecated
    public final void getGenericOnOff(@NonNull final byte[] dstAddress, @NonNull final GenericOnOffGet genericOnOffGet) {
        final GenericOnOffGetState genericOnOffGetState = new GenericOnOffGetState(mContext, dstAddress, genericOnOffGet, mMeshTransport, this);
        genericOnOffGetState.setTransportCallbacks(mInternalTransportCallbacks);
        genericOnOffGetState.setStatusCallbacks(mStatusCallbacks);
        mMeshMessageState = genericOnOffGetState;
        genericOnOffGetState.executeSend();
    }

    @Deprecated
    @Override
    public final void setGenericOnOff(@NonNull final ProvisionedMeshNode node, @NonNull final MeshModel model,
                                      @NonNull final byte[] dstAddress, final boolean aszmic, final int appKeyIndex,
                                      final Integer transitionSteps, final Integer transitionResolution, final Integer delay, final boolean state) throws IllegalArgumentException {
        if (model.getBoundAppkeys().isEmpty())
            throw new IllegalArgumentException("There are no app keys bound to this model");

        final byte[] appKey = MeshParserUtils.toByteArray(model.getBoundAppKey(appKeyIndex));

        final GenericOnOffSet genericOnOffSet = new GenericOnOffSet(node, appKey, state, transitionSteps, transitionResolution, delay, aszmic ? 1 : 0);
        final GenericOnOffSetState genericOnOffSetState = new GenericOnOffSetState(mContext, dstAddress, genericOnOffSet, mMeshTransport, this);
        genericOnOffSetState.setTransportCallbacks(mInternalTransportCallbacks);
        genericOnOffSetState.setStatusCallbacks(mStatusCallbacks);
        mMeshMessageState = genericOnOffSetState;
        genericOnOffSetState.executeSend();
    }

    @Deprecated
    @Override
    public final void setGenericOnOff(final byte[] dstAddress, final GenericOnOffSet genericOnOffSet) throws IllegalArgumentException {
        final GenericOnOffSetState genericOnOffSetState = new GenericOnOffSetState(mContext, dstAddress, genericOnOffSet, mMeshTransport, this);
        genericOnOffSetState.setTransportCallbacks(mInternalTransportCallbacks);
        genericOnOffSetState.setStatusCallbacks(mStatusCallbacks);
        mMeshMessageState = genericOnOffSetState;
        genericOnOffSetState.executeSend();
    }

    @Deprecated
    @Override
    public final void setGenericOnOffUnacknowledged(@NonNull final ProvisionedMeshNode node, @NonNull final MeshModel model,
                                                    @NonNull final byte[] dstAddress, final boolean aszmic, final int appKeyIndex,
                                                    @NonNull final Integer transitionSteps, @NonNull final Integer transitionResolution,
                                                    @NonNull final Integer delay, final boolean state) throws IllegalArgumentException {
        if (model.getBoundAppkeys().isEmpty())
            throw new IllegalArgumentException("There are no app keys bound to this model");

        final byte[] appKey = MeshParserUtils.toByteArray(model.getBoundAppKey(appKeyIndex));
        final GenericOnOffSetUnacknowledged genericOnOffSetUnacked = new GenericOnOffSetUnacknowledged(node, appKey, state, transitionSteps, transitionResolution, delay, aszmic ? 1 : 0);
        setGenericOnOffUnacknowledged(dstAddress, genericOnOffSetUnacked);
    }

    @Deprecated
    @Override
    public final void setGenericOnOffUnacknowledged(@NonNull final byte[] dstAddress, @NonNull final GenericOnOffSetUnacknowledged genericOnOffSetUnacked) {
        final GenericOnOffSetUnacknowledgedState genericOnOffSetUnAckedState = new GenericOnOffSetUnacknowledgedState(mContext, dstAddress, genericOnOffSetUnacked, mMeshTransport, this);
        genericOnOffSetUnAckedState.setTransportCallbacks(mInternalTransportCallbacks);
        genericOnOffSetUnAckedState.setStatusCallbacks(mStatusCallbacks);
        mMeshMessageState = genericOnOffSetUnAckedState;
        genericOnOffSetUnAckedState.executeSend();
    }

    @Deprecated
    @Override
    public final void getGenericLevel(@NonNull final ProvisionedMeshNode node, @NonNull final MeshModel model, @NonNull final byte[] dstAddress, final boolean aszmic, final int appKeyIndex) {
        if (model.getBoundAppkeys().isEmpty())
            throw new IllegalArgumentException("There are no app keys bound to this model");

        final byte[] appKey = MeshParserUtils.toByteArray(model.getBoundAppKey(appKeyIndex));

        final GenericLevelGet genericLevelGet = new GenericLevelGet(node, appKey, aszmic ? 1 : 0);
        getGenericLevel(dstAddress, genericLevelGet);
    }

    @Deprecated
    @Override
    public final void getGenericLevel(@NonNull final byte[] dstAddress, @NonNull final GenericLevelGet genericLevelGet) {
        final GenericLevelGetState genericLevelGetState = new GenericLevelGetState(mContext, dstAddress, genericLevelGet, mMeshTransport, this);
        genericLevelGetState.setTransportCallbacks(mInternalTransportCallbacks);
        genericLevelGetState.setStatusCallbacks(mStatusCallbacks);
        mMeshMessageState = genericLevelGetState;
        genericLevelGetState.executeSend();
    }

    @Deprecated
    @Override
    public final void setGenericLevel(@NonNull final ProvisionedMeshNode node, @NonNull final MeshModel model, @NonNull final byte[] dstAddress,
                                      final boolean aszmic, final int appKeyIndex, final Integer transitionSteps,
                                      final Integer transitionResolution, final Integer delay, final int level) throws IllegalArgumentException {
        if (model.getBoundAppkeys().isEmpty())
            throw new IllegalArgumentException("There are no app keys bound to this model");

        final byte[] appKey = MeshParserUtils.toByteArray(model.getBoundAppKey(appKeyIndex));
        final GenericLevelSet genericLevelSet = new GenericLevelSet(node, appKey, transitionSteps, transitionResolution, delay, level, aszmic ? 1 : 0);
        setGenericLevel(dstAddress, genericLevelSet);
    }

    @Deprecated
    @Override
    public final void setGenericLevel(@NonNull final byte[] dstAddress, @NonNull final GenericLevelSet genericLevelSet) {
        final GenericLevelSetState genericLevelSetState = new GenericLevelSetState(mContext, dstAddress, genericLevelSet, mMeshTransport, this);
        genericLevelSetState.setTransportCallbacks(mInternalTransportCallbacks);
        genericLevelSetState.setStatusCallbacks(mStatusCallbacks);
        mMeshMessageState = genericLevelSetState;
        genericLevelSetState.executeSend();
    }

    @Deprecated
    @Override
    public final void setGenericLevelUnacknowledged(@NonNull final ProvisionedMeshNode node, @NonNull final MeshModel model, @NonNull final byte[] dstAddress,
                                                    final boolean aszmic, final int appKeyIndex, final Integer transitionSteps,
                                                    final Integer transitionResolution, final Integer delay, final int level) throws IllegalArgumentException {
        if (model.getBoundAppkeys().isEmpty())
            throw new IllegalArgumentException("There are no app keys bound to this model");

        final byte[] appKey = MeshParserUtils.toByteArray(model.getBoundAppKey(appKeyIndex));
        final GenericLevelSetUnacknowledged genericLevelSetUnacked = new GenericLevelSetUnacknowledged(node, appKey, transitionSteps, transitionResolution, delay, level, aszmic ? 1 : 0);
        setGenericLevelUnacknowledged(dstAddress, genericLevelSetUnacked);
    }

    @Deprecated
    @Override
    public final void setGenericLevelUnacknowledged(@NonNull final byte[] dstAddress, @NonNull final GenericLevelSetUnacknowledged genericLevelSetUnacked) {
        final GenericLevelSetUnacknowledgedState genericLevelSetUnAckedState = new GenericLevelSetUnacknowledgedState(mContext, dstAddress, genericLevelSetUnacked, mMeshTransport, this);
        genericLevelSetUnAckedState.setTransportCallbacks(mInternalTransportCallbacks);
        genericLevelSetUnAckedState.setStatusCallbacks(mStatusCallbacks);
        mMeshMessageState = genericLevelSetUnAckedState;
        genericLevelSetUnAckedState.executeSend();
    }

    @Deprecated
    @Override
    public void sendVendorModelUnacknowledgedMessage(@NonNull final ProvisionedMeshNode node, @NonNull final VendorModel model, @NonNull final byte[] dstAddress, final boolean aszmic, final int appKeyIndex, final int opcode, final byte[] parameters) {
        if (model.getBoundAppkeys().isEmpty())
            throw new IllegalArgumentException("There are no app keys bound to this model");

        final byte[] appKey = MeshParserUtils.toByteArray(model.getBoundAppKey(appKeyIndex));
        final int modelId = model.getModelId();
        final int companyIdentifier = model.getCompanyIdentifier();
        final VendorModelMessageUnacked vendorModelMessageUnacked = new VendorModelMessageUnacked(node, appKey, modelId, companyIdentifier, opcode, parameters, aszmic ? 1 : 0);
        sendVendorModelUnacknowledgedMessage(dstAddress, vendorModelMessageUnacked);
    }

    @Deprecated
    @Override
    public void sendVendorModelUnacknowledgedMessage(@NonNull final byte[] dstAddress, @NonNull final VendorModelMessageUnacked vendorModelMessageUnacked) {
        final VendorModelMessageUnackedState vendorModelMessageUnackedState = new VendorModelMessageUnackedState(mContext, dstAddress, vendorModelMessageUnacked, mMeshTransport, this);
        vendorModelMessageUnackedState.setTransportCallbacks(mInternalTransportCallbacks);
        vendorModelMessageUnackedState.setStatusCallbacks(mStatusCallbacks);
        mMeshMessageState = vendorModelMessageUnackedState;
        vendorModelMessageUnackedState.executeSend();
    }

    @Deprecated
    @Override
    public final void sendVendorModelAcknowledgedMessage(@NonNull final ProvisionedMeshNode node, @NonNull final VendorModel model, @NonNull final byte[] dstAddress, final boolean aszmic, final int appKeyIndex, final int opcode, final byte[] parameters) {
        if (model.getBoundAppkeys().isEmpty())
            throw new IllegalArgumentException("There are no app keys bound to this model");

        final byte[] appKey = MeshParserUtils.toByteArray(model.getBoundAppKey(appKeyIndex));
        final int modelId = model.getModelId();
        final int companyIdentifier = model.getCompanyIdentifier();
        final VendorModelMessageAcked vendorModelMessageAcked = new VendorModelMessageAcked(node, appKey, modelId, companyIdentifier, opcode, parameters, aszmic ? 1 : 0);
        sendVendorModelAcknowledgedMessage(dstAddress, vendorModelMessageAcked);
    }

    @Deprecated
    @Override
    public final void sendVendorModelAcknowledgedMessage(@NonNull final byte[] dstAddress, @NonNull final VendorModelMessageAcked vendorModelMessageAcked) {
        final VendorModelMessageAckedState message = new VendorModelMessageAckedState(mContext, dstAddress, vendorModelMessageAcked, mMeshTransport, this);
        message.setTransportCallbacks(mInternalTransportCallbacks);
        message.setStatusCallbacks(mStatusCallbacks);
        mMeshMessageState = message;
        message.executeSend();
    }

    @Override
    public final void sendMeshMessage(@NonNull final MeshMessage configurationMessage) {
        sendConfigMessage(configurationMessage);
    }

    @Override
    public final void sendMeshMessage(@NonNull final byte[] dstAddress, @NonNull final MeshMessage genericMessage) {
        sendGenericMessage(dstAddress, genericMessage);
    }

    private final void sendConfigMessage(@NonNull final MeshMessage configMessage) {
        if (configMessage instanceof ConfigCompositionDataGet) {
            final ConfigCompositionDataGetState compositionDataGetState = new ConfigCompositionDataGetState(mContext, (ConfigCompositionDataGet) configMessage, mMeshTransport, this);
            compositionDataGetState.setTransportCallbacks(mInternalTransportCallbacks);
            compositionDataGetState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = compositionDataGetState;
            compositionDataGetState.executeSend();
        } else if (configMessage instanceof ConfigAppKeyAdd) {
            final ConfigAppKeyAddState configAppKeyAddState = new ConfigAppKeyAddState(mContext, (ConfigAppKeyAdd) configMessage, mMeshTransport, this);
            configAppKeyAddState.setTransportCallbacks(mInternalTransportCallbacks);
            configAppKeyAddState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = configAppKeyAddState;
            configAppKeyAddState.executeSend();
        } else if (configMessage instanceof ConfigModelAppBind) {
            final ConfigModelAppBindState configModelAppBindState = new ConfigModelAppBindState(mContext, (ConfigModelAppBind) configMessage, mMeshTransport, this);
            configModelAppBindState.setTransportCallbacks(mInternalTransportCallbacks);
            configModelAppBindState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = configModelAppBindState;
            configModelAppBindState.executeSend();
        } else if (configMessage instanceof ConfigModelAppUnbind) {
            final ConfigModelAppUnbindState modelAppUnbindState = new ConfigModelAppUnbindState(mContext, (ConfigModelAppUnbind) configMessage, mMeshTransport, this);
            modelAppUnbindState.setTransportCallbacks(mInternalTransportCallbacks);
            modelAppUnbindState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = modelAppUnbindState;
            modelAppUnbindState.executeSend();
        } else if (configMessage instanceof ConfigModelPublicationSet) {
            final ConfigModelPublicationSetState configModelPublicationSetState = new ConfigModelPublicationSetState(mContext, (ConfigModelPublicationSet) configMessage,
                    mMeshTransport, this);
            configModelPublicationSetState.setTransportCallbacks(mInternalTransportCallbacks);
            configModelPublicationSetState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = configModelPublicationSetState;
            configModelPublicationSetState.executeSend();
        } else if (configMessage instanceof ConfigModelSubscriptionAdd) {
            final ConfigModelSubscriptionAddState configModelSubscriptionAddState = new ConfigModelSubscriptionAddState(mContext, (ConfigModelSubscriptionAdd) configMessage,
                    mMeshTransport, this);
            configModelSubscriptionAddState.setTransportCallbacks(mInternalTransportCallbacks);
            configModelSubscriptionAddState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = configModelSubscriptionAddState;
            configModelSubscriptionAddState.executeSend();
        } else if (configMessage instanceof ConfigModelSubscriptionDelete) {
            final ConfigModelSubscriptionDeleteState configModelSubscriptionDeleteState = new ConfigModelSubscriptionDeleteState(mContext, (ConfigModelSubscriptionDelete) configMessage,
                    mMeshTransport, this);
            configModelSubscriptionDeleteState.setTransportCallbacks(mInternalTransportCallbacks);
            configModelSubscriptionDeleteState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = configModelSubscriptionDeleteState;
            configModelSubscriptionDeleteState.executeSend();
        } else if (configMessage instanceof ConfigNodeReset) {
            final ConfigNodeResetState configNodeResetState = new ConfigNodeResetState(mContext, (ConfigNodeReset) configMessage, mMeshTransport, this);
            configNodeResetState.setTransportCallbacks(mInternalTransportCallbacks);
            configNodeResetState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = configNodeResetState;
            configNodeResetState.executeSend();
        }
    }

    private final void sendGenericMessage(@NonNull final byte[] dstAddress, @NonNull final MeshMessage genericMessage) {
        if (genericMessage instanceof GenericOnOffGet) {
            final GenericOnOffGetState genericOnOffGetState = new GenericOnOffGetState(mContext, dstAddress, (GenericOnOffGet) genericMessage, mMeshTransport, this);
            genericOnOffGetState.setTransportCallbacks(mInternalTransportCallbacks);
            genericOnOffGetState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = genericOnOffGetState;
            genericOnOffGetState.executeSend();
        } else if (genericMessage instanceof GenericOnOffSet) {
            final GenericOnOffSetState genericOnOffSetState = new GenericOnOffSetState(mContext, dstAddress, (GenericOnOffSet) genericMessage, mMeshTransport, this);
            genericOnOffSetState.setTransportCallbacks(mInternalTransportCallbacks);
            genericOnOffSetState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = genericOnOffSetState;
            genericOnOffSetState.executeSend();
        } else if (genericMessage instanceof GenericOnOffSetUnacknowledged) {
            final GenericOnOffSetUnacknowledgedState genericOnOffSetUnAckedState = new GenericOnOffSetUnacknowledgedState(mContext, dstAddress, (GenericOnOffSetUnacknowledged) genericMessage, mMeshTransport, this);
            genericOnOffSetUnAckedState.setTransportCallbacks(mInternalTransportCallbacks);
            genericOnOffSetUnAckedState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = genericOnOffSetUnAckedState;
            genericOnOffSetUnAckedState.executeSend();
        } else if (genericMessage instanceof GenericLevelGet) {
            final GenericLevelGetState genericLevelGetState = new GenericLevelGetState(mContext, dstAddress, (GenericLevelGet) genericMessage, mMeshTransport, this);
            genericLevelGetState.setTransportCallbacks(mInternalTransportCallbacks);
            genericLevelGetState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = genericLevelGetState;
            genericLevelGetState.executeSend();
        } else if (genericMessage instanceof GenericLevelSet) {
            final GenericLevelSetState genericLevelSetState = new GenericLevelSetState(mContext, dstAddress, (GenericLevelSet) genericMessage, mMeshTransport, this);
            genericLevelSetState.setTransportCallbacks(mInternalTransportCallbacks);
            genericLevelSetState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = genericLevelSetState;
            genericLevelSetState.executeSend();
        } else if (genericMessage instanceof GenericLevelSetUnacknowledged) {
            final GenericLevelSetUnacknowledgedState genericLevelSetUnAckedState = new GenericLevelSetUnacknowledgedState(mContext, dstAddress,
                    (GenericLevelSetUnacknowledged) genericMessage, mMeshTransport, this);
            genericLevelSetUnAckedState.setTransportCallbacks(mInternalTransportCallbacks);
            genericLevelSetUnAckedState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = genericLevelSetUnAckedState;
            genericLevelSetUnAckedState.executeSend();
        } else if (genericMessage instanceof VendorModelMessageAcked) {
            final VendorModelMessageAckedState message = new VendorModelMessageAckedState(mContext, dstAddress, (VendorModelMessageAcked) genericMessage, mMeshTransport, this);
            message.setTransportCallbacks(mInternalTransportCallbacks);
            message.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = message;
            message.executeSend();
        } else if (genericMessage instanceof VendorModelMessageUnacked) {
            final VendorModelMessageUnackedState vendorModelMessageUnackedState = new VendorModelMessageUnackedState(mContext,
                    dstAddress, (VendorModelMessageUnacked) genericMessage, mMeshTransport, this);
            vendorModelMessageUnackedState.setTransportCallbacks(mInternalTransportCallbacks);
            vendorModelMessageUnackedState.setStatusCallbacks(mStatusCallbacks);
            mMeshMessageState = vendorModelMessageUnackedState;
            vendorModelMessageUnackedState.executeSend();
        } else {

        }
    }
}
