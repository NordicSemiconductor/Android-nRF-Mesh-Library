package no.nordicsemi.android.mesh;

/**
 * Callbacks to notify importing from the Mesh Database JSON and loading a network from the local database
 */
interface LoadNetworkCallbacks {

    /**
     * Notifies when the mesh network is loaded from the local database
     *
     * @param meshNetwork {@link MeshNetwork}
     */
    void onNetworkLoadedFromDb(final MeshNetwork meshNetwork);

    /**
     * Notifies when the mesh network fails to load
     *
     * @param error error
     */
    void onNetworkLoadFailed(final String error);
}
