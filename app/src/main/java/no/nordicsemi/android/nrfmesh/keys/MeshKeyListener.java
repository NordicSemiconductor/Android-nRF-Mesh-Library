package no.nordicsemi.android.nrfmesh.keys;

import androidx.annotation.NonNull;

public interface MeshKeyListener {

    /**
     * Invoked when the name of the key has been changed
     *
     * @param name Name
     * @return true if the name was set or false otherwise
     */
    boolean onKeyNameUpdated(@NonNull final String name);

    /**
     * Invoked when the name of the key has been changed
     *
     * @param key      Updated key
     * @return true if the key was updated or false otherwise
     */
    boolean onKeyUpdated(@NonNull final String key);
}
