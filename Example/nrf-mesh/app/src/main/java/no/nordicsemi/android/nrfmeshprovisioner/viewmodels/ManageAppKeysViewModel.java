package no.nordicsemi.android.nrfmeshprovisioner.viewmodels;

import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;

public class ManageAppKeysViewModel extends ViewModel {

    private final NrfMeshRepository mNrfMeshRepository;

    @Inject
    ManageAppKeysViewModel(final NrfMeshRepository nrfMeshRepository) {
        mNrfMeshRepository = nrfMeshRepository;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }


    /**
     * Returns the {@link NrfMeshRepository}
     */
    public NrfMeshRepository getNrfMeshRepository(){
        return mNrfMeshRepository;
    }

    public ProvisioningSettingsLiveData getProvisioningLiveData() {
        return mNrfMeshRepository.getProvisioningSettingsLiveData();
    }
}
