package no.nordicsemi.android.mesh;

import androidx.annotation.NonNull;

import java.util.List;

import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;

interface MeshNetworkCallbacks {

    void onMeshNetworkUpdated();

    void onNetworkKeyAdded(@NonNull final NetworkKey networkKey);

    void onNetworkKeyUpdated(@NonNull final NetworkKey networkKey);

    void onNetworkKeyDeleted(@NonNull final NetworkKey networkKey);

    void onApplicationKeyAdded(@NonNull final ApplicationKey applicationKey);

    void onApplicationKeyUpdated(@NonNull final ApplicationKey applicationKey);

    void onApplicationKeyDeleted(@NonNull final ApplicationKey applicationKey);

    void onProvisionerAdded(@NonNull final Provisioner provisioner);

    void onProvisionerUpdated(@NonNull final Provisioner provisioner);

    void onProvisionersUpdated(@NonNull final List<Provisioner> provisioner);

    void onProvisionerDeleted(@NonNull final Provisioner provisioner);

    void onNodeDeleted(@NonNull final ProvisionedMeshNode meshNode);

    void onNodeAdded(@NonNull final ProvisionedMeshNode meshNode);

    void onNodeUpdated(@NonNull final ProvisionedMeshNode meshNode);

    void onNodesUpdated();

    void onGroupAdded(@NonNull final Group group);

    void onGroupUpdated(@NonNull final Group group);

    void onGroupDeleted(@NonNull final Group group);

    void onSceneAdded(@NonNull final Scene scene);

    void onSceneUpdated(@NonNull final Scene scene);

    void onSceneDeleted(@NonNull final Scene scene);
}
