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

package no.nordicsemi.android.nrfmeshprovisioner.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.CompositionDataParser;
import no.nordicsemi.android.meshprovisioner.utils.Element;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ExtendedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.MeshNodeStates;

import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.ACTION_GENERIC_ON_OFF_STATE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_APP_KEY_INDEX;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_CONFIGURATION_STATE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_ELEMENT_ADDRESS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_GENERIC_ON_OFF_PRESENT_STATE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_IS_SUCCESS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_MODEL_ID;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_PUBLISH_ADDRESS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_STATUS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_SUBSCRIPTION_ADDRESS;

public class ModelConfigurationRepository extends BaseMeshRepository {

    private static final String TAG = ModelConfigurationRepository.class.getSimpleName();
    private MutableLiveData<Boolean> mPresentState = new MutableLiveData<>();

    public ModelConfigurationRepository(final Context context) {
        super(context);
    }

    public LiveData<Boolean> getGenericOnOffState() {
        return mPresentState;
    }

    public LiveData<Boolean> isConnected() {
        return mIsConnected;
    }

    @Override
    public void onConnectionStateChanged(final String connectionState) {

    }

    @Override
    public void isDeviceConnected(final boolean isConnected) {

    }

    @Override
    public void onDeviceReady(final boolean isReady) {

    }

    @Override
    public void isReconnecting(final boolean isReconnecting) {

    }

    @Override
    public void onProvisioningStateChanged(final Intent intent) {

    }

    @Override
    public void onConfigurationStateChanged(final Intent intent) {
        handleConfigurationStates(intent);
    }

    @Override
    protected void onGenericOnOfStateReceived(final Intent intent) {
        super.onGenericOnOfStateReceived(intent);
        handleGenericOnOffState(intent);
    }

