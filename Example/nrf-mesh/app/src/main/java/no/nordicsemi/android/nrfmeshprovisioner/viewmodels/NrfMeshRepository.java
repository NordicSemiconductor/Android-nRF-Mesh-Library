package no.nordicsemi.android.nrfmeshprovisioner.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.meshprovisioner.BaseMeshNode;
import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.meshprovisioner.MeshManagerTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshProvisioningStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.meshmessagestates.MeshModel;
import no.nordicsemi.android.meshprovisioner.meshmessagestates.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.messages.ConfigAppKeyStatus;
import no.nordicsemi.android.meshprovisioner.messages.ConfigCompositionDataStatus;
import no.nordicsemi.android.meshprovisioner.messages.ConfigModelAppStatus;
import no.nordicsemi.android.meshprovisioner.messages.ConfigModelPublicationStatus;
import no.nordicsemi.android.meshprovisioner.messages.ConfigModelSubscriptionStatus;
import no.nordicsemi.android.meshprovisioner.messages.ConfigNodeResetStatus;
import no.nordicsemi.android.meshprovisioner.messages.GenericLevelStatus;
import no.nordicsemi.android.meshprovisioner.messages.GenericOnOffStatus;
import no.nordicsemi.android.meshprovisioner.messages.MeshMessage;
import no.nordicsemi.android.meshprovisioner.messages.VendorModelMessageStatus;
import no.nordicsemi.android.meshprovisioner.provisionerstates.UnprovisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.Element;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManager;
import no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManagerCallbacks;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ExtendedMeshModel;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ExtendedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.SingleLiveEvent;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.TransactionFailedLiveData;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManager.MESH_PROXY_UUID;

@SuppressWarnings("unused")
public class NrfMeshRepository implements MeshProvisioningStatusCallbacks, MeshStatusCallbacks, MeshManagerTransportCallbacks, BleMeshManagerCallbacks {

    private static final String TAG = NrfMeshRepository.class.getSimpleName();

    /**
     * Connection states Connecting, Connected, Disconnecting, Disconnected etc.
     **/
    private final MutableLiveData<Boolean> mIsConnectedToMesh = new MutableLiveData<>();

    /**
     * Connection states Connecting, Connected, Disconnecting, Disconnected etc.
     **/
    private final SingleLiveEvent<Boolean> mIsConnected = new SingleLiveEvent<>();

    /**
     * Flag to determine if the device is ready
     **/
    private final MutableLiveData<Boolean> mOnDeviceReady = new MutableLiveData<>();

    /**
     * Updates the connection state while connecting to a peripheral
     **/
    private final MutableLiveData<String> mConnectionState = new MutableLiveData<>();

    /**
     * Flag to determine if a reconnection is in the progress when provisioning has completed
     **/
    private final MutableLiveData<Boolean> mIsReconnecting = new MutableLiveData<>();

    private final SingleLiveEvent<Void> mNodeSetupComplete = new SingleLiveEvent<>();
    /**
     * Flag to determine if a reconnection is in the progress when provisioning has completed
     **/
    final MutableLiveData<byte[]> mConfigurationSrc = new MutableLiveData<>();

    private final MutableLiveData<BaseMeshNode> mMeshNodeLiveData = new MutableLiveData<>();

    private final NetworkInformation mNetworkInformation;

    /** Contains the initial provisioning live data **/
    private NetworkInformationLiveData mNetworkInformationLiveData;

    /** Contains the initial provisioning live data **/
    private final ProvisioningSettingsLiveData mProvisioningLiveData = new ProvisioningSettingsLiveData();

    /**
     * Flag to determine if provisioning was completed
     **/
    private boolean mIsProvisioningComplete = false;

    /**
     * Contains the {@link ExtendedMeshNode}
     **/
    ExtendedMeshNode mExtendedMeshNode;

    /**
     * Contains the {@link ExtendedMeshModel}
     **/
    ExtendedMeshModel mExtendedMeshModel;

    /**
     * Mesh model to configure
     **/
    final MutableLiveData<MeshModel> mMeshModel = new MutableLiveData<>();

