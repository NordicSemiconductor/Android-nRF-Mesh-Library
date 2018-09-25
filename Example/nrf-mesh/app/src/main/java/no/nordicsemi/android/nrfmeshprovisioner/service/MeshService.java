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

package no.nordicsemi.android.nrfmeshprovisioner.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.meshprovisioner.BaseMeshNode;
import no.nordicsemi.android.meshprovisioner.MeshStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.meshprovisioner.MeshManagerTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshProvisioningStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.ProvisioningSettings;
import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.states.UnprovisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.AddressUtils;
import no.nordicsemi.android.meshprovisioner.utils.ConfigModelPublicationSetParams;
import no.nordicsemi.android.meshprovisioner.utils.Element;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManager;
import no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManagerCallbacks;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ConfigModelPublicationStatusLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.MeshNodeStates;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManager.MESH_PROXY_UUID;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.*;

public class MeshService extends Service implements BleMeshManagerCallbacks,
        MeshProvisioningStatusCallbacks,
        MeshStatusCallbacks,
        MeshManagerTransportCallbacks {

    public static final String NRF_MESH_GROUP_ID = "NRF_MESH_GROUP_ID";
    private static final String PRIMARY_CHANNEL = "PRIMARY_CHANNEL";
    private static final String PRIMARY_CHANNEL_ID = "no.nordicsemi.android.nrfmeshprovisioner";
    private static final int FOREGROUND_NOTIFICATION_ID = 1102;
    private static String TAG = MeshService.class.getSimpleName();
    /**
     * Mesh ble manager handles the ble operations
     **/
    @Inject
    BleMeshManager mBleMeshManager;
    MeshManagerApi mMeshManagerApi;
    /**
     * Connection states Connecting, Connected, Disconnecting, Disconnected etc.
     **/
    private boolean mIsConnected;
    /**
     * Flag to determine if the device is ready
     **/
    private boolean mOnDeviceReady;
    /**
     * Flag to determine if a reconnection is in the progress when provisioning has completed
     **/
    private boolean mIsReconnecting;
    /**
     * Flag to determine if provisioning was completed
     **/
    private boolean mIsProvisioningComplete = false;
    /**
     * Flag to determine if the intial configuration was completed
     **/
    private boolean mIsConfigurationComplete = false;
    /**
     * Contains the {@link UnprovisionedMeshNode}
     **/
    private BaseMeshNode mMeshNode;
    /**
     * Mesh model to configure
     **/
    private MeshModel mMeshModel;
    /**
     * Mesh model to configure
     **/
    private Element mElement;
    /**
     * App key bind status
     **/
    private ConfigModelPublicationStatusLiveData mConfigModelPublicationStatus = new ConfigModelPublicationStatusLiveData();
    /**
     * Contains the initial provisioning live data
     **/
    private ProvisioningSettings mProvisioningSettings;
    /**
     * app key index
     **/
    private int mAppKeyIndex;
    /**
     * app key
     **/
    private String mAppKey;
    /**
     * flag to avoid adding app key when requesting composition data only as the initial provisioning steps of the app will continue adding app key after a composition data get
     **/
    private boolean mShouldAddAppKeyBeAdded = false;
    private BluetoothDevice mBluetoothDevice;
    private String mDeviceName;
    private Handler mHandler;
    private boolean mIsScanning;
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(final int callbackType, final ScanResult result) {
            //In order to connect to the correct device, the hash advertised in the advertisement data should be matched.
            //This is to make sure we connect to the same device as device addresses could change after provisioning.
            final ScanRecord scanRecord = result.getScanRecord();
            if (scanRecord != null) {
                final byte[] serviceData = scanRecord.getServiceData(new ParcelUuid((MESH_PROXY_UUID)));
                if (serviceData != null) {
                    if (mMeshManagerApi.isAdvertisedWithNodeIdentity(serviceData)) {
                        final ProvisionedMeshNode node = (ProvisionedMeshNode) mMeshNode;
                        if (mMeshManagerApi.nodeIdentityMatches(node, serviceData)) {
                            stopScan();
                            sendBroadcastConnectivityState(getString(R.string.state_scanning_provisioned_node_found, scanRecord.getDeviceName()));
                            onProvisionedDeviceFound(node, new ExtendedBluetoothDevice(result));
                        }
                    }
                }
            }
        }

        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
            // Batch scan is disabled (report delay = 0)
        }

        @Override
        public void onScanFailed(final int errorCode) {

        }
    };
    private final Runnable mScannerTimeout = () -> {
        Toast.makeText(getApplicationContext(), R.string.provisioned_device_not_found, Toast.LENGTH_SHORT).show();
        stopScan();
    };
    private final Runnable mReconnectRunnable = this::startScan;
    /**
     * Flag to verify if we are connecting to a mesh network or an unprovisioned devices
     **/
    private boolean mConnectToMeshNetwork;
    private NotificationManager mNotificationManager;
    private NotificationChannel mNotificationChannel;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
        mMeshManagerApi = new MeshManagerApi(this);
        mMeshManagerApi.setProvisionerManagerTransportCallbacks(this);
        mMeshManagerApi.setProvisioningStatusCallbacks(this);
        mMeshManagerApi.setMeshStatusCallbacks(this);

        mBleMeshManager.setGattCallbacks(this);

        createNotificationPrerequisites();
        startForeground(FOREGROUND_NOTIFICATION_ID, createForegroundNotification());
        mHandler = new Handler();
        //Load provisioning data in to a LiveData
        mProvisioningSettings = mMeshManagerApi.getProvisioningSettings();
        Log.v(TAG, "Service created");

    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        createForegroundNotification();
        if (intent.getExtras() != null) {
            final String action = intent.getAction();
            final ExtendedBluetoothDevice device = intent.getExtras().getParcelable(EXTRA_DEVICE);
            switch (action) {
                case Utils.ACTION_CONNECT_TO_UNPROVISIONED_NODE:
                    mConnectToMeshNetwork = false;
                    if (device != null) {
                        connect(device);
                    }
                    break;
                case Utils.ACTION_CONNECT_TO_MESH_NETWORK:
                    mConnectToMeshNetwork = true;
                    if (device != null) {
                        connect(device);
                    }
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    private void createNotificationPrerequisites() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Utils.checkIfVersionIsOreoOrAbove()) {
            if (mNotificationChannel == null) {
                mNotificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID, PRIMARY_CHANNEL, NotificationManager.IMPORTANCE_LOW);
            }
            mNotificationManager.createNotificationChannel(mNotificationChannel);
        }
    }

    /**
     * Creates a Notifications for the devices that are currently connected.
     */
    private Notification createForegroundNotification() {
        final NotificationCompat.Builder builder = getBackgroundNotificationBuilder();
        builder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
        builder.setGroup(NRF_MESH_GROUP_ID).setDefaults(0).setOngoing(false); // an ongoing notification will not be shown on Android Wear
        builder.setContentTitle(getString(R.string.mesh_provioner_service_running));
        return builder.build();
    }

    /**
     * Returns a notification builder
     */
    private NotificationCompat.Builder getBackgroundNotificationBuilder() {
        /*final Intent parentIntent = new Intent(this, MainActivity.class);
        parentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);*/

        // Both activities above have launchMode="singleTask" in the AndroidManifest.xml file, so if the task is already running, it will be resumed
        //final PendingIntent pendingIntent = PendingIntent.getActivities(this, Utils.OPEN_ACTIVITY_REQ, new Intent[]{parentIntent}, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), PRIMARY_CHANNEL);
        builder./*setContentIntent(pendingIntent)*/setAutoCancel(true);
        builder.setSmallIcon(R.drawable.ic_stat_mesh_notifcation);
        builder.setChannelId(PRIMARY_CHANNEL_ID);
        return builder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        Log.v(TAG, "Service got killed");
    }

    @Override
    public MeshServiceBinder onBind(Intent intent) {
        return new MeshServiceBinder();
    }

    @Override
    public void onDeviceConnecting(final BluetoothDevice device) {
        sendBroadcastConnectivityState(getString(R.string.state_connecting));
    }

    @Override
    public void onDeviceConnected(final BluetoothDevice device) {
        mBluetoothDevice = device;
        mIsReconnecting = false;
        mIsConnected = true;
        sendBroadcastConnectivityState(getString(R.string.state_discovering_services));
        sendBroadcastIsConnected(mIsConnected);
    }

    @Override
    public void onDeviceDisconnecting(final BluetoothDevice device) {
        mIsConnected = false;
    }

    @Override
    public void onDeviceDisconnected(final BluetoothDevice device) {
        handleConnectivityStates(false);
    }

    @Override
    public void onLinklossOccur(final BluetoothDevice device) {
        mIsReconnecting = false;
        mIsProvisioningComplete = false;
        mIsConfigurationComplete = false;
        handleConnectivityStates(false);
    }

    @Override
    public void onServicesDiscovered(final BluetoothDevice device, final boolean optionalServicesFound) {
        sendBroadcastConnectivityState(getString(R.string.state_initializing));
    }

    @Override
    public void onDeviceReady(final BluetoothDevice device) {
        mOnDeviceReady = true;
        sendBroadcastDeviceReady(true);

        if (!mConnectToMeshNetwork) {
            if (mBleMeshManager.isProvisioningComplete()) {
                if (!mIsConfigurationComplete) {
                    //We update the bluetooth device after a startScan because some devices may start advertising with different mac address
                    mMeshNode.setBluetoothDeviceAddress(device.getAddress());
                    //Adding a slight delay here so we don't send anything before we receive the mesh beacon message
                    mHandler.postDelayed(() -> {
                        mMeshManagerApi.getCompositionData((ProvisionedMeshNode) mMeshNode);
                        //We set this to true so that once provisioning is complete, it will
                        // continue to with the app key add configuration after the Composition data is received
                        mShouldAddAppKeyBeAdded = true;
                    }, 2000);
                }
            }
        }
    }

    @Override
    public boolean shouldEnableBatteryLevelNotifications(final BluetoothDevice device) {
        return false;
    }

    @Override
    public void onBatteryValueReceived(final BluetoothDevice device, final int value) {

    }

    @Override
    public void onBondingRequired(final BluetoothDevice device) {

    }

    @Override
    public void onBonded(final BluetoothDevice device) {

    }

    @Override
    public void onError(final BluetoothDevice device, final String message, final int errorCode) {

    }

    @Override
    public void onDeviceNotSupported(final BluetoothDevice device) {

    }

    @Override
    public void onDataReceived(final BluetoothDevice bluetoothDevice, final int mtu, final byte[] pdu) {
        final BaseMeshNode node = mMeshNode;
        mMeshManagerApi.handleNotifications(node, mtu, pdu);
    }

    @Override
    public void onDataSent(final BluetoothDevice device, final int mtu, final byte[] pdu) {
        final BaseMeshNode node = mMeshNode;
        mMeshManagerApi.handleWrites(node, mtu, pdu);
    }

    @Override
    public void sendPdu(final BaseMeshNode meshNode, final byte[] pdu) {
        mMeshNode = meshNode;
        mBleMeshManager.sendPdu(pdu);
    }

    @Override
    public int getMtu() {
        return mBleMeshManager.getMtuSize();
    }

    @Override
    public void onProvisioningInviteSent(final UnprovisionedMeshNode meshNode) {
        final Intent intent = new Intent(ACTION_PROVISIONING_STATE);
        intent.putExtra(EXTRA_PROVISIONING_STATE, MeshNodeStates.MeshNodeStatus.PROVISIONING_INVITE.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onProvisioningCapabilitiesReceived(final UnprovisionedMeshNode meshNode) {
        final Intent intent = new Intent(ACTION_PROVISIONING_STATE);
        intent.putExtra(EXTRA_PROVISIONING_STATE, MeshNodeStates.MeshNodeStatus.PROVISIONING_CAPABILITIES.getState());
        mMeshNode = meshNode;
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onProvisioningStartSent(final UnprovisionedMeshNode meshNode) {
        final Intent intent = new Intent(ACTION_PROVISIONING_STATE);
        intent.putExtra(EXTRA_PROVISIONING_STATE, MeshNodeStates.MeshNodeStatus.PROVISIONING_START.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onProvisioningPublicKeySent(final UnprovisionedMeshNode meshNode) {
        final Intent intent = new Intent(ACTION_PROVISIONING_STATE);
        intent.putExtra(EXTRA_PROVISIONING_STATE, MeshNodeStates.MeshNodeStatus.PROVISIONING_PUBLIC_KEY_SENT.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onProvisioningPublicKeyReceived(final UnprovisionedMeshNode meshNode) {
        final Intent intent = new Intent(ACTION_PROVISIONING_STATE);
        intent.putExtra(EXTRA_PROVISIONING_STATE, MeshNodeStates.MeshNodeStatus.PROVISIONING_PUBLIC_KEY_RECEIVED.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onProvisioningAuthenticationInputRequested(final UnprovisionedMeshNode meshNode) {
        final Intent intent = new Intent(ACTION_PROVISIONING_STATE);
        intent.putExtra(EXTRA_PROVISIONING_STATE, MeshNodeStates.MeshNodeStatus.PROVISIONING_AUTHENTICATION_INPUT_WAITING.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onProvisioningInputCompleteSent(final UnprovisionedMeshNode meshNode) {
        final Intent intent = new Intent(ACTION_PROVISIONING_STATE);
        intent.putExtra(EXTRA_PROVISIONING_STATE, MeshNodeStates.MeshNodeStatus.PROVISIONING_INPUT_COMPLETE.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onProvisioningConfirmationSent(final UnprovisionedMeshNode meshNode) {
        final Intent intent = new Intent(ACTION_PROVISIONING_STATE);
        intent.putExtra(EXTRA_PROVISIONING_STATE, MeshNodeStates.MeshNodeStatus.PROVISIONING_CONFIRMATION_SENT.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onProvisioningConfirmationReceived(final UnprovisionedMeshNode meshNode) {
        final Intent intent = new Intent(ACTION_PROVISIONING_STATE);
        intent.putExtra(EXTRA_PROVISIONING_STATE, MeshNodeStates.MeshNodeStatus.PROVISIONING_CONFIRMATION_RECEIVED.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onProvisioningRandomSent(final UnprovisionedMeshNode meshNode) {
        final Intent intent = new Intent(ACTION_PROVISIONING_STATE);
        intent.putExtra(EXTRA_PROVISIONING_STATE, MeshNodeStates.MeshNodeStatus.PROVISIONING_RANDOM_SENT.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onProvisioningRandomReceived(final UnprovisionedMeshNode meshNode) {
        final Intent intent = new Intent(ACTION_PROVISIONING_STATE);
        intent.putExtra(EXTRA_PROVISIONING_STATE, MeshNodeStates.MeshNodeStatus.PROVISIONING_RANDOM_RECEIVED.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onProvisioningDataSent(final UnprovisionedMeshNode meshNode) {
        final Intent intent = new Intent(ACTION_PROVISIONING_STATE);
        intent.putExtra(EXTRA_PROVISIONING_STATE, MeshNodeStates.MeshNodeStatus.PROVISIONING_DATA_SENT.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onProvisioningFailed(final UnprovisionedMeshNode meshNode, final int errorCode) {
        final Intent intent = new Intent(ACTION_PROVISIONING_STATE);
        intent.putExtra(EXTRA_PROVISIONING_STATE, MeshNodeStates.MeshNodeStatus.PROVISIONING_FAILED.getState());
        intent.putExtra(EXTRA_DATA, errorCode);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        mIsProvisioningComplete = false;
    }

    @Override
    public void onProvisioningComplete(final ProvisionedMeshNode provisionedMeshNode) {
        provisionedMeshNode.setIsProvisioned(true);
        mMeshNode = provisionedMeshNode;
        mIsProvisioningComplete = true;

        final Intent intent = new Intent(ACTION_PROVISIONING_STATE);
        intent.putExtra(EXTRA_PROVISIONING_STATE, MeshNodeStates.MeshNodeStatus.PROVISIONING_COMPLETE.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        mIsProvisioningComplete = true;
        mIsReconnecting = true;
        final Intent intent1 = new Intent(ACTION_IS_RECONNECTING);
        intent1.putExtra(EXTRA_DATA, mIsReconnecting);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);

        //mIsReconnecting.postValue(true);
        mBleMeshManager.setProvisioningComplete(true);
        mBleMeshManager.disconnect();
        mBleMeshManager.refreshDeviceCache();
        mHandler.postDelayed(mReconnectRunnable, 1500); //Added a slight delay to disconnect and refresh the cache
    }

    @Override
    public void onTransactionFailed(final ProvisionedMeshNode node, final int src, final boolean hasIncompleteTimerExpired) {
        mMeshNode = node;
        final Intent intent = new Intent(ACTION_TRANSACTION_STATE);
        intent.putExtra(EXTRA_ELEMENT_ADDRESS, src);
        intent.putExtra(EXTRA_DATA, hasIncompleteTimerExpired);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onUnknownPduReceived(ProvisionedMeshNode node) {
        //According to the spec we can disconnect if we receive an unexpected pdu
        //Well why don't we gracefully handle this J
    }

    @Override
    public void onBlockAcknowledgementSent(final ProvisionedMeshNode node) {
        mMeshNode = node;
        final Intent intent = new Intent(ACTION_CONFIGURATION_STATE);
        intent.putExtra(EXTRA_CONFIGURATION_STATE, MeshNodeStates.MeshNodeStatus.SENDING_BLOCK_ACKNOWLEDGEMENT.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onBlockAcknowledgementReceived(final ProvisionedMeshNode node) {
        mMeshNode = node;
        final Intent intent = new Intent(ACTION_CONFIGURATION_STATE);
        intent.putExtra(EXTRA_CONFIGURATION_STATE, MeshNodeStates.MeshNodeStatus.BLOCK_ACKNOWLEDGEMENT_RECEIVED.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onGetCompositionDataSent(final ProvisionedMeshNode node) {
        mMeshNode = node;
        final Intent intent = new Intent(ACTION_CONFIGURATION_STATE);
        intent.putExtra(EXTRA_CONFIGURATION_STATE, MeshNodeStates.MeshNodeStatus.COMPOSITION_DATA_GET_SENT.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onCompositionDataStatusReceived(final ProvisionedMeshNode node) {
        mMeshNode = node;
        final Intent intent = new Intent(ACTION_CONFIGURATION_STATE);
        intent.putExtra(EXTRA_CONFIGURATION_STATE, MeshNodeStates.MeshNodeStatus.COMPOSITION_DATA_STATUS_RECEIVED.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        if (mShouldAddAppKeyBeAdded) {
            mShouldAddAppKeyBeAdded = false; //set it to false once the message is set on the handler
            //We send app key add after composition is complete. Adding a delay so that we don't send anything before the acknowledgement is sent out.
            mHandler.postDelayed(() -> {
                final int appKeyIndex = mAppKeyIndex;
                final String appKey = mAppKey = mProvisioningSettings.getAppKeys().get(appKeyIndex);
                mMeshManagerApi.addAppKey(node, appKeyIndex, appKey);
                mAppKeyIndex = 0;
                mAppKey = null;
            }, 1500);
        }
    }

    @Override
    public void onAppKeyAddSent(final ProvisionedMeshNode node) {
        mMeshNode = node;
        final Intent intent = new Intent(ACTION_CONFIGURATION_STATE);
        intent.putExtra(EXTRA_CONFIGURATION_STATE, MeshNodeStates.MeshNodeStatus.SENDING_APP_KEY_ADD.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onAppKeyStatusReceived(final ProvisionedMeshNode node, final boolean success, final int status, final int netKeyIndex, final int appKeyIndex) {
        mIsConfigurationComplete = true;
        mMeshNode = node;
        final Intent intent = new Intent(ACTION_CONFIGURATION_STATE);
        intent.putExtra(EXTRA_CONFIGURATION_STATE, MeshNodeStates.MeshNodeStatus.APP_KEY_STATUS_RECEIVED.getState());
        intent.putExtra(EXTRA_STATUS, status);
        intent.putExtra(EXTRA_IS_SUCCESS, success);
        intent.putExtra(EXTRA_NET_KEY_INDEX, netKeyIndex);
        intent.putExtra(EXTRA_APP_KEY_INDEX, appKeyIndex);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onAppKeyBindSent(final ProvisionedMeshNode node) {
        mMeshNode = node;
        final Intent intent = new Intent(ACTION_CONFIGURATION_STATE);
        intent.putExtra(EXTRA_CONFIGURATION_STATE, MeshNodeStates.MeshNodeStatus.APP_BIND_SENT.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onAppKeyUnbindSent(final ProvisionedMeshNode node) {
        mMeshNode = node;
        final Intent intent = new Intent(ACTION_CONFIGURATION_STATE);
        intent.putExtra(EXTRA_CONFIGURATION_STATE, MeshNodeStates.MeshNodeStatus.APP_UNBIND_SENT.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onAppKeyBindStatusReceived(final ProvisionedMeshNode node, final boolean success, final int status, final int elementAddress, final int appKeyIndex, final int modelIdentifier) {
        mMeshNode = node;
        final Element element = node.getElements().get(elementAddress);
        mElement = element;
        mMeshModel = element.getMeshModels().get(modelIdentifier);
        final Intent intent = new Intent(ACTION_CONFIGURATION_STATE);
        intent.putExtra(EXTRA_CONFIGURATION_STATE, MeshNodeStates.MeshNodeStatus.APP_BIND_STATUS_RECEIVED.getState());
        intent.putExtra(EXTRA_IS_SUCCESS, success);
        intent.putExtra(EXTRA_STATUS, status);
        intent.putExtra(EXTRA_ELEMENT_ADDRESS, elementAddress);
        intent.putExtra(EXTRA_APP_KEY_INDEX, appKeyIndex);
        intent.putExtra(EXTRA_MODEL_ID, modelIdentifier);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onPublicationSetSent(final ProvisionedMeshNode node) {
        mMeshNode = node;
        final Intent intent = new Intent(ACTION_CONFIGURATION_STATE);
        intent.putExtra(EXTRA_CONFIGURATION_STATE, MeshNodeStates.MeshNodeStatus.PUBLISH_ADDRESS_SET_SENT.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onPublicationStatusReceived(final ProvisionedMeshNode node, final boolean success, final int status, final byte[] elementAddress, final byte[] publishAddress, final int modelIdentifier) {
        mMeshNode = node;
        final Element element = node.getElements().get(AddressUtils.getUnicastAddressInt(elementAddress));
        mElement = element;
        mMeshModel = element.getMeshModels().get(modelIdentifier);

        final Intent intent = new Intent(ACTION_CONFIGURATION_STATE);
        intent.putExtra(EXTRA_CONFIGURATION_STATE, MeshNodeStates.MeshNodeStatus.PUBLISH_ADDRESS_STATUS_RECEIVED.getState());
        intent.putExtra(EXTRA_IS_SUCCESS, success);
        intent.putExtra(EXTRA_STATUS, status);
        intent.putExtra(EXTRA_ELEMENT_ADDRESS, elementAddress);
        intent.putExtra(EXTRA_PUBLISH_ADDRESS, publishAddress);
        intent.putExtra(EXTRA_MODEL_ID, modelIdentifier);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onSubscriptionAddSent(final ProvisionedMeshNode node) {
        mMeshNode = node;
        final Intent intent = new Intent(ACTION_CONFIGURATION_STATE);
        intent.putExtra(EXTRA_CONFIGURATION_STATE, MeshNodeStates.MeshNodeStatus.SUBSCRIPTION_ADD_SENT.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onSubscriptionDeleteSent(final ProvisionedMeshNode node) {
        mMeshNode = node;
        final Intent intent = new Intent(ACTION_CONFIGURATION_STATE);
        intent.putExtra(EXTRA_CONFIGURATION_STATE, MeshNodeStates.MeshNodeStatus.SUBSCRIPTION_DELETE_SENT.getState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onSubscriptionStatusReceived(final ProvisionedMeshNode node, final boolean success, final int status, final byte[] elementAddress, final byte[] subscriptionAddress, final int modelIdentifier) {
        mMeshNode = node;
        final Element element = node.getElements().get(AddressUtils.getUnicastAddressInt(elementAddress));
        mElement = element;
        mMeshModel = element.getMeshModels().get(modelIdentifier);

        final Intent intent = new Intent(ACTION_CONFIGURATION_STATE);
        intent.putExtra(EXTRA_CONFIGURATION_STATE, MeshNodeStates.MeshNodeStatus.SUBSCRIPTION_STATUS_RECEIVED.getState());
        intent.putExtra(EXTRA_IS_SUCCESS, success);
        intent.putExtra(EXTRA_STATUS, status);
        intent.putExtra(EXTRA_ELEMENT_ADDRESS, elementAddress);
        intent.putExtra(EXTRA_PUBLISH_ADDRESS, subscriptionAddress);
        intent.putExtra(EXTRA_MODEL_ID, modelIdentifier);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onGenericOnOffGetSent(final ProvisionedMeshNode node) {
        mMeshNode = node;
    }

    @Override
    public void onGenericOnOffSetSent(final ProvisionedMeshNode node, final boolean presentOnOff, final boolean targetOnOff, final int remainingTime) {
        mMeshNode = node;
    }

    @Override
    public void onGenericOnOffSetUnacknowledgedSent(final ProvisionedMeshNode node) {
        mMeshNode = node;
        final Intent intent = new Intent(ACTION_GENERIC_STATE);
        intent.putExtra(EXTRA_GENERIC_ON_OFF_SET_UNACK, "");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onGenericOnOffStatusReceived(final ProvisionedMeshNode node, final boolean presentOnOff, final Boolean targetOnOff, final int transitionSteps, final int transitionResolution) {
        mMeshNode = node;
        final Intent intent = new Intent(ACTION_GENERIC_ON_OFF_STATE);
        intent.putExtra(EXTRA_GENERIC_PRESENT_STATE, presentOnOff);
        intent.putExtra(EXTRA_GENERIC_TARGET_STATE, targetOnOff);
        intent.putExtra(EXTRA_GENERIC_TRANSITION_STEPS, transitionSteps);
        intent.putExtra(EXTRA_GENERIC_TRANSITION_RES, transitionResolution);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onGenericLevelSetUnacknowledgedSent(ProvisionedMeshNode node) {
        // TODO
    }

    @Override
    public void onGenericLevelSetSent(ProvisionedMeshNode node, boolean presentOnOff, boolean targetOnOff, int remainingTime) {
        // TODO
    }

    @Override
    public void onGenericLevelGetSent(ProvisionedMeshNode node) {
        // TODO
    }

    @Override
    public void onGenericLevelStatusReceived(ProvisionedMeshNode node, int presentLevel, int targetLevel, int transitionSteps, int transitionResolution) {
        mMeshNode = node;
        final Intent intent = new Intent(ACTION_GENERIC_LEVEL_STATE);
        intent.putExtra(EXTRA_GENERIC_PRESENT_STATE, presentLevel);
        intent.putExtra(EXTRA_GENERIC_TARGET_STATE, targetLevel);
        intent.putExtra(EXTRA_GENERIC_TRANSITION_STEPS, transitionSteps);
        intent.putExtra(EXTRA_GENERIC_TRANSITION_RES, transitionResolution);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onUnacknowledgedVendorModelMessageSent(final ProvisionedMeshNode node) {
        mMeshNode = node;
    }

    @Override
    public void onAcknowledgedVendorModelMessageSent(final ProvisionedMeshNode node) {
        mMeshNode = node;
    }

    @Override
    public void onVendorModelMessageStatusReceived(final ProvisionedMeshNode node, final byte[] pdu) {
        mMeshNode = node;
        final Intent intent = new Intent(ACTION_VENDOR_MODEL_MESSAGE_STATE);
        intent.putExtra(EXTRA_DATA, pdu);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onMeshNodeResetSent(final ProvisionedMeshNode node) {
        mMeshNode = node;
        final Intent intent = new Intent(ACTION_CONFIGURATION_STATE);
        intent.putExtra(EXTRA_DATA_NODE_RESET, "");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onMeshNodeResetStatusReceived(final ProvisionedMeshNode node) {
        if(node != null) {
            mMeshNode = null;
            mElement = null;
            mMeshModel = null;
            final Intent intent = new Intent(ACTION_CONFIGURATION_STATE);
            intent.putExtra(EXTRA_DATA_NODE_RESET_STATUS, MeshNodeStates.MeshNodeStatus.NODE_RESET_STATUS_RECEIVED.getState());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private void handleConnectivityStates(final boolean connected) {
        //Check if provisioning is complete
        if (mIsProvisioningComplete) {
            //We do a startScan upon provisioning is complete so check for that
            if (mIsReconnecting) {
                sendBroadcastReconnecting(true);
            } else if (mIsConfigurationComplete && !connected) {
                //If its not reconnecting, may be the device died/linkloss
                mIsConnected = false;
                sendBroadcastIsConnected(false);
            }
        } else {
            if (!connected) {
                mIsConnected = false;
                sendBroadcastIsConnected(false);
            }
        }
    }

    /**
     * Broadcast the device connected state
     *
     * @param isConnected boolean containing connected/disconnected state
     */
    private void sendBroadcastIsConnected(final boolean isConnected) {
        final Intent intent = new Intent(ACTION_IS_CONNECTED);
        intent.putExtra(EXTRA_DATA, isConnected);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Broadcast the device ready state
     *
     * @param isReady boolean containing the ready state
     */
    private void sendBroadcastDeviceReady(final boolean isReady) {
        final Intent intent = new Intent(ACTION_ON_DEVICE_READY);
        intent.putExtra(EXTRA_DATA, isReady);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Broadcast the reconnecting state
     *
     * @param isReconnecting boolean containing the reconnecting state
     */
    private void sendBroadcastReconnecting(final boolean isReconnecting) {
        final Intent intent = new Intent(ACTION_IS_RECONNECTING);
        intent.putExtra(EXTRA_DATA, isReconnecting);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Broadcast the connectivity state
     *
     * @param connectionState string containing the state
     */
    private void sendBroadcastConnectivityState(final String connectionState) {
        final Intent intent = new Intent(ACTION_CONNECTION_STATE);
        intent.putExtra(EXTRA_DATA, connectionState);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Starts reconnecting to the device
     */
    private void startScan() {
        if (mIsScanning)
            return;

        mIsScanning = true;
        sendBroadcastConnectivityState(getString(R.string.state_scanning_provisioned_node, mDeviceName));
        // Scanning settings
        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                // Refresh the devices list every second
                .setReportDelay(0)
                // Hardware filtering has some issues on selected devices
                .setUseHardwareFilteringIfSupported(false)
                // Samsung S6 and S6 Edge report equal value of RSSI for all devices. In this app we ignore the RSSI.
                /*.setUseHardwareBatchingIfSupported(false)*/
                .build();

        // Let's use the filter to scan only for Blinky devices
        final List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid((MESH_PROXY_UUID))).build());

        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.startScan(filters, settings, scanCallback);
        Log.v(TAG, "Scan started");
        mHandler.postDelayed(mScannerTimeout, 20000);
    }

    /**
     * stop scanning for bluetooth devices.
     */
    private void stopScan() {
        mHandler.removeCallbacks(mScannerTimeout);
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.stopScan(scanCallback);
        mIsScanning = false;
    }

    private void onProvisionedDeviceFound(final ProvisionedMeshNode node, final ExtendedBluetoothDevice device) {
        node.setBluetoothDeviceAddress(device.getAddress());
        mMeshNode = node;
        connect(device);
    }

    /**
     * Connect to peripheral
     *
     * @param device bluetooth device
     */
    public void connect(final ExtendedBluetoothDevice device) {
        final LogSession logSession = Logger.newSession(getApplicationContext(), null, device.getAddress(), device.getName());
        mBleMeshManager.setLogger(logSession);
        mBluetoothDevice = device.getDevice();
        mDeviceName = device.getName();
        mBleMeshManager.connect(device.getDevice());
    }

    public class MeshServiceBinder extends Binder {

        public MeshManagerApi getMeshManagerApi() {
            return mMeshManagerApi;
        }

        /**
         * Connect to peripheral
         *
         * @param device bluetooth device
         */
        public void connect(final ExtendedBluetoothDevice device) {
            final LogSession logSession = Logger.newSession(getApplicationContext(), null, device.getAddress(), device.getName());
            mBleMeshManager.setLogger(logSession);
            mBluetoothDevice = device.getDevice();
            mDeviceName = device.getName();
            mBleMeshManager.connect(device.getDevice());
        }

        /**
         * Disconnects from peripheral
         */
        public void disconnect() {
            stopScan();
            mHandler.removeCallbacks(mReconnectRunnable);
            mIsReconnecting = false;
            mIsProvisioningComplete = false;
            mIsConfigurationComplete = false;
            mBleMeshManager.disconnect();
        }

        public Map<Integer, ProvisionedMeshNode> getProvisionedNodes() {
            return mMeshManagerApi.getProvisionedNodes();
        }

        /**
         * Send composition data get message
         *
         * @param meshNode meshnode
         */
        public void sendCompositionDataGet(final ProvisionedMeshNode meshNode) {
            mShouldAddAppKeyBeAdded = false;
            mMeshManagerApi.getCompositionData(meshNode);
        }

        public void saveApplicationKeys(final Map<Integer, String> appKeys) {
            Utils.saveApplicationKeys(getApplicationContext(), appKeys);
        }

        public String getNetworkId() {
            final byte[] networkKey = MeshParserUtils.toByteArray(mProvisioningSettings.getNetworkKey());
            return mMeshManagerApi.generateNetworkId(networkKey);
        }

        public BaseMeshNode getMeshNode() {
            return mMeshNode;
        }

        public void setMeshNode(final ProvisionedMeshNode node) {
            if (node != null) {
                mMeshNode = node;
            }
        }

        public Element getElement() {
            return mElement;
        }

        /**
         * Selects the mesh model to be configured
         *
         * @param element element belonging to the node
         */
        public void setElement(final Element element) {
            mElement = element;
        }

        public MeshModel getMeshModel() {
            return mMeshModel;
        }

        /**
         * Selects the mesh model to be configured
         *
         * @param meshModel updates the mesh model
         */
        public void setMeshModel(final MeshModel meshModel) {
            mMeshModel = meshModel;
        }

        public ConfigModelPublicationStatusLiveData getConfigModelPublicationStatus() {
            return mConfigModelPublicationStatus;

        }

        public void identifyNode(final String nodeName){
            mIsProvisioningComplete = false;
            mIsConfigurationComplete = false;
            final String networkKey = mProvisioningSettings.getNetworkKey();
            final int keyIndex = mProvisioningSettings.getKeyIndex();
            final int flags = mProvisioningSettings.getFlags();
            final int ivIndex = mProvisioningSettings.getIvIndex();
            final int unicastAddress = mProvisioningSettings.getUnicastAddress();
            final int globalTtl = mProvisioningSettings.getGlobalTtl();
            final BluetoothDevice device = mBluetoothDevice;
            mMeshManagerApi.identifyNode(device.getAddress(), nodeName);
        }

        public void startProvisioning() {
            mIsProvisioningComplete = false;
            mIsConfigurationComplete = false;
            final String networkKey = mProvisioningSettings.getNetworkKey();
            final int keyIndex = mProvisioningSettings.getKeyIndex();
            final int flags = mProvisioningSettings.getFlags();
            final int ivIndex = mProvisioningSettings.getIvIndex();
            final int unicastAddress = mProvisioningSettings.getUnicastAddress();
            final int globalTtl = mProvisioningSettings.getGlobalTtl();
            final BluetoothDevice device = mBluetoothDevice;
            mMeshManagerApi.startProvisioning((UnprovisionedMeshNode) mMeshNode);
        }

        public void confirmProvisioning(final String pin) {
            mMeshManagerApi.setProvisioningConfirmation(pin);
        }

        public BluetoothDevice getBluetoothDevice() {
            return mBleMeshManager.getBluetoothDevice();
        }

        public boolean isConnected() {
            return mBleMeshManager.isConnected();
        }

        /**
         * Get composition data from the node
         *
         * @param node corresponding mesh node
         */
        public void sendGetCompositionData(final ProvisionedMeshNode node) {
            mMeshNode = node;
            mMeshManagerApi.getCompositionData(node);
        }

        public void sendAppKeyAdd(final int appKeyIndex, final String appKey) {
            mAppKeyIndex = appKeyIndex;
            mAppKey = appKey;
            mMeshManagerApi.addAppKey((ProvisionedMeshNode) mMeshNode, appKeyIndex, appKey);
        }

        public void sendAppKeyAdd(final ProvisionedMeshNode meshNode, final int appKeyIndex, final String appKey) {
            mMeshNode = meshNode;
            mAppKeyIndex = appKeyIndex;
            mAppKey = appKey;
            mMeshManagerApi.addAppKey(meshNode, appKeyIndex, appKey);
        }

        /**
         * Binds appkey to model
         *
         * @param meshNode       corresponding mesh node
         * @param elementAddress element address in the node
         * @param meshModel      mesh model
         * @param appKeyIndex    index of the application key that has already been added to the mesh node
         */
        public void sendBindAppKey(final ProvisionedMeshNode meshNode, final byte[] elementAddress, final MeshModel meshModel, final int appKeyIndex) {
            mMeshManagerApi.bindAppKey(meshNode, elementAddress, meshModel, appKeyIndex);
        }

        public void sendUnbindAppKey(final ProvisionedMeshNode meshNode, final byte[] elementAddress, final MeshModel meshModel, final int appKeyIndex) {
            mMeshManagerApi.unbindAppKey(meshNode, elementAddress, meshModel, appKeyIndex);
        }

        public void sendConfigModelPublicationSet(final ConfigModelPublicationSetParams configModelPublicationSetParams) {
            mMeshManagerApi.sendConfigModelPublicationSet(configModelPublicationSetParams);
        }

        public void sendConfigModelSubscriptionAdd(final ProvisionedMeshNode node, final Element element, final MeshModel meshModel, final byte[] subsciptionAddress) {
            mMeshManagerApi.addSubscriptionAddress(node,
                    element.getElementAddress(), subsciptionAddress, meshModel.getModelId());
        }

        public void sendConfigModelSubscriptionDelete(final ProvisionedMeshNode node, final Element element, final MeshModel meshModel, final byte[] subsciptionAddress) {
            mMeshManagerApi.deleteSubscriptionAddress(node,
                    element.getElementAddress(), subsciptionAddress, meshModel.getModelId());
        }

        public ProvisionedMeshNode getMeshNode(final int unicastAddress) {
            return mMeshManagerApi.getProvisionedNodes().get(unicastAddress);
        }

        /**
         * Reset mesh network
         */
        public void resetMeshNetwork() {
            mMeshManagerApi.resetMeshNetwork();
            mMeshNode = null;
            mElement = null;
            mMeshModel = null;
            mAppKeyIndex = 0;
            mAppKey = null;
        }

        public void setSelectedAppkey(final int appKeyIndex, final String appkey) {
            mAppKeyIndex = appKeyIndex;
            mAppKey = appkey;
        }

        public String getSelectedAppKey() {
            return mAppKey;
        }

        public ProvisioningSettings getProvisioningSettings() {
            return mMeshManagerApi.getProvisioningSettings();
        }

        /**
         * Send generic on off get to mesh node
         *
         * @param node                 mesh node to send generic on off get
         * @param model                model identifier
         * @param address              address to which the message must be sent to to which this model belongs to
         */
        public void sendGenericOnOffGet(final ProvisionedMeshNode node, final MeshModel model, final byte[] address, final int appKeyIndex) {
            mMeshManagerApi.getGenericOnOff(node, model, address, appKeyIndex);
        }

        /**
         * Send generic on off set to mesh node
         *
         * @param node                 mesh node to send generic on off get
         * @param model                model identifier
         * @param address              address to which the message must be sent to to which this model belongs to
         * @param transitionSteps      the number of steps
         * @param transitionResolution the resolution for the number of steps
         * @param delay                message execution delay in 5ms steps. After this delay milliseconds the model will execute the required behaviour.
         * @param state                on off state
         */
        public void sendGenericOnOffSet(final ProvisionedMeshNode node, final MeshModel model, final byte[] address, final int appKeyIndex,
                                        final Integer transitionSteps, final Integer transitionResolution, final Integer delay, final boolean state) {
            mMeshManagerApi.setGenericOnOff(node, model, address, appKeyIndex, transitionSteps, transitionResolution, delay, state);
        }

        /**
         * Send generic on off set unacknowledged to mesh node
         *
         * @param node                 mesh node to send generic on off get
         * @param model                model identifier
         * @param address              address to which the message must be sent to to which this model belongs to
         * @param transitionSteps      the number of steps
         * @param transitionResolution the resolution for the number of steps
         * @param delay                message execution delay in 5ms steps. After this delay milliseconds the model will execute the required behaviour.
         * @param state                on off state
         */
        public void sendGenericOnOffSetUnacknowledged(final ProvisionedMeshNode node, final MeshModel model, final byte[] address, final int appKeyIndex,
                                                      final Integer transitionSteps, final Integer transitionResolution, final Integer delay, final boolean state) {
            mMeshManagerApi.setGenericOnOffUnacknowledged(node, model, address, appKeyIndex, transitionSteps, transitionResolution, delay, state);
        }

        public void resetMeshNode(final ProvisionedMeshNode provisionedMeshNode) {
            mMeshManagerApi.resetMeshNode(provisionedMeshNode);
        }

        public void sendVendorModelUnacknowledgedMessage(final ProvisionedMeshNode node, final MeshModel model, final byte[] address, final int appKeyIndex, final int opcode, final byte[] parameters) {
            mMeshManagerApi.sendVendorModelUnacknowledgedMessage(node, model, address, appKeyIndex, opcode, parameters);
        }

        public void sendVendorModelAcknowledgedMessage(final ProvisionedMeshNode node, final MeshModel model, final byte[] address, final int appKeyIndex, final int opcode, final byte[] parameters) {
            mMeshManagerApi.sendVendorModelAcknowledgedMessage(node, model, address, appKeyIndex, opcode, parameters);
        }

        /**
         * Send generic level get to mesh node
         *
         * @param node                 mesh node to send generic on off get
         * @param model                model identifier
         * @param address              address to which the message must be sent to to which this model belongs to
         */
        public void sendGenericLevelGet(final ProvisionedMeshNode node, final MeshModel model, final byte[] address, final int appKeyIndex) {
            mMeshManagerApi.getGenericLevel(node, model, address, appKeyIndex);
        }

        public void sendGenericLevelSet(final ProvisionedMeshNode node, final MeshModel model, final byte[] address, final int appKeyIndex,
                                        final Integer transitionSteps, final Integer transitionResolution, final Integer delay, final int level) {
            mMeshManagerApi.setGenericLevel(node, model, address, appKeyIndex, transitionSteps, transitionResolution, delay, level);
        }

        public void sendGenericLevelSetUnacknowledged(final ProvisionedMeshNode node, final MeshModel model, final byte[] address, final int appKeyIndex,
                                                      final Integer transitionSteps, final Integer transitionResolution, final Integer delay, final int level) {
            mMeshManagerApi.setGenericLevelUnacknowledged(node, model, address, appKeyIndex, transitionSteps, transitionResolution, delay, level);
        }
    }
}
