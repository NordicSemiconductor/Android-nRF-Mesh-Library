package no.nordicsemi.android.nrfmesh.scenes;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.Scene;

public interface SceneCallbacks {

    /**
     * Creates a scene
     *
     * @return {@link Scene}
     */
    Scene createScene();

    /**
     * Creates a scene
     *
     * @param name Scene name
     */
    Scene createScene(@NonNull final String name);

    /**
     * Creates a Scene
     *
     * @param name   Scene name
     * @param number Scene number
     */
    Scene createScene(@NonNull final String name, final int number);

    /**
     * Adds a scene
     *
     * @param name    Name
     * @param number Address
     * @return true if successful or false otherwise
     */
    boolean onSceneAdded(@NonNull final String name, final int number);

    /**
     * Adds a scene
     *
     * @param scene {@link Scene}
     * @return true if successful or false otherwise
     */
    boolean onSceneAdded(@NonNull final Scene scene);

    /**
     * Invoked when a scene is updated
     *
     * @param scene Scene
     * @return true if successful or false otherwise
     */
    boolean onSceneUpdated(@NonNull final Scene scene);
}
