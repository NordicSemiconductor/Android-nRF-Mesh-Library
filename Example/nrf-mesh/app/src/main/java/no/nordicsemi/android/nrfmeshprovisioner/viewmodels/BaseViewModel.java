package no.nordicsemi.android.nrfmeshprovisioner.viewmodels;

import android.content.Context;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import no.nordicsemi.android.meshprovisioner.Group;
import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManager;

/**
 * abstract base class for ViewModels
 */
abstract class BaseViewModel extends ViewModel {

    final NrfMeshRepository mNrfMeshRepository;

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
    public final MeshNetworkLiveData getMeshNetworkLiveData() {
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
     * Returns the LiveData containing a list of {@link Group}
     */
    public final LiveData<List<Group>> getGroups() {
        return mNrfMeshRepository.getGroups();
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
}
