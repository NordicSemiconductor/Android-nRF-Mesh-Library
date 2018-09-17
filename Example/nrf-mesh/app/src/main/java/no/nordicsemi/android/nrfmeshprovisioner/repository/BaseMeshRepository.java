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
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.meshprovisioner.ProvisioningSettings;
import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.Element;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.AppKeyBindStatusLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.AppKeyStatusLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.CompositionDataStatusLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ConfigModelPublicationStatusLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ConfigModelSubscriptionStatusLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ExtendedMeshModel;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ExtendedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ProvisionedNodesLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ProvisioningLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ProvisioningStateLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.TransactionFailedLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.service.MeshService;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;

import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.ACTION_CONFIGURATION_STATE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.ACTION_CONNECTION_STATE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.ACTION_GENERIC_ON_OFF_STATE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.ACTION_IS_CONNECTED;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.ACTION_IS_RECONNECTING;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.ACTION_ON_DEVICE_READY;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.ACTION_PROVISIONING_STATE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.ACTION_TRANSACTION_STATE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.ACTION_VENDOR_MODEL_MESSAGE_STATE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_DATA;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_ELEMENT_ADDRESS;

public abstract class BaseMeshRepository {

    protected static final String TAG = BaseMeshRepository.class.getSimpleName();

    /** Application context **/
    protected final Context mContext;

    /** Connection states Connecting, Connected, Disconnecting, Disconnected etc. **/
    final MutableLiveData<Boolean> mIsConnected = new MutableLiveData<>();

    /** Flag to determine if the device is ready **/
    final MutableLiveData<Boolean> mOnDeviceReady = new MutableLiveData<>();

    /** Updates the connection state while connecting to a peripheral **/
    final MutableLiveData<String> mConnectionState = new MutableLiveData<>();

    /** Flag to determine if a reconnection is in the progress when provisioning has completed **/
    final MutableLiveData<Boolean> mIsReconnecting = new MutableLiveData<>();

    /** Flag to determine if a reconnection is in the progress when provisioning has completed **/
    final MutableLiveData<byte[]> mConfigurationSrc = new MutableLiveData<>();

    /** Flag to determine if provisioning was completed **/
    boolean mIsProvisioningComplete = false;

    /** Contains the {@link ExtendedMeshNode} **/
    ExtendedMeshNode mExtendedMeshNode;

    /** Contains the {@link ExtendedMeshModel} **/
    ExtendedMeshModel mExtendedMeshModel;

    /** Mesh model to configure **/
    final MutableLiveData<MeshModel> mMeshModel = new MutableLiveData<>();

    /** Mesh model to configure **/
    final MutableLiveData<Element> mElement = new MutableLiveData<>();

    /** App key add status **/
    final CompositionDataStatusLiveData mCompositionDataStatus = new CompositionDataStatusLiveData();

    /** App key add status **/
    final AppKeyStatusLiveData mAppKeyStatus = new AppKeyStatusLiveData();

    /** App key bind status **/
    final AppKeyBindStatusLiveData mAppKeyBindStatus = new AppKeyBindStatusLiveData();

    /** publication status **/
    final ConfigModelPublicationStatusLiveData mConfigModelPublicationStatus = new ConfigModelPublicationStatusLiveData();

    /** Subscription bind status **/
    final ConfigModelSubscriptionStatusLiveData mConfigModelSubscriptionStatus = new ConfigModelSubscriptionStatusLiveData();

    /** Contains the initial provisioning live data **/
    final ProvisioningLiveData mProvisioningLiveData = new ProvisioningLiveData();

    /** Contains the provisioned nodes **/
    final ProvisionedNodesLiveData mProvisionedNodesLiveData = new ProvisionedNodesLiveData();

    final TransactionFailedLiveData mTransactionFailedLiveData = new TransactionFailedLiveData();

