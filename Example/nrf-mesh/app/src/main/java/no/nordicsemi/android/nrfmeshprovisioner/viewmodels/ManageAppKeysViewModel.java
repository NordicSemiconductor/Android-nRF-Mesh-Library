package no.nordicsemi.android.nrfmeshprovisioner.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;

import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.ManageAppKeysActivity;

/**
 * ViewModel for {@link ManageAppKeysActivity}
 */
public class ManageAppKeysViewModel extends ViewModel {

    private final NrfMeshRepository mNrfMeshRepository;

    @Inject
    ManageAppKeysViewModel(final NrfMeshRepository nrfMeshRepository) {
        mNrfMeshRepository = nrfMeshRepository;
    }

    /**
     * Returns live data object containing provisioning settings.
     */
    public MeshNetworkLiveData getMeshNetworkLiveData() {
        return mNrfMeshRepository.getMeshNetworkLiveData();
    }

    public LiveData<ProvisionedMeshNode> getSelectedMeshNode() {
        return mNrfMeshRepository.getSelectedMeshNode();
    }
}
