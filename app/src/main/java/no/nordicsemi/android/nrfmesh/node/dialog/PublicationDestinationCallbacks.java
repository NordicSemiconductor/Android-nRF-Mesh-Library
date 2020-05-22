package no.nordicsemi.android.nrfmesh.node.dialog;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.Group;

/**
 * Publication destination callbacks.
 */
public interface PublicationDestinationCallbacks {

    /**
     * Invoked when publish address set.
     *
     * @param address publish address
     */
    void onPublishAddressSet(int address);

    /**
     * Invoked when publish address set.
     *
     * @param group Group
     */
    void onPublishAddressSet(@NonNull Group group);
}
