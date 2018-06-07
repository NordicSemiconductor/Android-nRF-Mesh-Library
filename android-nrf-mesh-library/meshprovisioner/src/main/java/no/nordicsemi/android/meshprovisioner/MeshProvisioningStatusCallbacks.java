package no.nordicsemi.android.meshprovisioner;

import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.states.UnprovisionedMeshNode;

public interface MeshProvisioningStatusCallbacks {

    void onProvisioningInviteSent(final UnprovisionedMeshNode unprovisionedMeshNode);

    void onProvisioningCapabilitiesReceived(final UnprovisionedMeshNode unprovisionedMeshNode);

    void onProvisioningStartSent(final UnprovisionedMeshNode unprovisionedMeshNode);

    void onProvisioningPublicKeySent(final UnprovisionedMeshNode unprovisionedMeshNode);

    void onProvisioningPublicKeyReceived(final UnprovisionedMeshNode unprovisionedMeshNode);

    void onProvisioningAuthenticationInputRequested(final UnprovisionedMeshNode unprovisionedMeshNode);

    void onProvisioningInputCompleteSent(final UnprovisionedMeshNode unprovisionedMeshNode);

    void onProvisioningConfirmationSent(final UnprovisionedMeshNode unprovisionedMeshNode);

    void onProvisioningConfirmationReceived(final UnprovisionedMeshNode unprovisionedMeshNode);

    void onProvisioningRandomSent(final UnprovisionedMeshNode unprovisionedMeshNode);

    void onProvisioningRandomReceived(final UnprovisionedMeshNode unprovisionedMeshNode);

    void onProvisioningDataSent(final UnprovisionedMeshNode unprovisionedMeshNode);

    void onProvisioningFailed(final UnprovisionedMeshNode unprovisionedMeshNode, final String error);

    void onProvisioningComplete(final ProvisionedMeshNode provisionedMeshNode);

}