    final ProvisioningStateLiveData mProvisioningStateLiveData;
    MeshService.MeshServiceBinder mBinder;
    MeshManagerApi mMeshManagerApi;
    private boolean mIsBound;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mBinder = (MeshService.MeshServiceBinder) service;
            if (mBinder != null) {
                mIsBound = true;
                mMeshManagerApi = mBinder.getMeshManagerApi();
                mProvisioningLiveData.loadProvisioningData(mContext, mBinder.getProvisioningSettings());
                mConfigurationSrc.postValue(mMeshManagerApi.getConfiguratorSrc());
                mProvisionedNodesLiveData.updateProvisionedNodes(mBinder.getProvisionedNodes());
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mBinder = null;
            mIsBound = false;
        }
    };

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_IS_CONNECTED:
                    final boolean isConnected = intent.getExtras().getBoolean(EXTRA_DATA);
                    isDeviceConnected(isConnected);
                    break;
                case ACTION_CONNECTION_STATE:
                    onConnectionStateChanged(intent.getExtras().getString(EXTRA_DATA));
                    break;
                case ACTION_IS_RECONNECTING:
                    isReconnecting(intent.getExtras().getBoolean(EXTRA_DATA));
                    break;
                case ACTION_ON_DEVICE_READY:
                    onDeviceReady(intent.getExtras().getBoolean(EXTRA_DATA));
                    break;
                case ACTION_PROVISIONING_STATE:
                    onProvisioningStateChanged(intent);
                    break;
                case ACTION_CONFIGURATION_STATE:
                    onConfigurationMessageStateChanged(intent);
                    break;
                case ACTION_GENERIC_ON_OFF_STATE:
                    onGenericMessageStateChanged(intent);
                    break;
                case ACTION_VENDOR_MODEL_MESSAGE_STATE:
                    onGenericMessageStateChanged(intent);
                    break;
                case ACTION_TRANSACTION_STATE:
                    onTransactionStateReceived(intent);
                    break;
            }
        }
    };

    BaseMeshRepository(final Context context){
        final Intent intent = new Intent(context, MeshService.class);
        context.startService(intent);
        context.bindService(intent, mServiceConnection, 0);
        mContext = context;
        mProvisioningStateLiveData = new ProvisioningStateLiveData();
        mIsReconnecting.postValue(false);
    }

    public void unbindService(){
        mContext.unbindService(mServiceConnection);
        mIsBound = false;
        mBinder = null;
    }

    public abstract void onConnectionStateChanged(final String connectionState);

    public abstract void isDeviceConnected(final boolean isConnected);

    public abstract void onDeviceReady(final boolean isReady);

    public abstract void isReconnecting(final boolean isReconnecting);

    public abstract void onProvisioningStateChanged(final Intent intent);

    public abstract void onConfigurationMessageStateChanged(final Intent intent);

    protected void onGenericMessageStateChanged(final Intent intent){

    }

    protected void onTransactionStateReceived(final Intent intent){
        final String action = intent.getAction();
        final ProvisionedMeshNode node = (ProvisionedMeshNode) mBinder.getMeshNode();
        switch (action) {
            case ACTION_TRANSACTION_STATE:
                if(mExtendedMeshNode != null) {
                    Log.v(TAG, "TRANSACTION FAILED");
                    mExtendedMeshNode.updateMeshNode(node);
                    final int elementAddress = intent.getExtras().getInt(EXTRA_ELEMENT_ADDRESS);
                    final boolean incompleteTimerExpired = intent.getBooleanExtra(EXTRA_DATA, true);
                    mTransactionFailedLiveData.onTransactionFailed(elementAddress, incompleteTimerExpired);
                }
                break;
        }
    }

    public ProvisioningLiveData getProvisioningData(){
        return mProvisioningLiveData;
    }

    public TransactionFailedLiveData getTransactionFailedLiveData() {
        return mTransactionFailedLiveData;
    }

    /**
     * Registers a broadcast receiver to receive events from the {@link MeshService}
     */
    public void registerBroadcastReceiver(){
        final IntentFilter intentFilter = Utils.createIntentFilters();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    public void unregisterBroadcastReceiver(){
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
    }

    /**
     * Disconnect from peripheral
     */
    public void disconnect() {
        mBinder.disconnect();
    }

    public void setMeshNode(final ProvisionedMeshNode meshNode) {
        if(mBinder != null) {
            mBinder.setMeshNode(meshNode);
        }

        if(mExtendedMeshNode == null) {
            mExtendedMeshNode = new ExtendedMeshNode(meshNode);
        } else {
            mExtendedMeshNode.updateMeshNode(meshNode);
        }
    }

    /**
     * Selects the mesh model to be configured
     * @param element element belonging to the node
     */
    public void setElement(final Element element) {
        mBinder.setElement(element);
        mElement.postValue(element);
    }

    public LiveData<MeshModel> getMeshModel() {
        return mMeshModel;
    }

    /**
     * Selects the mesh node to be configured
     * @param meshNode mesh node to configure
     */
    public void setModel(final ProvisionedMeshNode meshNode, final int elementAddress, final int modelId) {
        /*if(mExtendedMeshNode == null) {
            mExtendedMeshNode = new ExtendedMeshNode(meshNode);
        } else {
            mExtendedMeshNode.updateMeshNode(meshNode);
        }*/
        setMeshNode(meshNode);
        final Element element = meshNode.getElements().get(elementAddress);
        if(element != null) {
            mElement.setValue(element);
        }

        final MeshModel model = element.getMeshModels().get(modelId);
        if(model != null) {
            mMeshModel.setValue(model);
        }
    }

    public ExtendedMeshNode getExtendedMeshNode() {
        return mExtendedMeshNode;
    }

    public CompositionDataStatusLiveData getCompositionDataStatus() {
        return mCompositionDataStatus;
    }

    public AppKeyStatusLiveData getAppKeyStatus() {
        return mAppKeyStatus;
    }

    public AppKeyBindStatusLiveData getAppKeyBindStatus() {
        return mAppKeyBindStatus;
    }

    public ConfigModelPublicationStatusLiveData getConfigModelPublicationStatus() {
        return mConfigModelPublicationStatus;
    }

    public ConfigModelSubscriptionStatusLiveData getConfigModelSubscriptionStatus() {
        return mConfigModelSubscriptionStatus;
    }

    /**
     * Reset mesh network
     */
    public void resetMeshNetwork() {
        mBinder.disconnect();
        mBinder.resetMeshNetwork();
        mProvisioningLiveData.refreshProvisioningData(mBinder.getProvisioningSettings());
        mProvisionedNodesLiveData.clearNodes();
        mExtendedMeshNode = null;
    }

    public void setSelectedAppKey(final int appKeyIndex, final String appkey) {
        mBinder.setSelectedAppkey(appKeyIndex, appkey);
        mProvisioningLiveData.setSelectedAppKey(appkey);
    }

    public String getSelectedAppKey() {
        return mBinder.getSelectedAppKey();
    }

    public void sendAppKeyAdd(final int appKeyIndex, final String appKey) {
        mBinder.sendAppKeyAdd((ProvisionedMeshNode) mExtendedMeshNode.getMeshNode(), appKeyIndex, appKey);
    }

    public void refreshProvisioningData() {
        if(mBinder != null) {
            final ProvisioningSettings settings = mMeshManagerApi.getProvisioningSettings();
            mProvisioningLiveData.update(settings);
        }
    }

    public LiveData<byte[]> getConfigurationSrc() {
        return mConfigurationSrc;
    }
}
