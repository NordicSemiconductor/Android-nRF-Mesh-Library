package no.nordicsemi.android.nrfmeshprovisioner.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.meshprovisioner.Group;
import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.meshprovisioner.MeshManagerCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.MeshProvisioningStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.Provisioner;
import no.nordicsemi.android.meshprovisioner.UnprovisionedBeacon;
import no.nordicsemi.android.meshprovisioner.models.SigModelParser;
import no.nordicsemi.android.meshprovisioner.provisionerstates.ProvisioningState;
import no.nordicsemi.android.meshprovisioner.provisionerstates.UnprovisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.ConfigAppKeyAdd;
import no.nordicsemi.android.meshprovisioner.transport.ConfigAppKeyStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigCompositionDataGet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigCompositionDataStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelAppStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelPublicationStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelSubscriptionStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNetworkTransmitStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNodeResetStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigProxyStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigRelayStatus;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.GenericLevelStatus;
import no.nordicsemi.android.meshprovisioner.transport.GenericOnOffStatus;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.transport.ProxyConfigFilterStatus;
import no.nordicsemi.android.meshprovisioner.transport.VendorModelMessageStatus;
import no.nordicsemi.android.meshprovisioner.utils.AddressUtils;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManager;
import no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManagerCallbacks;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManager.MESH_PROXY_UUID;

@SuppressWarnings("unused")
public class NrfMeshRepository implements MeshProvisioningStatusCallbacks, MeshStatusCallbacks, MeshManagerCallbacks, BleMeshManagerCallbacks {

    private static final String TAG = NrfMeshRepository.class.getSimpleName();
    private static final int ATTENTION_TIMER = 5;
    public static final String EXPORT_PATH = Environment.getExternalStorageDirectory() + File.separator +
            "Nordic Semiconductor" + File.separator + "nRF Mesh" + File.separator;
    private static final String EXPORTED_PATH = "sdcard" + File.separator + "Nordic Semiconductor" + File.separator + "nRF Mesh" + File.separator;

    /**
     * Connection States Connecting, Connected, Disconnecting, Disconnected etc.
     **/
    private final MutableLiveData<Boolean> mIsConnectedToProxy = new MutableLiveData<>();

    /**
     * Live data flag containing connected state.
     **/
    private MutableLiveData<Boolean> mIsConnected;

    /**
     * LiveData to notify when device is ready
     **/
    private final MutableLiveData<Void> mOnDeviceReady = new MutableLiveData<>();

    /**
     * Updates the connection state while connecting to a peripheral
     **/
    private final MutableLiveData<String> mConnectionState = new MutableLiveData<>();

    /**
     * Flag to determine if a reconnection is in the progress when provisioning has completed
     **/
    private final SingleLiveEvent<Boolean> mIsReconnecting = new SingleLiveEvent<>();

    private final MutableLiveData<UnprovisionedMeshNode> mUnprovisionedMeshNodeLiveData = new MutableLiveData<>();

    private final MutableLiveData<ProvisionedMeshNode> mProvisionedMeshNodeLiveData = new MutableLiveData<>();

    private final SingleLiveEvent<Integer> mConnectedMeshNodeAddress = new SingleLiveEvent<>();
    /**
     * Contains the initial provisioning live data
     **/
    private NetworkInformationLiveData mNetworkInformationLiveData;

    /**
     * Flag to determine if provisioning was completed
     **/
    private boolean mIsProvisioningComplete = false;

    /**
     * Holds the selected MeshNode to configure
     **/
    private MutableLiveData<ProvisionedMeshNode> mExtendedMeshNode = new MutableLiveData<>();

    /**
     * Holds the selected Element to configure
     **/
    private MutableLiveData<Element> mSelectedElement = new MutableLiveData<>();

    /**
     * Holds the selected mesh model to configure
     **/
    private MutableLiveData<MeshModel> mSelectedModel = new MutableLiveData<>();

    private final MutableLiveData<Group> mSelectedGroupLiveData = new MutableLiveData<>();
    /**
     * Composition data status
     **/
    final SingleLiveEvent<ConfigCompositionDataStatus> mCompositionDataStatus = new SingleLiveEvent<>();

    /**
     * App key add status
     **/
    final SingleLiveEvent<ConfigAppKeyStatus> mAppKeyStatus = new SingleLiveEvent<>();

    /**
     * Contains the MeshNetwork
     **/
    private MeshNetworkLiveData mMeshNetworkLiveData = new MeshNetworkLiveData();

    private SingleLiveEvent<String> mNetworkImportState = new SingleLiveEvent<>();
    private SingleLiveEvent<String> mNetworkExportState = new SingleLiveEvent<>();

