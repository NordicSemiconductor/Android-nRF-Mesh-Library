package no.nordicsemi.android.nrfmeshprovisioner.viewmodels;

import androidx.annotation.NonNull;

import javax.inject.Inject;

import no.nordicsemi.android.nrfmeshprovisioner.ManageAppKeysActivity;

/**
 * ViewModel for {@link ManageAppKeysActivity}
 */
public class ManageAppKeysViewModel extends BaseViewModel {

    @Inject
    ManageAppKeysViewModel(@NonNull final NrfMeshRepository nrfMeshRepository) {
        super(nrfMeshRepository);
    }
}
