package no.nordicsemi.android.nrfmeshprovisioner.viewmodels;

import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;

import no.nordicsemi.android.nrfmeshprovisioner.repository.MeshRepository;

public class ManageAppKeysViewModel extends ViewModel {

    private final MeshRepository mMeshRepository;
    private final NrfMeshRepository mNrfMeshRepository;

    @Inject
    ManageAppKeysViewModel(final MeshRepository meshRepository, final NrfMeshRepository nrfMeshRepository) {
        super();
        this.mMeshRepository = meshRepository;
        mMeshRepository.registerBroadcastReceiver();
        mNrfMeshRepository = nrfMeshRepository;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public MeshRepository getMeshRepository() {
        return mMeshRepository;
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