    private SingleLiveEvent<MeshMessage> mMeshMessageLiveData = new SingleLiveEvent<>();
    /**
     * Contains the provisioned nodes
     **/
    private final MutableLiveData<List<ProvisionedMeshNode>> mProvisionedNodes = new MutableLiveData<>();

    private final MutableLiveData<List<Group>> mGroups = new MutableLiveData<>();

    private final TransactionStatusLiveData mTransactionFailedLiveData = new TransactionStatusLiveData();

    private MeshManagerApi mMeshManagerApi;
    private BleMeshManager mBleMeshManager;
    private Handler mHandler;
    private UnprovisionedMeshNode mUnprovisionedMeshNode;
    private ProvisionedMeshNode mProvisionedMeshNode;
    private boolean mIsReconnectingFlag;
    private boolean mIsScanning;
    private boolean mSetupProvisionedNode;
    private ProvisioningStatusLiveData mProvisioningStateLiveData;
    private MeshNetwork mMeshNetwork;
    private boolean mIsCompositionDataReceived;
    private boolean mIsAppKeyAddCompleted;

    private final Runnable mReconnectRunnable = this::startScan;

    private final Runnable mScannerTimeout = () -> {
        stopScan();
        mIsReconnecting.postValue(false);
    };

    public NrfMeshRepository(final MeshManagerApi meshManagerApi,
                             final NetworkInformation networkInformation,
                             final BleMeshManager bleMeshManager) {
        //Initialize the mesh api
        mMeshManagerApi = meshManagerApi;
        mMeshManagerApi.setMeshManagerCallbacks(this);
        mMeshManagerApi.setProvisioningStatusCallbacks(this);
        mMeshManagerApi.setMeshStatusCallbacks(this);
        mMeshManagerApi.loadMeshNetwork();
        //Initialize the ble manager
        mBleMeshManager = bleMeshManager;
        mBleMeshManager.setGattCallbacks(this);
        mHandler = new Handler();
    }

    void clearInstance() {
        mBleMeshManager = null;
    }

    /**
     * Returns {@link SingleLiveEvent} containing the device ready state.
     */
    LiveData<Void> isDeviceReady() {
        return mOnDeviceReady;
    }

    /**
     * Returns {@link SingleLiveEvent} containing the device ready state.
     */
    LiveData<String> getConnectionState() {
        return mConnectionState;
    }

    /**
     * Returns {@link SingleLiveEvent} containing the device ready state.
     */
    LiveData<Boolean> isConnected() {
        return mIsConnected;
    }

    /**
     * Returns {@link SingleLiveEvent} containing the device ready state.
     */
    LiveData<Boolean> isConnectedToProxy() {
        return mIsConnectedToProxy;
    }

    LiveData<Boolean> isReconnecting() {
        return mIsReconnecting;
    }

    boolean isProvisioningComplete() {
        return mIsProvisioningComplete;
    }

    boolean isCompositionDataStatusReceived() {
        return mIsCompositionDataReceived;
    }

    boolean isAppKeyAddCompleted() {
        return mIsAppKeyAddCompleted;
    }

    final MeshNetworkLiveData getMeshNetworkLiveData() {
        return mMeshNetworkLiveData;
    }

    LiveData<List<ProvisionedMeshNode>> getProvisionedNodes() {
        return mProvisionedNodes;
    }

    LiveData<List<Group>> getGroups() {
        return mGroups;
    }

    LiveData<String> getNetworkLoadState() {
        return mNetworkImportState;
    }

    LiveData<String> getNetworkExportState() {
        return mNetworkExportState;
    }

    NetworkInformationLiveData getNetworkInformationLiveData() {
        return mNetworkInformationLiveData;
    }

    ProvisioningStatusLiveData getProvisioningState() {
        return mProvisioningStateLiveData;
    }

    TransactionStatusLiveData getTransactionStatusLiveData() {
        return mTransactionFailedLiveData;
    }

    /**
     * Returns the mesh manager api
     *
     * @return {@link MeshManagerApi}
     */
    MeshManagerApi getMeshManagerApi() {
        return mMeshManagerApi;
    }

    /**
     * Returns the ble mesh manager
     *
     * @return {@link BleMeshManager}
     */
    BleMeshManager getBleMeshManager() {
        return mBleMeshManager;
    }

    /**
     * Returns the {@link MeshMessageLiveData} live data object containing the mesh message
     */
    public LiveData<MeshMessage> getMeshMessageLiveData() {
        return mMeshMessageLiveData;
    }

    LiveData<Group> getSelectedGroup() {
        return mSelectedGroupLiveData;
    }

    /**
     * Reset mesh network
     */
    void resetMeshNetwork() {
        disconnect();
        mMeshManagerApi.resetMeshNetwork();
    }

