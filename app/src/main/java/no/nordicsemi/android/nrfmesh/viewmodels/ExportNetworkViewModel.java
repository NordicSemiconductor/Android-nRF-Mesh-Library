package no.nordicsemi.android.nrfmesh.viewmodels;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import no.nordicsemi.android.mesh.ApplicationKeysConfig;
import no.nordicsemi.android.mesh.GroupsConfig;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.mesh.NetworkKeysConfig;
import no.nordicsemi.android.mesh.NodesConfig;
import no.nordicsemi.android.mesh.Provisioner;
import no.nordicsemi.android.mesh.ProvisionersConfig;
import no.nordicsemi.android.mesh.ScenesConfig;
import no.nordicsemi.android.nrfmesh.export.ExportNetworkActivity;
import no.nordicsemi.android.nrfmesh.utils.NetworkExportUtils;

/**
 * ViewModel for {@link ExportNetworkActivity}
 */
@HiltViewModel
public class ExportNetworkViewModel extends BaseViewModel implements NetworkExportUtils.NetworkExportCallbacks {

    private final SingleLiveEvent<String> networkExportState = new SingleLiveEvent<>();
    private final MutableLiveData<Void> exportStatus = new MutableLiveData<>();
    private boolean exportEverything = true;
    private boolean exportDeviceKeys = true;
    private final List<Provisioner> provisioners = new ArrayList<>();
    private final List<NetworkKey> networkKeys = new ArrayList<>();

    @Inject
    ExportNetworkViewModel(@NonNull final NrfMeshRepository nrfMeshRepository) {
        super(nrfMeshRepository);
        exportStatus.postValue(null);
    }

    public LiveData<Void> getExportStatus() {
        return exportStatus;
    }

    public LiveData<String> getNetworkExportState() {
        return networkExportState;
    }

    /**
     * Select a provisioner to be added to be exported.
     *
     * @param provisioner Provisioner
     */
    public void addProvisioner(@NonNull final Provisioner provisioner) {
        provisioners.add(provisioner);
        exportStatus.postValue(null);
    }

    /**
     * Remove provisioner from the list of provisioners.
     *
     * @param provisioner Provisioner
     */
    public void removeProvisioner(@NonNull final Provisioner provisioner) {
        provisioners.remove(provisioner);
        exportStatus.postValue(null);
    }

    public List<Provisioner> getProvisioners() {
        return provisioners;
    }

    /**
     * Add Network Key from the list of keys.
     *
     * @param networkKey Network Key
     */
    public void addNetworkKey(@NonNull final NetworkKey networkKey) {
        networkKeys.add(networkKey);
        exportStatus.postValue(null);
    }

    /**
     * Remove Network Key from the list of keys.
     *
     * @param networkKey Network Key
     */
    public void removeNetworkKey(@NonNull final NetworkKey networkKey) {
        networkKeys.remove(networkKey);
        exportStatus.postValue(null);
    }

    /**
     * Returns the list of network keys to export
     */
    public List<NetworkKey> getNetworkKeys() {
        return networkKeys;
    }

    /**
     * Returns true if the everything is to be exported.
     */
    public boolean isExportEverything() {
        return exportEverything;
    }

    /**
     * Sets the flag to true if everything must be exported.
     *
     * @param exportEverything flag
     */
    public void setExportEverything(final boolean exportEverything) {
        this.exportEverything = exportEverything;
        exportStatus.postValue(null);
    }

    public boolean isExportDeviceKeys() {
        return exportDeviceKeys;
    }

    /**
     * Sets the flag to true if all device keys must be exported.
     *
     * @param exportDeviceKeys flag
     */
    public void setExportDeviceKeys(final boolean exportDeviceKeys) {
        this.exportDeviceKeys = exportDeviceKeys;
        exportStatus.postValue(null);
    }

    /**
     * Export mesh network
     */
    private String export() throws IllegalArgumentException {
        if (exportEverything) {
            return mNrfMeshRepository.getMeshManagerApi().exportMeshNetwork();
        } else {
            final ApplicationKeysConfig applicationKeysConfig = new ApplicationKeysConfig.ExportAll().build();
            final NodesConfig nodesConfig = exportDeviceKeys ? new NodesConfig.ExportWithDeviceKey().build() : new NodesConfig.ExportWithoutDeviceKey().build();
            final NetworkKeysConfig networkKeysConfig =
                    mNrfMeshRepository.getMeshNetworkLiveData().getMeshNetwork().getNetKeys().size() == networkKeys.size() ?
                            new NetworkKeysConfig.ExportAll().build() : new NetworkKeysConfig.ExportSome(networkKeys).build();

            final ProvisionersConfig provisionersConfig = mNrfMeshRepository
                    .getMeshNetworkLiveData().getMeshNetwork().getProvisioners().size() == provisioners.size() ?
                    new ProvisionersConfig.ExportAll().build() : new ProvisionersConfig.ExportSome(provisioners).build();
            return mNrfMeshRepository
                    .getMeshManagerApi()
                    .exportMeshNetwork(networkKeysConfig, applicationKeysConfig, nodesConfig, provisionersConfig,
                            new GroupsConfig.ExportAll().build(), new ScenesConfig.ExportAll().build());
        }
    }

    public boolean exportNetwork(@NonNull final OutputStream outputStream) throws IOException {
        final String network = export();
        outputStream.write(network.getBytes());
        outputStream.close();
        return true;
    }

    public boolean exportNetwork() throws IOException {
        final String network = export();
        final String fileName = getNetworkLiveData().getNetworkName() + ".json";
        final String path = NrfMeshRepository.EXPORT_PATH;
        final File directory = new File(path);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                return false;
            }
        }
        final File file = new File(path, fileName);
        final BufferedWriter br = new BufferedWriter(new FileWriter(file.getAbsolutePath()));
        br.write(network);
        br.flush();
        br.close();
        return true;
    }


    @Override
    public void onNetworkExported() {
        networkExportState.postValue(getNetworkLiveData().getMeshNetwork().getMeshName() + " has been successfully exported.");
    }

    @Override
    public void onNetworkExportFailed(@NonNull final String error) {
        networkExportState.postValue(error);
    }
}
