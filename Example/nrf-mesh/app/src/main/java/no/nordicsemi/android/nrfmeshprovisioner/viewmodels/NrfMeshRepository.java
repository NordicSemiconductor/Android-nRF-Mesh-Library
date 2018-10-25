package no.nordicsemi.android.nrfmeshprovisioner.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.meshprovisioner.MeshManagerTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshProvisioningStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.provisionerstates.UnprovisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.transport.ConfigAppKeyAdd;
import no.nordicsemi.android.meshprovisioner.transport.ConfigAppKeyStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigCompositionDataGet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigCompositionDataStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelAppStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelPublicationStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelSubscriptionStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNodeResetStatus;
import no.nordicsemi.android.meshprovisioner.transport.GenericLevelStatus;
import no.nordicsemi.android.meshprovisioner.transport.GenericOnOffStatus;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.transport.VendorModelMessageStatus;
import no.nordicsemi.android.meshprovisioner.models.SigModelParser;
import no.nordicsemi.android.meshprovisioner.provisionerstates.ProvisioningCapabilities;
import no.nordicsemi.android.meshprovisioner.provisionerstates.ProvisioningState;
import no.nordicsemi.android.meshprovisioner.utils.Element;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManager;
import no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManagerCallbacks;
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
    private final MutableLiveData<Boolean> mIsReconnecting = new MutableLiveData<>();

    private final MutableLiveData<ProvisioningCapabilities> capabilitiesMutableLiveData = new MutableLiveData<>();

    /**
     * Flag to determine if a reconnection is in the progress when provisioning has completed
     **/
    private final MutableLiveData<byte[]> mConfigurationSrc = new MutableLiveData<>();

    private final MutableLiveData<UnprovisionedMeshNode> mUnprovisionedMeshNodeLiveData = new MutableLiveData<>();
    private final MutableLiveData<ProvisionedMeshNode> mProvisionedMeshNodeLiveData = new MutableLiveData<>();

    private final NetworkInformation mNetworkInformation;

    /**
     * Contains the initial provisioning live data
     **/
    private NetworkInformationLiveData mNetworkInformationLiveData;

    /**
     * Flag to determine if provisioning was completed
     **/
    private boolean mIsProvisioningComplete = false;

    /**
     * Contains the {@link ExtendedMeshNode}
     **/
    private ExtendedMeshNode mExtendedMeshNode;

    /**
     * Contains the {@link ExtendedElement}
     **/
    private ExtendedElement mExtendedElement;

    /**
     * Contains the {@link ExtendedMeshModel}
     **/
    private ExtendedMeshModel mExtendedMeshModel;

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
    private final ProvisioningSettingsLiveData mProvisioningSettingsLiveData;

    private MeshMessageLiveData mMeshMessageLiveData = new MeshMessageLiveData();
    /**
     * Contains the provisioned nodes
     **/
    private final MutableLiveData<Map<Integer, ProvisionedMeshNode>> mProvisionedNodes = new MutableLiveData<>();

    private final TransactionStatusLiveData mTransactionFailedLiveData = new TransactionStatusLiveData();

    //private static NrfMeshRepository mNrfMeshRepository;
    private MeshManagerApi mMeshManagerApi;
    private BleMeshManager mBleMeshManager;
    private Handler mHandler;
    private UnprovisionedMeshNode mUnprovisionedMeshNode;
    private ProvisionedMeshNode mProvisionedMeshNode;
    private boolean mIsReconnectingFlag;
    private boolean mIsScanning;
    private boolean mSetupProvisionedNode;
    private ProvisioningStatusLiveData mProvisioningStateLiveData;

    private final Runnable mReconnectRunnable = this::startScan;

    private final Runnable mScannerTimeout = this::stopScan;

    public NrfMeshRepository(final MeshManagerApi meshManagerApi, final NetworkInformation networkInformation, final BleMeshManager bleMeshManager) {
        //Initialize the mesh api
        mMeshManagerApi = meshManagerApi;
        mMeshManagerApi.setProvisionerManagerTransportCallbacks(this);
        mMeshManagerApi.setProvisioningStatusCallbacks(this);
        mMeshManagerApi.setMeshStatusCallbacks(this);
        //Load live data with provisioned nodes
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

    LiveData<Map<Integer, ProvisionedMeshNode>> getProvisionedNodes() {
        return mProvisionedNodes;
    }

    final ProvisioningSettingsLiveData getProvisioningSettingsLiveData() {
        return mProvisioningSettingsLiveData;
    }

    NetworkInformationLiveData getNetworkInformationLiveData() {
        return mNetworkInformationLiveData;
    }

    LiveData<byte[]> getConfigurationSrcLiveData() {
        return mConfigurationSrc;
    }

    public LiveData<ProvisioningCapabilities> getCapabilitiesMutableLiveData() {
        return capabilitiesMutableLiveData;
    }

    boolean setConfiguratorSrc(final byte[] configuratorSrc) {
        if (mMeshManagerApi.setConfiguratorSrc(configuratorSrc)) {
            mConfigurationSrc.postValue(mMeshManagerApi.getConfiguratorSrc());
            return true;
        }
        return false;
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
    MeshMessageLiveData getMeshMessageLiveData() {
        return mMeshMessageLiveData;
    }

    /**
     * Reset mesh network
     */
    void resetMeshNetwork() {
        disconnect();
        mMeshManagerApi.resetMeshNetwork();
        mProvisionedNodes.postValue(mMeshManagerApi.getProvisionedNodes());
        mNetworkInformation.refreshProvisioningData();
        mProvisioningSettingsLiveData.refresh(mMeshManagerApi.getProvisioningSettings());
    }

    /**
     * Connect to peripheral
     *
     * @param device bluetooth device
     */
    public void connect(final Context context, final ExtendedBluetoothDevice device, final boolean connectToNetwork) {
        mNetworkInformationLiveData.getValue().setNodeName(device.getName());
        mIsProvisioningComplete = false;
        clearExtendedMeshNode();
        final LogSession logSession = Logger.newSession(context, null, device.getAddress(), device.getName());
        mBleMeshManager.setLogger(logSession);
        final BluetoothDevice bluetoothDevice = device.getDevice();
        initIsConnectedLiveData(connectToNetwork);
        mBleMeshManager.connect(bluetoothDevice);
    }

    /**
     * Connect to peripheral
     *
     * @param device bluetooth device
     */
    private void connectToProxy(final ExtendedBluetoothDevice device) {
        initIsConnectedLiveData(true);
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
    public void disconnect() {
        clearMeshNodeLiveData();
        removeCallbacks();
        mIsProvisioningComplete = false;
        mBleMeshManager.disconnect();
    }

    void removeCallbacks() {
        mHandler.removeCallbacksAndMessages(null);
    }

    void clearMeshNodeLiveData() {
        mUnprovisionedMeshNodeLiveData.setValue(null);
        mProvisionedMeshNodeLiveData.setValue(null);
    }

    private void clearExtendedMeshNode() {
        if (mExtendedMeshNode != null) {
            mExtendedMeshNode.clearNode();
        }
    }

    LiveData<UnprovisionedMeshNode> getUnprovisionedMeshNode() {
        return mUnprovisionedMeshNodeLiveData;
    }

    /**
     * Returns the selected mesh node
     */
    ExtendedMeshNode getSelectedMeshNode() {
        return mExtendedMeshNode;
    }

    /**
     * Sets the mesh node to be configured
     *
     * @param node provisioned mesh node
     */
    void setSelectedMeshNode(final ProvisionedMeshNode node) {
        if (mExtendedMeshNode == null) {
            mExtendedMeshNode = new ExtendedMeshNode(node);
        } else {
            mExtendedMeshNode.updateMeshNode(node);
        }
    }

    /**
     * Returns the selected element
     */
    ExtendedElement getSelectedElement() {
        return mExtendedElement;
    }

    /**
     * Set the selected {@link Element} to be configured
     *
     * @param element element
     */
    void setSelectedElement(final Element element) {
        if (mExtendedElement == null) {
            mExtendedElement = new ExtendedElement(element);
        } else {
            mExtendedElement.setElement(element);
        }
    }

    /**
     * Returns the selected mesh model
     */
    ExtendedMeshModel getSelectedModel() {
        return mExtendedMeshModel;
    }

    /**
     * Set the selected model to be configured
     *
     * @param model mesh model
     */
    void setSelectedModel(final MeshModel model) {
        if (mExtendedMeshModel == null) {
            mExtendedMeshModel = new ExtendedMeshModel(model);
        } else {
            mExtendedMeshModel.setMeshModel(model);
        }
    }

    void sendGetCompositionData() {
        final ProvisionedMeshNode node = mExtendedMeshNode.getMeshNode();
        final ConfigCompositionDataGet configCompositionDataGet = new ConfigCompositionDataGet(node, 0);
        mMeshManagerApi.sendMeshConfigurationMessage(configCompositionDataGet);
    }

    void sendAppKeyAdd(final ConfigAppKeyAdd configAppKeyAdd) {
        mMeshManagerApi.sendMeshConfigurationMessage(configAppKeyAdd);
    }

    @Override
    public void onDataReceived(final BluetoothDevice bluetoothDevice, final int mtu, final byte[] pdu) {
        try {
            if (mExtendedMeshNode != null && mExtendedMeshNode.getMeshNode() != null) {
                //node = mProvisionedMeshNode = mExtendedMeshNode.getMeshNode();
            } else {
                //node = mProvisionedMeshNode = mUnprovisionedMeshNodeLiveData.getValue();
            }
            mMeshManagerApi.handleNotifications(mtu, pdu);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
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
        mConnectionState.postValue("Disconnecting...");
        if (mIsReconnectingFlag) {
            mIsConnected.postValue(false);
        }
        /*mSetupProvisionedNode = false;
        mIsConnectedToProxy.postValue(false);*/
    }

    @Override
    public void onDeviceDisconnected(final BluetoothDevice device) {
        Log.v(TAG, "Disconnected");
        mConnectionState.postValue("Disconnected!");
        if (mIsReconnectingFlag) {
            mIsReconnectingFlag = false;
            mIsReconnecting.postValue(false);
        } else {
            mIsConnected.postValue(false);
            mIsConnectedToProxy.postValue(false);
            clearExtendedMeshNode();
        }
        mSetupProvisionedNode = false;
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
                //We update the bluetooth device after a startScan because some devices may start advertising with different mac address
                mProvisionedMeshNode.setBluetoothDeviceAddress(device.getAddress());
                //Adding a slight delay here so we don't send anything before we receive the mesh beacon message
                final ConfigCompositionDataGet compositionDataGet = new ConfigCompositionDataGet(mProvisionedMeshNode, 0);
                mHandler.postDelayed(() -> mMeshManagerApi.sendMeshConfigurationMessage(compositionDataGet), 2000);
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
    }

    @Override
    public void onDeviceNotSupported(final BluetoothDevice device) {

    }

    @Override
    public void sendProvisioningPdu(final UnprovisionedMeshNode meshNode, final byte[] pdu) {
        mBleMeshManager.sendPdu(pdu);
    }

    @Override
    public void sendMeshPdu(final ProvisionedMeshNode meshNode, final byte[] pdu) {
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
        mIsReconnectingFlag = true;
        mIsReconnecting.postValue(true);
        mBleMeshManager.disconnect();
        mBleMeshManager.refreshDeviceCache();
        mProvisionedNodes.postValue(mMeshManagerApi.getProvisionedNodes());
        mHandler.postDelayed(mReconnectRunnable, 1500); //Added a slight delay to disconnect and refresh the cache
    }

    @Override
    public void onTransactionFailed(final ProvisionedMeshNode node, final int src, final boolean hasIncompleteTimerExpired) {
        mProvisionedMeshNode = node;
        if (mTransactionFailedLiveData.hasActiveObservers()) {
            mTransactionFailedLiveData.onTransactionFailed(src, hasIncompleteTimerExpired);
        }
    }

    @Override
    public void onUnknownPduReceived(final ProvisionedMeshNode node, final int src, final byte[] accessPayload) {
        updateNode(node);
    }

    @Override
    public void onBlockAcknowledgementSent(final ProvisionedMeshNode node) {
        mProvisionedMeshNode = node;
        if (mSetupProvisionedNode) {
            mProvisionedMeshNodeLiveData.postValue(node);
            mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisioningState.States.SENDING_BLOCK_ACKNOWLEDGEMENT);
        }
    }

    @Override
    public void onBlockAcknowledgementReceived(final ProvisionedMeshNode node) {
        mProvisionedMeshNode = node;
        if (mSetupProvisionedNode) {
            mProvisionedMeshNodeLiveData.postValue(node);
            mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisioningState.States.BLOCK_ACKNOWLEDGEMENT_RECEIVED);
        }
    }

    @Override
    public void onMeshMessageSent(final MeshMessage meshMessage) {
        final ProvisionedMeshNode node = meshMessage.getMeshNode();
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

    @Override
    public void onMeshMessageReceived(final MeshMessage meshMessage) {
        final ProvisionedMeshNode node = meshMessage.getMeshNode();
        if (meshMessage instanceof ConfigCompositionDataStatus) {
            final ConfigCompositionDataStatus status = (ConfigCompositionDataStatus) meshMessage;
            if (mSetupProvisionedNode) {
                mProvisionedMeshNodeLiveData.postValue(node);
                mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisioningState.States.COMPOSITION_DATA_STATUS_RECEIVED);
                //We send app key add after composition is complete. Adding a delay so that we don't send anything before the acknowledgement is sent out.
                if (!mMeshManagerApi.getProvisioningSettings().getAppKeys().isEmpty()) {
                    mHandler.postDelayed(() -> {
                        final String appKey = mProvisioningSettingsLiveData.getSelectedAppKey();
                        final int index = mMeshManagerApi.getProvisioningSettings().getAppKeys().indexOf(appKey);
                        final ConfigAppKeyAdd configAppKeyAdd = new ConfigAppKeyAdd(node, MeshParserUtils.toByteArray(appKey), index, 0);
                            mMeshManagerApi.sendMeshConfigurationMessage(configAppKeyAdd);
                    }, 2500);
                }
            } else {
                updateNode(node);
            }
        } else if (meshMessage instanceof ConfigAppKeyStatus) {
            final ConfigAppKeyStatus status = (ConfigAppKeyStatus) meshMessage;
            if (mSetupProvisionedNode) {
                mSetupProvisionedNode = false;
                mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisioningState.States.APP_KEY_STATUS_RECEIVED);
            } else {
                updateNode(node);
                mMeshMessageLiveData.postValue(status);
            }
        } else if (meshMessage instanceof ConfigModelAppStatus) {

            if(updateNode(node)) {
                final ConfigModelAppStatus status = (ConfigModelAppStatus) meshMessage;
                final Element element = node.getElements().get(status.getElementAddress());
                if(node.getElements().containsKey(status.getElementAddress())) {
                    mExtendedElement.setElement(element);
                    final MeshModel model = element.getMeshModels().get(status.getModelIdentifier());
                    mExtendedMeshModel.setMeshModel(model);
                }
            }

        } else if (meshMessage instanceof ConfigModelPublicationStatus) {

            if(updateNode(node)) {
                final ConfigModelPublicationStatus status = (ConfigModelPublicationStatus) meshMessage;
                if(node.getElements().containsKey(status.getElementAddress())) {
                    final Element element = node.getElements().get(status.getElementAddress());
                    mExtendedElement.setElement(element);
                    final MeshModel model = element.getMeshModels().get(status.getModelIdentifier());
                    mExtendedMeshModel.setMeshModel(model);
                }
            }

        } else if (meshMessage instanceof ConfigModelSubscriptionStatus) {

            if(updateNode(node)) {
                final ConfigModelSubscriptionStatus status = (ConfigModelSubscriptionStatus) meshMessage;
                if(node.getElements().containsKey(status.getElementAddress())) {
                    final Element element = node.getElements().get(status.getElementAddress());
                    mExtendedElement.setElement(element);
                    final MeshModel model = element.getMeshModels().get(status.getModelIdentifier());
                    mExtendedMeshModel.setMeshModel(model);
                }
            }

        } else if (meshMessage instanceof ConfigNodeResetStatus) {

            final ConfigNodeResetStatus status = (ConfigNodeResetStatus) meshMessage;
            mExtendedMeshNode.clearNode();
            mProvisionedNodes.postValue(mMeshManagerApi.getProvisionedNodes());
            mMeshMessageLiveData.postValue(status);

        } else if (meshMessage instanceof GenericOnOffStatus) {
            if(updateNode(node)) {
                final GenericOnOffStatus status = (GenericOnOffStatus) meshMessage;
                if(node.getElements().containsKey(status.getSrcAddress())) {
                    final Element element = node.getElements().get(status.getSrcAddress());
                    mExtendedElement.setElement(element);
                    final MeshModel model = element.getMeshModels().get((int) SigModelParser.GENERIC_ON_OFF_SERVER);
                    mExtendedMeshModel.setMeshModel(model);
                }
            }
        } else if (meshMessage instanceof GenericLevelStatus) {

            if(updateNode(node)) {
                final GenericLevelStatus status = (GenericLevelStatus) meshMessage;
                if(node.getElements().containsKey(status.getSrcAddress())) {
                    final Element element = node.getElements().get(status.getSrcAddress());
                    mExtendedElement.setElement(element);
                    final MeshModel model = element.getMeshModels().get((int) SigModelParser.GENERIC_LEVEL_SERVER);
                    mExtendedMeshModel.setMeshModel(model);
                }
            }

        } else if (meshMessage instanceof VendorModelMessageStatus) {

            if(updateNode(node)) {
                final VendorModelMessageStatus status = (VendorModelMessageStatus) meshMessage;
                if(node.getElements().containsKey(status.getSrcAddress())) {
                    final Element element = node.getElements().get(status.getSrcAddress());
                    mExtendedElement.setElement(element);
                    final MeshModel model = element.getMeshModels().get(status.getModelIdentifier());
                    mExtendedMeshModel.setMeshModel(model);
                }
            }
        }

        if (mMeshMessageLiveData.hasActiveObservers()) {
            mMeshMessageLiveData.postValue(meshMessage);
        }
    }

    /**
     * We should only update the selected node, since sending messages to group address will notify with nodes that is not on the UI
     */
    private boolean updateNode(final ProvisionedMeshNode node){
        if(mProvisionedMeshNode.getUnicastAddressInt() == node.getUnicastAddressInt()) {
            mProvisionedMeshNode = node;
            mExtendedMeshNode.updateMeshNode(node);
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
                final byte[] serviceData = scanRecord.getServiceData(new ParcelUuid((MESH_PROXY_UUID)));
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
        node.setBluetoothDeviceAddress(device.getAddress());
        mProvisionedMeshNode = node;
        connectToProxy(device);
    }
}