    /**
     * Connect to peripheral
     *
     * @param device bluetooth device
     */
    void connect(final Context context, final ExtendedBluetoothDevice device, final boolean connectToNetwork) {
        mMeshNetworkLiveData.getValue().setNodeName(device.getName());
        mIsProvisioningComplete = false;
        mIsCompositionDataReceived = false;
        mIsAppKeyAddCompleted = false;
        clearExtendedMeshNode();
        final LogSession logSession = Logger.newSession(context, null, device.getAddress(), device.getName());
        mBleMeshManager.setLogger(logSession);
        final BluetoothDevice bluetoothDevice = device.getDevice();
        initIsConnectedLiveData(connectToNetwork);
        mConnectionState.postValue("Connecting....");
        //Added a 1 second delay for connection, mostly to wait for a disconnection to complete before connecting.
        mHandler.postDelayed(() -> mBleMeshManager.connect(bluetoothDevice), 1000);
    }

    /**
     * Connect to peripheral
     *
     * @param device bluetooth device
     */
    private void connectToProxy(final ExtendedBluetoothDevice device) {
        initIsConnectedLiveData(true);
        mConnectionState.postValue("Connecting....");
        mBleMeshManager.connect(device.getDevice());
    }

    private void initIsConnectedLiveData(final boolean connectToNetwork) {
        if (connectToNetwork) {
            mIsConnected = new SingleLiveEvent<>();
        } else {
            mIsConnected = new MutableLiveData<>();
        }
    }

    /**
     * Disconnects from peripheral
     */
    void disconnect() {
        clearProvisioningLiveData();
        removeCallbacks();
        mIsProvisioningComplete = false;
        mBleMeshManager.disconnect();
    }

    void removeCallbacks() {
        mHandler.removeCallbacksAndMessages(null);
    }

    public void identifyNode(final ExtendedBluetoothDevice device) {
        final UnprovisionedBeacon beacon = (UnprovisionedBeacon) device.getBeacon();
        if (beacon != null) {
            mMeshManagerApi.identifyNode(beacon.getUuid(), device.getName(), ATTENTION_TIMER);
        } else {
            final byte[] serviceData = Utils.getServiceData(device.getScanResult(), BleMeshManager.MESH_PROVISIONING_UUID);
            if (serviceData != null) {
                final UUID uuid = mMeshManagerApi.getDeviceUuid(serviceData);
                mMeshManagerApi.identifyNode(uuid, device.getName(), ATTENTION_TIMER);
            }
        }
    }

    void clearProvisioningLiveData() {
        mIsReconnectingFlag = false;
        mUnprovisionedMeshNodeLiveData.setValue(null);
        mProvisionedMeshNodeLiveData.setValue(null);
    }

    private void clearExtendedMeshNode() {
        if (mExtendedMeshNode != null) {
            mExtendedMeshNode.postValue(null);
        }
    }

    LiveData<UnprovisionedMeshNode> getUnprovisionedMeshNode() {
        return mUnprovisionedMeshNodeLiveData;
    }

    LiveData<ProvisionedMeshNode> getProvisionedMeshNode() {
        return mProvisionedMeshNodeLiveData;
    }

    LiveData<Integer> getConnectedMeshNodeAddress() {
        return mConnectedMeshNodeAddress;
    }

    /**
     * Returns the selected mesh node
     */
    LiveData<ProvisionedMeshNode> getSelectedMeshNode() {
        return mExtendedMeshNode;
    }

    /**
     * Sets the mesh node to be configured
     *
     * @param node provisioned mesh node
     */
    void setSelectedMeshNode(final ProvisionedMeshNode node) {
        mExtendedMeshNode.postValue(node);
        /*if (mExtendedMeshNode == null) {
            mExtendedMeshNode = new ExtendedMeshNode(node);
        } else {
            mExtendedMeshNode.updateMeshNode(node);
        }*/
    }

    /**
     * Returns the selected element
     */
    LiveData<Element> getSelectedElement() {
        return mSelectedElement;
    }

    /**
     * Set the selected {@link Element} to be configured
     *
     * @param element element
     */
    void setSelectedElement(final Element element) {
        mSelectedElement.postValue(element);
    }

    /**
     * Returns the selected mesh model
     */
    LiveData<MeshModel> getSelectedModel() {
        return mSelectedModel;
    }

    /**
     * Set the selected model to be configured
     *
     * @param model mesh model
     */
    void setSelectedModel(final MeshModel model) {
        mSelectedModel.postValue(model);
    }

    @Override
    public void onDataReceived(final BluetoothDevice bluetoothDevice, final int mtu, final byte[] pdu) {
        mMeshManagerApi.handleNotifications(mtu, pdu);
    }

