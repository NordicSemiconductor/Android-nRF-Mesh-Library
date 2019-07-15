package no.nordicsemi.android.nrfmeshprovisioner.viewmodels;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import no.nordicsemi.android.meshprovisioner.Provisioner;

public class RangesViewModel extends BaseViewModel {

    /**
     * Constructs {@link BaseViewModel}
     *
     * @param nRfMeshRepository Mesh Repository {@link NrfMeshRepository}
     */
    @Inject
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
