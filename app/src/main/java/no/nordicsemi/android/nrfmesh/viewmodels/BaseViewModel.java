package no.nordicsemi.android.nrfmesh.viewmodels;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.android.material.snackbar.Snackbar;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import no.nordicsemi.android.mesh.MeshManagerApi;
import no.nordicsemi.android.mesh.models.VendorModel;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.nrfmesh.ble.BleMeshManager;
import no.nordicsemi.android.nrfmesh.ble.ScannerActivity;
import no.nordicsemi.android.nrfmesh.node.ConfigurationClientActivity;
import no.nordicsemi.android.nrfmesh.node.ConfigurationServerActivity;
import no.nordicsemi.android.nrfmesh.node.GenericLevelServerActivity;
import no.nordicsemi.android.nrfmesh.node.GenericModelConfigurationActivity;
import no.nordicsemi.android.nrfmesh.node.GenericOnOffServerActivity;
import no.nordicsemi.android.nrfmesh.node.SceneServerModelActivity;
import no.nordicsemi.android.nrfmesh.node.SceneSetupServerModelActivity;
import no.nordicsemi.android.nrfmesh.node.SensorServerActivity;
import no.nordicsemi.android.nrfmesh.node.VendorModelActivity;
import no.nordicsemi.android.nrfmesh.utils.Utils;

import static no.nordicsemi.android.mesh.models.SigModelParser.CONFIGURATION_CLIENT;
import static no.nordicsemi.android.mesh.models.SigModelParser.CONFIGURATION_SERVER;
import static no.nordicsemi.android.mesh.models.SigModelParser.GENERIC_LEVEL_SERVER;
import static no.nordicsemi.android.mesh.models.SigModelParser.GENERIC_ON_OFF_SERVER;
import static no.nordicsemi.android.mesh.models.SigModelParser.SCENE_SERVER;
import static no.nordicsemi.android.mesh.models.SigModelParser.SCENE_SETUP_SERVER;
import static no.nordicsemi.android.mesh.models.SigModelParser.SENSOR_SERVER;

/**
 * abstract base class for ViewModels
 */
public abstract class BaseViewModel extends ViewModel {

    protected Queue<MeshMessage> messageQueue = new LinkedList<>();
    final NrfMeshRepository mNrfMeshRepository;
    boolean isActivityVisible = false;