    /**
     * Mesh model to configure
     **/
    final MutableLiveData<Element> mElement = new MutableLiveData<>();

    /**
     * App key add status
     **/
    final SingleLiveEvent<ConfigCompositionDataStatus> mCompositionDataStatus = new SingleLiveEvent<>();

    /**
     * App key add status
     **/
    final SingleLiveEvent<ConfigAppKeyStatus> mAppKeyStatus = new SingleLiveEvent<>();

    /**
     * App key bind status
     **/
    final SingleLiveEvent<ConfigModelAppStatus> mAppKeyBindStatus = new SingleLiveEvent<>();

    /**
     * publication status
     **/
    final SingleLiveEvent<ConfigModelPublicationStatus> mConfigModelPublicationStatus = new SingleLiveEvent<>();

    /**
     * Subscription bind status
     **/
    final SingleLiveEvent<ConfigModelSubscriptionStatus> mConfigModelSubscriptionStatus = new SingleLiveEvent<>();

    /**
     * Contains the initial provisioning live data
     **/
    private ProvisioningSettingsLiveData mProvisioningSettingsLiveData;

    /**
     * Contains the provisioned nodes
     **/
    private final MutableLiveData<Map<Integer, ProvisionedMeshNode>> mProvisionedNodes = new MutableLiveData<>();

    final TransactionFailedLiveData mTransactionFailedLiveData = new TransactionFailedLiveData();

    private static NrfMeshRepository mNrfMeshRepository;
    private final MeshManagerApi mMeshManagerApi;
    private final BleMeshManager mBleMeshManager;
    private BaseMeshNode mMeshNode;
    private boolean mIsReconnectingFlag;
    private final Handler mHandler;

    private boolean mIsScanning;


    private final Runnable mReconnectRunnable = this::startScan;
    private boolean mSetupProvisionedNode;

    private NrfMeshRepository(final MeshManagerApi meshManagerApi, final NetworkInformation networkInformation, final BleMeshManager bleMeshManager) {
        //Initialize the mesh api
        mMeshManagerApi = meshManagerApi;
        mMeshManagerApi.setProvisionerManagerTransportCallbacks(this);
        mMeshManagerApi.setProvisioningStatusCallbacks(this);
        mMeshManagerApi.setMeshStatusCallbacks(this);
        //Load live data with provisioned ndoes
        mProvisionedNodes.postValue(mMeshManagerApi.getProvisionedNodes());
        //Load live data with provisioning settings
        mProvisioningSettingsLiveData = new ProvisioningSettingsLiveData(mMeshManagerApi.getProvisioningSettings());
        //Load live data with configuration address
        mConfigurationSrc.postValue(mMeshManagerApi.getConfiguratorSrc());

        //Initialize the ble manager
        mBleMeshManager = bleMeshManager;
        mBleMeshManager.setGattCallbacks(this);

        mNetworkInformation = networkInformation;
        //Load live data with network information
        mNetworkInformationLiveData = new NetworkInformationLiveData(mNetworkInformation);
        mHandler = new Handler();
    }

    public static NrfMeshRepository getInstance(final MeshManagerApi meshManagerApi, final NetworkInformation networkInformation, final BleMeshManager bleMeshManager) {
        if (mNrfMeshRepository == null) {
            mNrfMeshRepository = new NrfMeshRepository(meshManagerApi, networkInformation, bleMeshManager);
        }
        return mNrfMeshRepository;
    }

    /**
     * Returns {@link SingleLiveEvent} containing the device ready state.
     */
    LiveData<Boolean> isDeviceReady() {
        return mOnDeviceReady;
    }

    /**
     * Returns {@link SingleLiveEvent} containing the device ready state.
     */
    public LiveData<String> getConnectionState() {
        return mConnectionState;
    }

    /**
     * Returns {@link SingleLiveEvent} containing the device ready state.
     */
    public LiveData<Boolean> isConnected() {
        return mIsConnected;
    }

    public LiveData<Boolean> isReconnecting() {
        return mIsReconnecting;
    }

    public boolean isProvisioningComplete() {
        return mIsProvisioningComplete;
    }

    LiveData<Void> isNodeSetupComplete() {
        return mNodeSetupComplete;
    }

