package no.nordicsemi.android.nrfmesh;

import java.util.UUID;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.Group;

public interface GroupCallbacks {

    /**
     * Creates a group
     *
     * @return {@link Group}
     */
    default Group createGroup() {
        //Default implementation
        return null;
    }

    /**
     * Creates a group
     *
     * @param name Group name
     * @return {@link Group}
     */
    default Group createGroup(@NonNull final String name) {
        //Default implementation
        return null;
    }

    /**
     * Creates a group
     *
     * @param name Group name
     * @return {@link Group}
     */
    default Group createGroup(@NonNull final String name, final int address) {
        //Default implementation
        return null;
    }

    /**
     * Creates a group
     *
     * @param uuid virtual label
     * @param name group name
     * @return {@link Group}
     */
    default Group createGroup(@NonNull final UUID uuid, final String name) {
        //Default implementation
        return null;
    }

    /**
     * Adds a group
     *
     * @param name    Name
     * @param address Address
     * @return true if successful or false otherwise
     */
    default boolean onGroupAdded(@NonNull final String name, final int address) {
        //Default implementation
        return false;
    }

    /**
     * Adds a group
     *
     * @param group {@link Group}
     * @return true if successful or false otherwise
     */
    default boolean onGroupAdded(@NonNull final Group group) {
        //Default implementation
        return false;
    }

    /**
     * Subscribe to a group
     *
     * @param group {@link Group}
     */
    default void subscribe(final Group group) {
        //Default implementation
    }
}