    /**
     * Constructs {@link BaseViewModel}
     *
     * @param nRfMeshRepository Mesh Repository {@link NrfMeshRepository}
     */
    BaseViewModel(@NonNull final NrfMeshRepository nRfMeshRepository) {
        mNrfMeshRepository = nRfMeshRepository;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    /**
     * Returns the Mesh repository
     */
    public final NrfMeshRepository getNrfMeshRepository() {
        return mNrfMeshRepository;
    }

    /**
     * Returns the {@link BleMeshManager}
     */
    public final BleMeshManager getBleMeshManager() {
        return mNrfMeshRepository.getBleMeshManager();
    }

    public Queue<MeshMessage> getMessageQueue() {
        return messageQueue;
    }

    public void removeMessage() {
        if (!messageQueue.isEmpty())
            messageQueue.remove();
    }

    /**
     * Navigate to scanner activity
     *
     * @param context                 Activity context
     * @param withResult              Start activity with result
     * @param requestCode             Request code when using with result
     * @param withProvisioningService Scan with provisioning service
     */
    public void navigateToScannerActivity(@NonNull final Activity context, final boolean withResult, final int requestCode, final boolean withProvisioningService) {
        final Intent intent = new Intent(context, ScannerActivity.class);
        intent.putExtra(Utils.EXTRA_DATA_PROVISIONING_SERVICE, withProvisioningService);
        if (withResult) {
            context.startActivityForResult(intent, requestCode);
        } else {
            context.startActivity(intent);
        }
    }

    /**
     * Start activity based on the type of the model
     *
     * <p> This way we can seperate the ui logic for different activities</p>
     *
     * @param context Activity context
     * @param model   {@link MeshModel}
     */
    public void navigateToModelActivity(@NonNull final Activity context, @NonNull final MeshModel model) {
        final Intent intent;
        if (model.getModelId() == CONFIGURATION_SERVER) {
            intent = new Intent(context, ConfigurationServerActivity.class);
        } else if (model.getModelId() == CONFIGURATION_CLIENT) {
            intent = new Intent(context, ConfigurationClientActivity.class);
        } else if (model.getModelId() == GENERIC_ON_OFF_SERVER) {
            intent = new Intent(context, GenericOnOffServerActivity.class);
        } else if (model.getModelId() == GENERIC_LEVEL_SERVER) {
            intent = new Intent(context, GenericLevelServerActivity.class);
        } else if (model.getModelId() == SCENE_SERVER) {
            intent = new Intent(context, SceneServerModelActivity.class);
        } else if (model.getModelId() == SCENE_SETUP_SERVER) {
            intent = new Intent(context, SceneSetupServerModelActivity.class);
        } else if (model.getModelId() == SENSOR_SERVER) {
            intent = new Intent(context, SensorServerActivity.class);
        } else if (model instanceof VendorModel) {
            intent = new Intent(context, VendorModelActivity.class);
        } else {
            intent = new Intent(context, GenericModelConfigurationActivity.class);
        }
        context.startActivity(intent);
    }

    /**
     * Connect to peripheral
     *
     * @param context          Context
     * @param device           {@link ExtendedBluetoothDevice} device
     * @param connectToNetwork True if connecting to an unprovisioned node or proxy node
     */
    public final void connect(@NonNull final Context context, @NonNull final ExtendedBluetoothDevice device, final boolean connectToNetwork) {
        mNrfMeshRepository.connect(context, device, connectToNetwork);
    }

    /**
     * Disconnect from peripheral
     */
    public final void disconnect() {
        mNrfMeshRepository.disconnect();
    }

    /**
     * Returns the address of the connected proxy address
     */
    public final LiveData<Integer> getConnectedProxyAddress() {
        return mNrfMeshRepository.getConnectedProxyAddress();
    }

    /**
     * Returns true currently connected to a peripheral device.
     */
    public final LiveData<Boolean> isConnected() {
        return mNrfMeshRepository.isConnected();
    }

    /**
     * Returns true if the device is ready
     */
    public final LiveData<Void> isDeviceReady() {
        return mNrfMeshRepository.isDeviceReady();
    }

    /**
     * Returns the connection state
     */
    public final LiveData<String> getConnectionState() {
        return mNrfMeshRepository.getConnectionState();
    }

    /**
     * Returns true if currently connected to the proxy node in the mesh network.
     */
    public final LiveData<Boolean> isConnectedToProxy() {
        return mNrfMeshRepository.isConnectedToProxy();
    }

    /**
     * Returns the mesh manager api
     */
    public final MeshManagerApi getMeshManagerApi() {
        return mNrfMeshRepository.getMeshManagerApi();
    }

    /**
     * Returns live data object containing provisioning settings.
     */
    public final MeshNetworkLiveData getNetworkLiveData() {
        return mNrfMeshRepository.getMeshNetworkLiveData();
    }

    /**
     * Returns the provisioned nodes as a live data object.
     */
    public LiveData<List<ProvisionedMeshNode>> getNodes() {
        return mNrfMeshRepository.getNodes();
    }

    /**
     * Get selected {@link ProvisionedMeshNode} mesh node
     */
    public final LiveData<ProvisionedMeshNode> getSelectedMeshNode() {
        return mNrfMeshRepository.getSelectedMeshNode();
    }

    /**
     * Set selected mesh node
     *
     * @param node {@link ProvisionedMeshNode}
     */
    public final void setSelectedMeshNode(@NonNull final ProvisionedMeshNode node) {
        mNrfMeshRepository.setSelectedMeshNode(node);
    }

    /**
     * Get selected element
     */
    public final LiveData<Element> getSelectedElement() {
        return mNrfMeshRepository.getSelectedElement();
    }

    /**
     * Set the element to be configured
     *
     * @param element {@link Element}
     */
    public final void setSelectedElement(@NonNull final Element element) {
        mNrfMeshRepository.setSelectedElement(element);
    }

    /**
     * Get selected model
     */
    public final LiveData<MeshModel> getSelectedModel() {
        return mNrfMeshRepository.getSelectedModel();
    }

    /**
     * Set the mesh model to be configured
     *
     * @param model {@link MeshModel}
     */
    public final void setSelectedModel(@NonNull final MeshModel model) {
        mNrfMeshRepository.setSelectedModel(model);
    }

    /**
     * Reset mesh network
     */
    public final void resetMeshNetwork() {
        mNrfMeshRepository.resetMeshNetwork();
    }

    /**
     * Returns the LiveData containing {@link MeshMessage}
     */
    public final LiveData<MeshMessage> getMeshMessage() {
        return mNrfMeshRepository.getMeshMessageLiveData();
    }

    /**
     * Returns an observable live data object containing the transaction status.
     *
     * @return {@link TransactionStatus}
     */
    public final LiveData<TransactionStatus> getTransactionStatus() {
        return mNrfMeshRepository.getTransactionStatus();
    }

    public boolean isModelExists(final int modelId) {
        final ProvisionedMeshNode node = getSelectedMeshNode().getValue();
        return node != null && node.isExist(modelId);
    }

    /**
     * Display disconnected snack bar
     *
     * @param context   Activity context
     * @param container container
     */
    public void displayDisconnectedSnackBar(@NonNull final Activity context, @NonNull final CoordinatorLayout container) {
        Snackbar.make(container, context.getString(R.string.disconnected_network_rationale), Snackbar.LENGTH_LONG)
                .setActionTextColor(context.getResources().getColor(R.color.colorSecondary))
                .setAction(context.getString(R.string.action_connect), v ->
                        navigateToScannerActivity(context, false, Utils.CONNECT_TO_NETWORK, false))
                .show();
    }

    /**
     * Display snack bar
     *
     * @param context   Activity context
     * @param container Coordinator layout
     * @param message   Message
     * @param duration  Snack bar duration
     */
    public void displaySnackBar(@NonNull final Context context, @NonNull final CoordinatorLayout container, @NonNull final String message, final int duration) {
        Snackbar.make(container, message, duration)
                .setActionTextColor(context.getResources().getColor(R.color.colorSecondary))
                .show();
    }

    public boolean isActivityVisible() {
        return isActivityVisible;
    }

    public void setActivityVisible(final boolean visible) {
        isActivityVisible = visible;
    }
}