    @Override
    public void onDataSent(final BluetoothDevice device, final int mtu, final byte[] pdu) {
        mMeshManagerApi.handleWriteCallbacks(mtu, pdu);
    }

    @Override
    public void onDeviceConnecting(final BluetoothDevice device) {
        mConnectionState.postValue("Connecting....");
    }

    @Override
    public void onDeviceConnected(final BluetoothDevice device) {
        mIsConnected.postValue(true);
        mConnectionState.postValue("Discovering services....");
        mIsConnectedToProxy.postValue(true);
    }

    @Override
    public void onDeviceDisconnecting(final BluetoothDevice device) {
        Log.v(TAG, "Disconnecting...");
        if(mIsReconnectingFlag){
            mConnectionState.postValue("Reconnecting...");
        } else {
            mConnectionState.postValue("Disconnecting...");
        }
    }

    @Override
    public void onDeviceDisconnected(final BluetoothDevice device) {
        Log.v(TAG, "Disconnected");
        if (mIsReconnectingFlag) {
            mIsReconnectingFlag = false;
            mIsReconnecting.postValue(false);
            mIsConnected.postValue(false);
        } else {
            mConnectionState.postValue("");
            mIsConnected.postValue(false);
            mIsConnectedToProxy.postValue(false);
            if (mConnectedMeshNodeAddress.getValue() != null) {
                final ProvisionedMeshNode node = mMeshManagerApi.getMeshNetwork().
                        getProvisionedNode(AddressUtils.getUnicastAddressBytes(mConnectedMeshNodeAddress.getValue()));
                if (node != null)
                    node.setProxyFilter(null);

            }
            clearExtendedMeshNode();
        }
        mSetupProvisionedNode = false;
        mConnectedMeshNodeAddress.postValue(null);
    }

    @Override
    public void onLinklossOccur(final BluetoothDevice device) {
        Log.v(TAG, "Link loss occurred");
        mIsConnected.postValue(false);
    }

    @Override
    public void onServicesDiscovered(final BluetoothDevice device, final boolean optionalServicesFound) {
        mConnectionState.postValue("Initializing...");
    }

