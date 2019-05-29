package no.nordicsemi.android.nrfmeshprovisioner.viewmodels;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import no.nordicsemi.android.nrfmeshprovisioner.keys.AppKeysActivity;

/**
 * ViewModel for {@link AppKeysActivity}
 */
public class AppKeysViewModel extends KeysViewModel {

    @Inject
    AppKeysViewModel(@NonNull final NrfMeshRepository nrfMeshRepository) {
        super(nrfMeshRepository);
    }
}
