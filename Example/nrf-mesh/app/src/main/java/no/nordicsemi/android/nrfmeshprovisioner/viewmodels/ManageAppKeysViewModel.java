package no.nordicsemi.android.nrfmeshprovisioner.viewmodels;

import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;

import no.nordicsemi.android.nrfmeshprovisioner.livedata.ProvisioningLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.repository.MeshRepository;

public class ManageAppKeysViewModel extends ViewModel {

    private final MeshRepository mMeshRepository;

    @Inject
    ManageAppKeysViewModel(final MeshRepository meshRepository) {
        super();
        this.mMeshRepository = meshRepository;
        mMeshRepository.registerBroadcastReceiver();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public MeshRepository getMeshRepository() {
        return mMeshRepository;
    }

    public ProvisioningLiveData getProvisioningLiveData() {
        return mMeshRepository.getProvisioningData();
    }
}
