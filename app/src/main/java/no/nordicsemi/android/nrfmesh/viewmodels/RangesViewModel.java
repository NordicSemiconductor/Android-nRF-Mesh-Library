package no.nordicsemi.android.nrfmesh.viewmodels;

import androidx.annotation.NonNull;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import no.nordicsemi.android.mesh.Provisioner;

public class RangesViewModel extends BaseViewModel {

    /**
     * Constructs {@link BaseViewModel}
     *
     * @param nRfMeshRepository Mesh Repository {@link NrfMeshRepository}
     */
    @ViewModelInject
    RangesViewModel(@NonNull final NrfMeshRepository nRfMeshRepository) {
        super(nRfMeshRepository);
    }

    public LiveData<Provisioner> getSelectedProvisioner() {
        return mNrfMeshRepository.getSelectedProvisioner();
    }

    public void setSelectedProvisioner(@NonNull final Provisioner provisioner) {
        mNrfMeshRepository.setSelectedProvisioner(provisioner);
    }
}