    @Override
    public void onDeviceReady(final BluetoothDevice device) {
        mOnDeviceReady.postValue(null);

        if (mBleMeshManager.isProvisioningComplete()) {
            if (mSetupProvisionedNode) {
                //Adding a slight delay here so we don't send anything before we receive the mesh beacon message
                final ProvisionedMeshNode node = mProvisionedMeshNodeLiveData.getValue();
                final ConfigCompositionDataGet compositionDataGet = new ConfigCompositionDataGet();
                mHandler.postDelayed(() -> mMeshManagerApi.sendMeshMessage(node.getUnicastAddress(), compositionDataGet), 2000);
            }
            mIsConnectedToProxy.postValue(true);
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
        Log.e(TAG, "Error: " + message + " Error Code: " + errorCode + " Device: " + device.getAddress());
        mConnectionState.postValue(message);
    }

    @Override
    public void onDeviceNotSupported(final BluetoothDevice device) {

    }

    @Override
    public void onNetworkLoaded(final MeshNetwork meshNetwork) {
        loadNetwork(meshNetwork);
        loadGroups();
    }

    @Override
    public void onNetworkUpdated(final MeshNetwork meshNetwork) {
        loadNetwork(meshNetwork);
        loadGroups();
        updateSelectedGroup();
    }

    @Override
    public void onNetworkLoadFailed(final String error) {
        mNetworkImportState.postValue(error);
    }

    @Override
    public void onNetworkImported(final MeshNetwork meshNetwork) {
        //We can delete the old network after the import has been successful!
        //But let's make sure we don't delete the same network in case someone imports the same network ;)
        final MeshNetwork oldNet = mMeshNetwork;
        if (!oldNet.getMeshUUID().equals(meshNetwork.getMeshUUID())) {
            mMeshManagerApi.deleteMeshNetworkFromDb(oldNet);
        }
        loadNetwork(meshNetwork);
        loadGroups();
        mNetworkImportState.postValue(meshNetwork.getMeshName() + " has been successfully imported.\n" +
                "In order to start sending messages to this network, please change the provisioner address. " +
                "Using the same provisioner address will cause messages to be discarded due to the usage of incorrect sequence numbers " +
                "for this address. However if the network does not contain any nodes you do not need to change the address");
    }

    @Override
    public void onNetworkExported(final MeshNetwork meshNetwork) {
        mNetworkExportState.postValue(meshNetwork.getMeshName() + " has been successfully exported. " +
                "You can find the exported network information in the following path. " + EXPORTED_PATH);
    }

    @Override
    public void onNetworkExportedJson(MeshNetwork meshNetwork, String networkJson) {

    }

    @Override
    public void onNetworkExportFailed(final String error) {
        mNetworkExportState.postValue(error);

    }

    @Override
    public void onNetworkImportFailed(final String error) {
        mNetworkImportState.postValue(error);
    }

    @Override
    public void sendProvisioningPdu(final UnprovisionedMeshNode meshNode, final byte[] pdu) {
        mBleMeshManager.sendPdu(pdu);
    }

    @Override
    public void sendMeshPdu(final byte[] pdu) {
        mBleMeshManager.sendPdu(pdu);
    }

    @Override
    public int getMtu() {
        return mBleMeshManager.getMtuSize();
    }

    @Override
    public void onProvisioningStateChanged(final UnprovisionedMeshNode meshNode, final ProvisioningState.States state, final byte[] data) {
        mUnprovisionedMeshNode = meshNode;
        mUnprovisionedMeshNodeLiveData.postValue(meshNode);
        switch (state) {
            case PROVISIONING_INVITE:
                mProvisioningStateLiveData = new ProvisioningStatusLiveData();
                break;
            case PROVISIONING_FAILED:
                mIsProvisioningComplete = false;
                break;
        }
        mProvisioningStateLiveData.onMeshNodeStateUpdated(state);

    }

    @Override
    public void onProvisioningFailed(final UnprovisionedMeshNode meshNode, final ProvisioningState.States state, final byte[] data) {
        mUnprovisionedMeshNode = meshNode;
        mUnprovisionedMeshNodeLiveData.postValue(meshNode);
        switch (state) {
            case PROVISIONING_FAILED:
                mIsProvisioningComplete = false;
                break;
        }
        mProvisioningStateLiveData.onMeshNodeStateUpdated(state);

    }

    @Override
    public void onProvisioningCompleted(final ProvisionedMeshNode meshNode, final ProvisioningState.States state, final byte[] data) {
        mProvisionedMeshNode = meshNode;
        mUnprovisionedMeshNodeLiveData.postValue(null);
        mProvisionedMeshNodeLiveData.postValue(meshNode);
        switch (state) {
            case PROVISIONING_COMPLETE:
                onProvisioningCompleted(meshNode);
                break;
        }
        mProvisioningStateLiveData.onMeshNodeStateUpdated(state);

    }

    private void onProvisioningCompleted(final ProvisionedMeshNode node) {
        mIsProvisioningComplete = true;
        mProvisionedMeshNode = node;
        mIsReconnecting.postValue(true);
        mBleMeshManager.disconnect();
        mBleMeshManager.refreshDeviceCache();
        mProvisionedNodes.postValue(mMeshNetwork.getProvisionedNodes());
        mHandler.post(() -> mConnectionState.postValue("Scanning for provisioned node"));
        mHandler.postDelayed(mReconnectRunnable, 1000); //Added a slight delay to disconnect and refresh the cache
    }

    @Override
    public void onTransactionFailed(final byte[] dst, final boolean hasIncompleteTimerExpired) {
        mProvisionedMeshNode = mMeshNetwork.getProvisionedNode(dst);
        if (mTransactionFailedLiveData.hasActiveObservers()) {
            mTransactionFailedLiveData.onTransactionFailed(dst, hasIncompleteTimerExpired);
        }
    }

    @Override
    public void onTransactionFailed(final int dst, final boolean hasIncompleteTimerExpired) {
        mProvisionedMeshNode = mMeshNetwork.getProvisionedNode(dst);
        if (mTransactionFailedLiveData.hasActiveObservers()) {
            mTransactionFailedLiveData.onTransactionFailed(dst, hasIncompleteTimerExpired);
        }
    }

    @Override
    public void onUnknownPduReceived(final byte[] src, final byte[] accessPayload) {
        final ProvisionedMeshNode node = mMeshNetwork.getProvisionedNode(src);
        if (node != null) {
            mProvisionedMeshNode = node;
            updateNode(node);
        }
    }

    @Override
    public void onUnknownPduReceived(final int src, final byte[] accessPayload) {
        final ProvisionedMeshNode node = mMeshNetwork.getProvisionedNode(src);
        if (node != null) {
            mProvisionedMeshNode = node;
            updateNode(node);
        }
    }

    @Override
    public void onBlockAcknowledgementSent(final byte[] dst) {
        if (dst != null) {
            final ProvisionedMeshNode node = mMeshNetwork.getProvisionedNode(dst);
            if (node != null) {
                mProvisionedMeshNode = node;
                if (mSetupProvisionedNode) {
                    mProvisionedMeshNodeLiveData.postValue(mProvisionedMeshNode);
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisioningState.States.SENDING_BLOCK_ACKNOWLEDGEMENT);
                }
            }
        }
    }