    LiveData<Boolean> isConnectedToNetwork(){
        return mIsConnectedToMesh;
    }

    public LiveData<Map<Integer, ProvisionedMeshNode>> getProvisionedNodes() {
        return mProvisionedNodes;
    }

    final ProvisioningSettingsLiveData getProvisioningSettingsLiveData(){
        return mProvisioningSettingsLiveData;
    }

    NetworkInformationLiveData getNetworkInformationLiveData(){
        return mNetworkInformationLiveData;
    }

    LiveData<byte[]> getConfigurationSrcLiveData(){
        return mConfigurationSrc;
    }

    boolean setConfiguratorSrc(final byte[] configuratorSrc) {
        if(mMeshManagerApi.setConfiguratorSrc(configuratorSrc)) {
            mConfigurationSrc.postValue(mMeshManagerApi.getConfiguratorSrc());
            return true;
        }
        return false;
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
     * Reset mesh network
     */
    public void resetMeshNetwork() {
        disconnect();
        mMeshManagerApi.resetMeshNetwork();
        mProvisionedNodes.postValue(mMeshManagerApi.getProvisionedNodes());
        mNetworkInformation.refreshProvisioningData();
        mProvisioningLiveData.reset(mMeshManagerApi.getProvisioningSettings());
        mExtendedMeshNode = null;
    }

    /**
     * Connect to peripheral
     *
     * @param device bluetooth device
     */
    public void connect(final Context context, final ExtendedBluetoothDevice device) {
        mNetworkInformationLiveData.getValue();
        mIsProvisioningComplete = false;
        final LogSession logSession = Logger.newSession(context, null, device.getAddress(), device.getName());
        mBleMeshManager.setLogger(logSession);
        mBleMeshManager.connect(device.getDevice());
    }

    /**
     * Connect to peripheral
     *
     * @param device bluetooth device
     */
    private void connect(final ExtendedBluetoothDevice device) {
        mBleMeshManager.connect(device.getDevice());
    }

    /**
     * Disconnects from peripheral
     */
    public void disconnect() {
        mIsProvisioningComplete = false;
        mMeshNodeLiveData.postValue(null);
        mBleMeshManager.disconnect();
    }

    public LiveData<BaseMeshNode> getExtendedMeshNode() {
        return mMeshNodeLiveData;
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
    public void onDeviceConnecting(final BluetoothDevice device) {
        mConnectionState.postValue("Connecting....");
    }

    @Override
    public void onDeviceConnected(final BluetoothDevice device) {
        mIsConnected.postValue(true);
        mConnectionState.postValue("Discovering services....");
    }

    @Override
    public void onDeviceDisconnecting(final BluetoothDevice device) {
        mConnectionState.postValue("Disconnecting...");
        if(mIsReconnectingFlag) {
            mIsConnected.postValue(false);
        }
        mSetupProvisionedNode = false;
    }

    @Override
    public void onDeviceDisconnected(final BluetoothDevice device) {
        mConnectionState.postValue("Disconnected.");
        if(mIsReconnectingFlag) {
            mIsReconnectingFlag = false;
            mIsConnected.postValue(false);
        }
        mOnDeviceReady.postValue(false);
        mSetupProvisionedNode = false;
    }

    @Override
    public void onLinklossOccur(final BluetoothDevice device) {
        mIsConnected.postValue(false);
    }

    @Override
    public void onServicesDiscovered(final BluetoothDevice device, final boolean optionalServicesFound) {
        mConnectionState.postValue("Initializing...");
    }

    @Override
    public void onDeviceReady(final BluetoothDevice device) {
        mOnDeviceReady.postValue(true);

        if (mBleMeshManager.isProvisioningComplete()) {
            if (mSetupProvisionedNode) {
                //We update the bluetooth device after a startScan because some devices may start advertising with different mac address
                mMeshNode.setBluetoothDeviceAddress(device.getAddress());
                //Adding a slight delay here so we don't send anything before we receive the mesh beacon message
                mHandler.postDelayed(() -> mMeshManagerApi.getCompositionData((ProvisionedMeshNode) mMeshNode), 2000);
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
    public void sendPdu(final BaseMeshNode meshNode, final byte[] pdu) {
        mBleMeshManager.sendPdu(pdu);
    }

    @Override
    public int getMtu() {
        return mBleMeshManager.getMtuSize();
    }

    @Override
    public void onProvisioningInviteSent(final UnprovisionedMeshNode node) {
        mMeshNode = node;
        mMeshNodeLiveData.postValue(node);
    }

    @Override
    public void onProvisioningCapabilitiesReceived(final UnprovisionedMeshNode node) {
        mMeshNode = node;
        mMeshNodeLiveData.postValue(node);
    }

    @Override
    public void onProvisioningStartSent(final UnprovisionedMeshNode node) {
        mMeshNode = node;
        mMeshNodeLiveData.postValue(node);

    }

    @Override
    public void onProvisioningPublicKeySent(final UnprovisionedMeshNode node) {
        mMeshNode = node;
        mMeshNodeLiveData.postValue(node);

    }

    @Override
    public void onProvisioningPublicKeyReceived(final UnprovisionedMeshNode node) {
        mMeshNode = node;
        mMeshNodeLiveData.postValue(node);

    }

    @Override
    public void onProvisioningAuthenticationInputRequested(final UnprovisionedMeshNode node) {
        mMeshNode = node;
        mMeshNodeLiveData.postValue(node);

    }

    @Override
    public void onProvisioningInputCompleteSent(final UnprovisionedMeshNode node) {
        mMeshNode = node;
        mMeshNodeLiveData.postValue(node);
    }

    @Override
    public void onProvisioningConfirmationSent(final UnprovisionedMeshNode node) {
        mMeshNode = node;
        mMeshNodeLiveData.postValue(node);
    }

    @Override
    public void onProvisioningConfirmationReceived(final UnprovisionedMeshNode node) {
        mMeshNode = node;
        mMeshNodeLiveData.postValue(node);
    }

    @Override
    public void onProvisioningRandomSent(final UnprovisionedMeshNode node) {
        mMeshNode = node;
        mMeshNodeLiveData.postValue(node);
    }

    @Override
    public void onProvisioningRandomReceived(final UnprovisionedMeshNode node) {
        mMeshNode = node;
        mMeshNodeLiveData.postValue(node);
    }

    @Override
    public void onProvisioningDataSent(final UnprovisionedMeshNode node) {
        mMeshNode = node;
        mMeshNodeLiveData.postValue(node);
    }

    @Override
    public void onProvisioningFailed(final UnprovisionedMeshNode node, final int errorCode) {
        mMeshNode = node;
        mMeshNodeLiveData.postValue(node);
    }

    @Override
    public void onProvisioningComplete(final ProvisionedMeshNode node) {
        mIsProvisioningComplete = true;
        node.setIsProvisioned(true);
        mMeshNode = node;
        mIsReconnectingFlag = true;
        mBleMeshManager.disconnect();
        mBleMeshManager.refreshDeviceCache();
        mProvisionedNodes.postValue(mMeshManagerApi.getProvisionedNodes());
        mIsReconnecting.postValue(true);
        mHandler.postDelayed(mReconnectRunnable, 1500); //Added a slight delay to disconnect and refresh the cache
    }

    @Override
    public void onTransactionFailed(final ProvisionedMeshNode node, final int src, final boolean hasIncompleteTimerExpired) {
        mMeshNode = node;
    }

    @Override
    public void onUnknownPduReceived(final ProvisionedMeshNode node) {
        mMeshNode = node;
    }

    @Override
    public void onBlockAcknowledgementSent(final ProvisionedMeshNode node) {
        mMeshNode = node;
    }

    @Override
    public void onBlockAcknowledgementReceived(final ProvisionedMeshNode node) {
        mMeshNode = node;
    }

    @Override
    public void onGetCompositionDataSent(@NonNull final ProvisionedMeshNode node) {
        mMeshNode = node;
    }

    @Override
    public void onCompositionDataStatusReceived(@NonNull final ConfigCompositionDataStatus compositionDataStatus) {
        if(mSetupProvisionedNode){
            //We send app key add after composition is complete. Adding a delay so that we don't send anything before the acknowledgement is sent out.
            if(!mMeshManagerApi.getProvisioningSettings().getAppKeys().isEmpty()) {
                mHandler.postDelayed(() -> {
                    final String appKey = mMeshManagerApi.getProvisioningSettings().getAppKeys().get(0);
                    final ProvisionedMeshNode node = (ProvisionedMeshNode) mMeshNode;
                    mMeshManagerApi.addAppKey(node, 0, appKey);
                }, 1500);
            }
        }
    }

    @Override
    public void onAppKeyAddSent(final ProvisionedMeshNode node) {

    }

    @Override
    public void onAppKeyStatusReceived(final ConfigAppKeyStatus status) {
        if(mSetupProvisionedNode){
            mSetupProvisionedNode = false;
            mNodeSetupComplete.postValue(null);
        }
    }

    @Override
    public void onAppKeyBindSent(final ProvisionedMeshNode node) {

    }

    @Override
    public void onAppKeyUnbindSent(final ProvisionedMeshNode node) {

    }

    @Override
    public void onAppKeyBindStatusReceived(@NonNull final ConfigModelAppStatus status) {

    }

    @Override
    public void onPublicationSetSent(final ProvisionedMeshNode node) {

    }

    @Override
    public void onPublicationStatusReceived(@NonNull final ConfigModelPublicationStatus status) {

    }

    @Override
    public void onSubscriptionAddSent(final ProvisionedMeshNode node) {

    }

    @Override
    public void onSubscriptionDeleteSent(final ProvisionedMeshNode node) {

    }

    @Override
    public void onSubscriptionStatusReceived(final ConfigModelSubscriptionStatus status) {

    }

    @Override
    public void onMeshNodeResetSent(final ProvisionedMeshNode node) {

    }

    @Override
    public void onMeshNodeResetStatusReceived(@NonNull final ConfigNodeResetStatus configNodeResetStatus) {

    }

    @Override
    public void onGenericOnOffGetSent(final ProvisionedMeshNode node) {

    }

    @Override
    public void onGenericOnOffSetSent(final ProvisionedMeshNode node, final boolean presentOnOff, final boolean targetOnOff, final int remainingTime) {

    }

    @Override
    public void onGenericOnOffSetUnacknowledgedSent(final ProvisionedMeshNode node) {

    }

    @Override
    public void onGenericOnOffStatusReceived(final GenericOnOffStatus status) {

    }

    @Override
    public void onGenericLevelGetSent(final ProvisionedMeshNode node) {

    }

    @Override
    public void onGenericLevelSetSent(final ProvisionedMeshNode node, final boolean presentOnOff, final boolean targetOnOff, final int remainingTime) {

    }

    @Override
    public void onGenericLevelSetUnacknowledgedSent(final ProvisionedMeshNode node) {

    }

    @Override
    public void onGenericLevelStatusReceived(final GenericLevelStatus status) {

    }

    @Override
    public void onUnacknowledgedVendorModelMessageSent(final ProvisionedMeshNode node) {

    }

    @Override
    public void onAcknowledgedVendorModelMessageSent(final ProvisionedMeshNode node) {

    }

    @Override
    public void onVendorModelMessageStatusReceived(final VendorModelMessageStatus status) {

    }

    @Override
    public void onMeshMessageSent(final MeshMessage meshMessage) {

    }

    @Override
    public void onMeshMessageReceived(final MeshMessage meshMessage) {

    }

    @Override
    public void onMeshStatusMessageReceived(final MeshMessage meshMessage) {

    }

    /**
     * Starts reconnecting to the device
     */
    private void startScan() {
        if (mIsScanning)
            return;

        mIsScanning = true;
        mConnectionState.postValue("Scanning for provisioned node");
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
    private final Runnable mScannerTimeout = this::stopScan;

    private void onProvisionedDeviceFound(final ProvisionedMeshNode node, final ExtendedBluetoothDevice device) {
        mSetupProvisionedNode = true;
        node.setBluetoothDeviceAddress(device.getAddress());
        mMeshNode = node;
        connect(device);
    }
}