    private void handleConfigurationStates(final Intent intent) {
        final int state = intent.getExtras().getInt(EXTRA_CONFIGURATION_STATE);
        final MeshNodeStates.MeshNodeStatus status = MeshNodeStates.MeshNodeStatus.fromStatusCode(state);
        final ProvisionedMeshNode node = mBinder.getMeshNode();
        final MeshModel model = mBinder.getMeshModel();
        switch (status) {
            case COMPOSITION_DATA_GET_SENT:
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, state);
                mExtendedMeshNode = new ExtendedMeshNode(node);
                break;
            case COMPOSITION_DATA_STATUS_RECEIVED:
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, state);
                mExtendedMeshNode.updateMeshNode(node);
                break;
            case SENDING_BLOCK_ACKNOWLEDGEMENT:
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, state);
                mExtendedMeshNode.updateMeshNode(node);
                break;
            case BLOCK_ACKNOWLEDGEMENT_RECEIVED:
                mExtendedMeshNode.updateMeshNode(node);
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, state);
                break;
            case SENDING_APP_KEY_ADD:
                mExtendedMeshNode.updateMeshNode(node);
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, state);
                break;
            case APP_KEY_STATUS_RECEIVED:
                if (intent.getExtras() != null) {
                    final int statusCode = intent.getExtras().getInt(EXTRA_STATUS);
                    mExtendedMeshNode.updateMeshNode(node);
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, state, statusCode);
                }
                break;
            case APP_BIND_SENT:
                break;
            case APP_BIND_STATUS_RECEIVED:
                if (intent.getExtras() != null) {
                    final boolean success = intent.getExtras().getBoolean(EXTRA_IS_SUCCESS);
                    final int statusCode = intent.getExtras().getInt(EXTRA_STATUS);
                    final int elementAddress = intent.getExtras().getInt(EXTRA_ELEMENT_ADDRESS);
                    final int appKeyIndex = intent.getExtras().getInt(EXTRA_APP_KEY_INDEX);
                    final int modelId = intent.getExtras().getInt(EXTRA_MODEL_ID);
                    mExtendedMeshNode.updateMeshNode(node);
                    mMeshModel.postValue(model);
                    mAppKeyBindStatus.onStatusChanged(success, statusCode, elementAddress, appKeyIndex, modelId);
                }
                break;
            case PUBLISH_ADDRESS_SET_SENT:
                break;
            case PUBLISH_ADDRESS_STATUS_RECEIVED:
                if (intent.getExtras() != null) {
                    final boolean success = intent.getExtras().getBoolean(EXTRA_IS_SUCCESS);
                    final int statusCode = intent.getExtras().getInt(EXTRA_STATUS);
                    final byte[] elementAddress = intent.getExtras().getByteArray(EXTRA_ELEMENT_ADDRESS);
                    final byte[] publishAddress = intent.getExtras().getByteArray(EXTRA_PUBLISH_ADDRESS);
                    final int modelId = intent.getExtras().getInt(EXTRA_MODEL_ID);
                    mExtendedMeshNode.updateMeshNode(node);
                    mConfigModelPublicationStatus.onStatusChanged(success, statusCode, elementAddress, publishAddress, modelId);
                }
                break;
            case SUBSCRIPTION_ADD_SENT:
                break;
            case SUBSCRIPTION_STATUS_RECEIVED:
                if (intent.getExtras() != null) {
                    final boolean success = intent.getExtras().getBoolean(EXTRA_IS_SUCCESS);
                    final int statusCode = intent.getExtras().getInt(EXTRA_STATUS);
                    final byte[] elementAddress = intent.getExtras().getByteArray(EXTRA_ELEMENT_ADDRESS);
                    final byte[] subscriptionAddress = intent.getExtras().getByteArray(EXTRA_SUBSCRIPTION_ADDRESS);
                    final int modelId = intent.getExtras().getInt(EXTRA_MODEL_ID);
                    mExtendedMeshNode.updateMeshNode(node);
                    mMeshModel.postValue(model);
                    mConfigModelSubscriptionStatus.onStatusChanged(success, statusCode, elementAddress, subscriptionAddress, modelId);
                }
                break;
        }
    }

    private void handleGenericOnOffState(final Intent intent) {
        final String action = intent.getAction();
        final ProvisionedMeshNode node = mBinder.getMeshNode();
        final MeshModel model = mBinder.getMeshModel();
        switch (action) {
            case ACTION_GENERIC_ON_OFF_STATE:
                final boolean presentState = intent.getExtras().getBoolean(EXTRA_GENERIC_ON_OFF_PRESENT_STATE);
                //TODO implement target state and remaining state.
                mPresentState.postValue(presentState);
                break;
        }
    }

    @Override
    public void setElement(final Element element) {
        super.setElement(element);
    }

    @Override
    public void setModel(final ProvisionedMeshNode node, final int elementAddress, final int modelId) {
        super.setModel(node, elementAddress, modelId);
    }

    /**
     * Binds appkey to model
     *
     * @param appKeyIndex index of the application key that has already been added to the mesh node
     */
    public void sendBindAppKey(final int appKeyIndex) {
        mBinder.sendBindAppKey(mExtendedMeshNode.getMeshNode(), mElement.getValue().getElementAddress(), mMeshModel.getValue(), appKeyIndex);
    }

    /**
     * Unbbinds appkey to model
     *
     * @param appKeyIndex index of the application key that has already been added to the mesh node
     */
    public void sendUnbindAppKey(final int appKeyIndex) {
        mBinder.sendUnbindAppKey(mExtendedMeshNode.getMeshNode(), mElement.getValue().getElementAddress(), mMeshModel.getValue(), appKeyIndex);
    }

    public void sendConfigModelPublishAddressSet(final byte[] publishAddress) {
        final ProvisionedMeshNode node = mExtendedMeshNode.getMeshNode();
        final Element element = mElement.getValue();
        final MeshModel model = mMeshModel.getValue();
        if (!model.getBoundAppKeyIndexes().isEmpty()) {
            final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
            mBinder.sendConfigModelPublishAddressSet(node, element, model, appKeyIndex, publishAddress);
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.error_no_app_keys_bound), Toast.LENGTH_SHORT).show();
        }
    }

    public void sendConfigModelSubscriptionAdd(final byte[] subscriptionAddress) {
        final ProvisionedMeshNode node = mExtendedMeshNode.getMeshNode();
        final Element element = mElement.getValue();
        final MeshModel model = mMeshModel.getValue();
        mBinder.sendConfigModelSubscriptionAdd(node, element, model, subscriptionAddress);
    }

    public void sendConfigModelSubscriptionDelete(final byte[] subscriptionAddress) {
        final ProvisionedMeshNode node = mExtendedMeshNode.getMeshNode();
        final Element element = mElement.getValue();
        final MeshModel model = mMeshModel.getValue();
        mBinder.sendConfigModelSubscriptionDelete(node, element, model, subscriptionAddress);
    }

    /**
     * Send generic on off get to mesh node
     *
     * @param node mesh node to send generic on off get
     */
    public void sendGenericOnOffGet(final ProvisionedMeshNode node) {
        final Element element = mElement.getValue();
        final MeshModel model = mMeshModel.getValue();

        if (!model.getBoundAppKeyIndexes().isEmpty()) {
            final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
            final byte[] address = element.getElementAddress();
            Log.v(TAG, "Sending message to element's unicast address: " + MeshParserUtils.bytesToHex(address, true));

            mBinder.sendGenericOnOffGet(node, model, address, appKeyIndex);
        } else {
            Toast.makeText(mContext, R.string.error_no_app_keys_bound, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Send generic on off set to mesh node
     *
     * @param node                 mesh node to send generic on off set
     * @param transitionSteps      the number of steps
     * @param transitionResolution the resolution for the number of steps
     * @param delay                message execution delay in 5ms steps. After this delay milliseconds the model will execute the required behaviour.
     * @param state                on off state
     */
    public void sendGenericOnOffSet(final ProvisionedMeshNode node, final Integer transitionSteps, final Integer transitionResolution, final Integer delay, final boolean state) {
        final Element element = mElement.getValue();
        final MeshModel model = mMeshModel.getValue();

        if (!model.getBoundAppKeyIndexes().isEmpty()) {
            final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
            if (!model.getSubscriptionAddresses().isEmpty()) {
                final byte[] address = model.getSubscriptionAddresses().get(0);
                Log.v(TAG, "Subscription addresses found for model: " + CompositionDataParser.formatModelIdentifier(model.getModelId(), true)
                        + ". Sending message to subscription address: " + MeshParserUtils.bytesToHex(address, true));
                mBinder.sendGenericOnOffSet(node, model, address, appKeyIndex, transitionSteps, transitionResolution, delay, state);
            } else {
                final byte[] address = element.getElementAddress();
                Log.v(TAG, "No subscription addresses found for model: " + CompositionDataParser.formatModelIdentifier(model.getModelId(), true)
                        + ". Sending message to element's unicast address: " + MeshParserUtils.bytesToHex(address, true));

                mBinder.sendGenericOnOffSet(node, model, address, appKeyIndex, transitionSteps, transitionResolution, delay, state);
            }
        } else {
            Toast.makeText(mContext, R.string.error_no_app_keys_bound, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Send generic on off set to mesh node
     *
     * @param node                 mesh node to send generic on off set
     * @param transitionSteps      the number of steps
     * @param transitionResolution the resolution for the number of steps
     * @param delay                message execution delay in 5ms steps. After this delay milliseconds the model will execute the required behaviour.
     * @param state                on off state
     */
    public void sendGenericOnOffSetUnacknowledged(final ProvisionedMeshNode node, final Integer transitionSteps, final Integer transitionResolution, final Integer delay, final boolean state) {
        final Element element = mElement.getValue();
        final MeshModel model = mMeshModel.getValue();

        if (!model.getBoundAppKeyIndexes().isEmpty()) {
            final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
            if (!model.getSubscriptionAddresses().isEmpty()) {
                final List<byte[]> addressList = model.getSubscriptionAddresses();
                for(int i = 0; i < addressList.size(); i++) {
                    final byte[] address = addressList.get(i);
                    Log.v(TAG, "Subscription addresses found for model: " + CompositionDataParser.formatModelIdentifier(model.getModelId(), true)
                            + ". Sending message to subscription address: " + MeshParserUtils.bytesToHex(address, true));
                    mBinder.sendGenericOnOffSetUnacknowledged(node, model, address, appKeyIndex, transitionSteps, transitionResolution, delay, state);
                }
            } else {
                final byte[] address = element.getElementAddress();
                Log.v(TAG, "No subscription addresses found for model: " + CompositionDataParser.formatModelIdentifier(model.getModelId(), true)
                        + ". Sending message to element's unicast address: " + MeshParserUtils.bytesToHex(address, true));

                mBinder.sendGenericOnOffSetUnacknowledged(node, model, address, appKeyIndex, transitionSteps, transitionResolution, delay, state);
            }
        } else {
            Toast.makeText(mContext, R.string.error_no_app_keys_bound, Toast.LENGTH_SHORT).show();
        }
    }
}
