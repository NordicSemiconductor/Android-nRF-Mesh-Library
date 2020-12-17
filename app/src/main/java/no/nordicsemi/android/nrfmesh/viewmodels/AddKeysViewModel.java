package no.nordicsemi.android.nrfmesh.viewmodels;

import androidx.annotation.NonNull;
import androidx.hilt.lifecycle.ViewModelInject;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.keys.AppKeysActivity;
import no.nordicsemi.android.nrfmesh.keys.NetKeysActivity;

/**
 * ViewModel for {@link NetKeysActivity}, {@link AppKeysActivity}
 */
public class AddKeysViewModel extends BaseViewModel {

    @ViewModelInject
    AddKeysViewModel(@NonNull final NrfMeshRepository nrfMeshRepository) {
        super(nrfMeshRepository);
    }

    /**
     * Checks if the key has been added to the node
     *
     * @param keyIndex index of the key
     * @return true if added or false otherwise
     */
    public boolean isNetKeyAdded(final int keyIndex) {
        final ProvisionedMeshNode node = getSelectedMeshNode().getValue();
        if (node != null) {
            return MeshParserUtils.isNodeKeyExists(node.getAddedNetKeys(), keyIndex);
        }
        return false;
    }

    /**
     * Checks if the key has been added to the node
     *
     * @param keyIndex index of the key
     * @return true if added or false otherwise
     */
    public boolean isAppKeyAdded(final int keyIndex) {
        final ProvisionedMeshNode node = getSelectedMeshNode().getValue();
        if (node != null) {
            return MeshParserUtils.isNodeKeyExists(node.getAddedAppKeys(), keyIndex);
        }
        return false;
    }

}
