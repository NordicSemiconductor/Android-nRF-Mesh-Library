package no.nordicsemi.android.nrfmesh.viewmodels;

import androidx.annotation.NonNull;
import androidx.hilt.lifecycle.ViewModelInject;
import no.nordicsemi.android.mesh.Provisioner;
import no.nordicsemi.android.nrfmesh.keys.AppKeysActivity;

/**
 * ViewModel for {@link AppKeysActivity}
 */
public class ProvisionersViewModel extends BaseViewModel {

    @ViewModelInject
    ProvisionersViewModel(@NonNull final NrfMeshRepository nrfMeshRepository) {
        super(nrfMeshRepository);
    }

    public void setSelectedProvisioner(@NonNull final Provisioner provisioner) {
        mNrfMeshRepository.setSelectedProvisioner(provisioner);
    }
}
