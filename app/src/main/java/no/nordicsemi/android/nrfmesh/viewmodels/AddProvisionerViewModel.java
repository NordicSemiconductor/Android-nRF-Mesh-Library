package no.nordicsemi.android.nrfmesh.viewmodels;

import androidx.annotation.NonNull;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import no.nordicsemi.android.mesh.Provisioner;
import no.nordicsemi.android.nrfmesh.keys.AppKeysActivity;

/**
 * ViewModel for {@link AppKeysActivity}
 */
public class AddProvisionerViewModel extends BaseViewModel {

    @ViewModelInject
    AddProvisionerViewModel(@NonNull final NrfMeshRepository nrfMeshRepository) {
        super(nrfMeshRepository);
        mNrfMeshRepository.clearTransactionStatus();
    }

    public void setSelectedProvisioner(@NonNull final Provisioner provisioner) {
        mNrfMeshRepository.setSelectedProvisioner(provisioner);
    }

    public LiveData<Provisioner> getSelectedProvisioner() {
        return mNrfMeshRepository.getSelectedProvisioner();
    }
}