    @Override
    public void onBlockAcknowledgementSent(final int dst) {
        final ProvisionedMeshNode node = mMeshNetwork.getProvisionedNode(dst);
        if (node != null) {
            mProvisionedMeshNode = node;
            if (mSetupProvisionedNode) {
                mProvisionedMeshNodeLiveData.postValue(mProvisionedMeshNode);
                mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisioningState.States.SENDING_BLOCK_ACKNOWLEDGEMENT);
            }
        }
    }

    @Override
    public void onBlockAcknowledgementReceived(final byte[] src) {
        final ProvisionedMeshNode node = mMeshNetwork.getProvisionedNode(src);
        if (node != null) {
            mProvisionedMeshNode = node;
            if (mSetupProvisionedNode) {
                mProvisionedMeshNodeLiveData.postValue(node);
                mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisioningState.States.BLOCK_ACKNOWLEDGEMENT_RECEIVED);
            }
        }
    }

    @Override
    public void onBlockAcknowledgementReceived(final int src) {
        final ProvisionedMeshNode node = mMeshNetwork.getProvisionedNode(src);
        if (node != null) {
            mProvisionedMeshNode = node;
            if (mSetupProvisionedNode) {
                mProvisionedMeshNodeLiveData.postValue(node);
                mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisioningState.States.BLOCK_ACKNOWLEDGEMENT_RECEIVED);
            }
        }
    }

    @Override
    public void onMeshMessageSent(final byte[] dst, final MeshMessage meshMessage) {
    }

    @Override
    public void onMeshMessageSent(final int dst, final MeshMessage meshMessage) {
        final ProvisionedMeshNode node = mMeshNetwork.getProvisionedNode(dst);
        if (node != null) {
            mProvisionedMeshNode = node;
            if (meshMessage instanceof ConfigCompositionDataGet) {
                if (mSetupProvisionedNode) {
                    mProvisionedMeshNodeLiveData.postValue(node);
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisioningState.States.COMPOSITION_DATA_GET_SENT);
                }
            } else if (meshMessage instanceof ConfigAppKeyStatus) {
                if (mSetupProvisionedNode) {
                    mProvisionedMeshNodeLiveData.postValue(node);
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisioningState.States.SENDING_APP_KEY_ADD);
                }
            }
        }
    }

    @Override
    public void onMeshMessageReceived(final byte[] src, final MeshMessage meshMessage) {
    }

    @Override
    public void onMeshMessageReceived(final int src, final MeshMessage meshMessage) {
        final ProvisionedMeshNode node = mMeshNetwork.getProvisionedNode(src);
        if (node != null)
            if (meshMessage instanceof ProxyConfigFilterStatus) {
                mProvisionedMeshNode = node;
                setSelectedMeshNode(node);
                final ProxyConfigFilterStatus status = (ProxyConfigFilterStatus) meshMessage;
                final int unicastAddress = status.getSrc();
                Log.v(TAG, "Proxy configuration source: " + MeshAddress.formatAddress(status.getSrc(), false));
                mConnectedMeshNodeAddress.postValue(unicastAddress);
                mMeshMessageLiveData.postValue(status);
            } else if (meshMessage instanceof ConfigCompositionDataStatus) {
                final ConfigCompositionDataStatus status = (ConfigCompositionDataStatus) meshMessage;
                if (mSetupProvisionedNode) {
                    mIsCompositionDataReceived = true;
                    mProvisionedMeshNodeLiveData.postValue(node);
                    mConnectedMeshNodeAddress.postValue(node.getUnicastAddress());
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisioningState.States.COMPOSITION_DATA_STATUS_RECEIVED);
                    //We send app key add after composition is complete. Adding a delay so that we don't send anything before the acknowledgement is sent out.
                    if (!mMeshNetwork.getAppKeys().isEmpty()) {
                        mHandler.postDelayed(() -> {
                            final ApplicationKey appKey = mMeshNetworkLiveData.getSelectedAppKey();
                            final ConfigAppKeyAdd configAppKeyAdd = new ConfigAppKeyAdd(node.getAddedNetworkKeys().get(0), appKey);
                            mMeshManagerApi.sendMeshMessage(node.getUnicastAddress(), configAppKeyAdd);
                        }, 2500);
                    }
                } else {
                    updateNode(node);
                }
            } else if (meshMessage instanceof ConfigAppKeyStatus) {
                final ConfigAppKeyStatus status = (ConfigAppKeyStatus) meshMessage;
                if (mSetupProvisionedNode) {
                    mIsAppKeyAddCompleted = true;
                    mSetupProvisionedNode = false;
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisioningState.States.APP_KEY_STATUS_RECEIVED);
                } else {
                    updateNode(node);
                    mMeshMessageLiveData.postValue(status);
                }
            } else if (meshMessage instanceof ConfigModelAppStatus) {

                if (updateNode(node)) {
                    final ConfigModelAppStatus status = (ConfigModelAppStatus) meshMessage;
                    final Element element = node.getElements().get(status.getElementAddress());
                    if (node.getElements().containsKey(status.getElementAddress())) {
                        mSelectedElement.postValue(element);
                        final MeshModel model = element.getMeshModels().get(status.getModelIdentifier());
                        mSelectedModel.postValue(model);
                    }
                }

            } else if (meshMessage instanceof ConfigModelPublicationStatus) {

                if (updateNode(node)) {
                    final ConfigModelPublicationStatus status = (ConfigModelPublicationStatus) meshMessage;
                    if (node.getElements().containsKey(status.getElementAddress())) {
                        final Element element = node.getElements().get(status.getElementAddress());
                        mSelectedElement.postValue(element);
                        final MeshModel model = element.getMeshModels().get(status.getModelIdentifier());
                        mSelectedModel.postValue(model);
                    }
                }

            } else if (meshMessage instanceof ConfigModelSubscriptionStatus) {

                if (updateNode(node)) {
                    final ConfigModelSubscriptionStatus status = (ConfigModelSubscriptionStatus) meshMessage;
                    if (node.getElements().containsKey(status.getElementAddress())) {
                        final Element element = node.getElements().get(status.getElementAddress());
                        mSelectedElement.postValue(element);
                        final MeshModel model = element.getMeshModels().get(status.getModelIdentifier());
                        mSelectedModel.postValue(model);
                    }
                }

            } else if (meshMessage instanceof ConfigNodeResetStatus) {

                final ConfigNodeResetStatus status = (ConfigNodeResetStatus) meshMessage;
                mExtendedMeshNode.postValue(null);
                mProvisionedNodes.postValue(mMeshNetwork.getProvisionedNodes());
                mMeshMessageLiveData.postValue(status);

            } else if (meshMessage instanceof ConfigNetworkTransmitStatus) {
                if (updateNode(node)) {
                    final ConfigNetworkTransmitStatus status = (ConfigNetworkTransmitStatus) meshMessage;
                    mMeshMessageLiveData.postValue(status);
                }

            } else if (meshMessage instanceof ConfigRelayStatus) {
                if (updateNode(node)) {
                    final ConfigRelayStatus status = (ConfigRelayStatus) meshMessage;
                    mMeshMessageLiveData.postValue(status);
                }

            } else if (meshMessage instanceof ConfigProxyStatus) {
                if (updateNode(node)) {
                    final ConfigProxyStatus status = (ConfigProxyStatus) meshMessage;
                    mMeshMessageLiveData.postValue(status);
                }

            } else if (meshMessage instanceof GenericOnOffStatus) {
                if (updateNode(node)) {
                    final GenericOnOffStatus status = (GenericOnOffStatus) meshMessage;
                    if (node.getElements().containsKey(status.getSrcAddress())) {
                        final Element element = node.getElements().get(status.getSrcAddress());
                        mSelectedElement.postValue(element);
                        final MeshModel model = element.getMeshModels().get((int) SigModelParser.GENERIC_ON_OFF_SERVER);
                        mSelectedModel.postValue(model);
                    }
                }
            } else if (meshMessage instanceof GenericLevelStatus) {

                if (updateNode(node)) {
                    final GenericLevelStatus status = (GenericLevelStatus) meshMessage;
                    if (node.getElements().containsKey(status.getSrcAddress())) {
                        final Element element = node.getElements().get(status.getSrcAddress());
                        mSelectedElement.postValue(element);
                        final MeshModel model = element.getMeshModels().get((int) SigModelParser.GENERIC_LEVEL_SERVER);
                        mSelectedModel.postValue(model);
                    }
                }

            } else if (meshMessage instanceof VendorModelMessageStatus) {

                if (updateNode(node)) {
                    final VendorModelMessageStatus status = (VendorModelMessageStatus) meshMessage;
                    if (node.getElements().containsKey(status.getSrcAddress())) {
                        final Element element = node.getElements().get(status.getSrcAddress());
                        mSelectedElement.postValue(element);
                        final MeshModel model = element.getMeshModels().get(status.getModelIdentifier());
                        mSelectedModel.postValue(model);
                    }
                }
            }

        if (mMeshMessageLiveData.hasActiveObservers()) {
            mMeshMessageLiveData.postValue(meshMessage);
        }

        //Refresh mesh network live data
        mMeshNetworkLiveData.refresh(mMeshManagerApi.getMeshNetwork());
    }

    @Override
    public void onMessageDecryptionFailed(final String meshLayer, final String errorMessage) {
        Log.e(TAG, "Decryption failed in " + meshLayer + " : " + errorMessage);
    }

    /**
     * Loads the network that was loaded from the db or imported from the mesh cdb
     *
     * @param meshNetwork mesh network that was loaded
     */
    private void loadNetwork(final MeshNetwork meshNetwork) {
        mMeshNetwork = meshNetwork;
        if (mMeshNetwork != null) {

            if (!mMeshNetwork.isProvisionerSelected()) {
                final Provisioner provisioner = meshNetwork.getProvisioners().get(0);
                provisioner.setLastSelected(true);
                mMeshNetwork.selectProvisioner(provisioner);
            }

            //Load live data with mesh network
            mMeshNetworkLiveData.loadNetworkInformation(meshNetwork);
            //Load live data with provisioned nodes
            mProvisionedNodes.postValue(mMeshNetwork.getProvisionedNodes());
        }
    }

    /**
     * We should only update the selected node, since sending messages to group address will notify with nodes that is not on the UI
     */
    private boolean updateNode(@NonNull final ProvisionedMeshNode node) {
        if (mProvisionedMeshNode.getUnicastAddress() == node.getUnicastAddress()) {
            mProvisionedMeshNode = node;
            mExtendedMeshNode.postValue(node);
            return true;
        }
        return false;
    }

    /**
     * Starts reconnecting to the device
     */
    private void startScan() {
        if (mIsScanning)
            return;

        mIsScanning = true;
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

        // Let's use the filter to scan only for Mesh devices
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

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(final int callbackType, final ScanResult result) {
            //In order to connectToProxy to the correct device, the hash advertised in the advertisement data should be matched.
            //This is to make sure we connectToProxy to the same device as device addresses could change after provisioning.
            final ScanRecord scanRecord = result.getScanRecord();
            if (scanRecord != null) {
                final byte[] serviceData = Utils.getServiceData(result, MESH_PROXY_UUID);
                if (serviceData != null) {
                    if (mMeshManagerApi.isAdvertisedWithNodeIdentity(serviceData)) {
                        final ProvisionedMeshNode node = mProvisionedMeshNode;
                        if (mMeshManagerApi.nodeIdentityMatches(node, serviceData)) {
                            stopScan();
                            mConnectionState.postValue("Provisioned node found");
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

    private void onProvisionedDeviceFound(final ProvisionedMeshNode node, final ExtendedBluetoothDevice device) {
        mSetupProvisionedNode = true;
        mProvisionedMeshNode = node;
        mIsReconnectingFlag = true;
        //Added an extra delay to ensure reconnection
        mHandler.postDelayed(() -> connectToProxy(device), 2000);
    }

    void importMeshNetwork(final Uri uri) {
        //We disconnect from the current mesh network before importing one
        mBleMeshManager.disconnect();
        mMeshManagerApi.importMeshNetwork(uri);
    }

    /**
     * Generates the groups based on the addresses each models have subscribed to
     */
    private void loadGroups() {
        final String uuid = mMeshNetwork.getMeshUUID();
        final List<Group> groups = new ArrayList<>();
        for (final ProvisionedMeshNode node : mMeshNetwork.getProvisionedNodes()) {
            for (Map.Entry<Integer, Element> elementEntry : node.getElements().entrySet()) {
                final Element element = elementEntry.getValue();
                for (Map.Entry<Integer, MeshModel> modelEntry : element.getMeshModels().entrySet()) {
                    final MeshModel model = modelEntry.getValue();
                    if (model != null) {
                        final List<Integer> subscriptionAddresses = model.getSubscribedAddresses();
                        for (Integer address : subscriptionAddresses) {
                            if (!mMeshNetwork.isGroupExist(address)) {
                                final Group group = new Group(address, uuid);
                                mMeshNetwork.addGroup(group);
                            }
                        }
                    }
                }
            }
        }
        mGroups.postValue(mMeshNetwork.getGroups());
    }

    private void updateSelectedGroup() {
        final Group selectedGroup = mSelectedGroupLiveData.getValue();
        if (selectedGroup != null) {
            mSelectedGroupLiveData.postValue(mMeshNetwork.getGroup(selectedGroup.getGroupAddress()));
        }
    }

    /**
     * Sets the group that was selected from the GroupAdapter.
     */
    void setSelectedGroup(final int address) {
        final Group group = mMeshNetwork.getGroup(address);
        if (group != null) {
            mSelectedGroupLiveData.postValue(group);
        }
    }
}
