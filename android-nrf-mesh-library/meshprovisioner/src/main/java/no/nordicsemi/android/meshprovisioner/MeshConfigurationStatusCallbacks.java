package no.nordicsemi.android.meshprovisioner;

import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;

public interface MeshConfigurationStatusCallbacks {
    void onUnknownPduReceived(final ProvisionedMeshNode node);

    void onBlockAcknowledgementSent(final ProvisionedMeshNode node);

    void onBlockAcknowledgementReceived(final ProvisionedMeshNode node);

    void onGetCompositionDataSent(final ProvisionedMeshNode node);

    void onCompositionDataStatusReceived(final ProvisionedMeshNode node);

    void onAppKeyAddSent(final ProvisionedMeshNode node);

    void onAppKeyStatusReceived(final ProvisionedMeshNode node, final boolean success, int status, final int netKeyIndex, final int appKeyIndex);

    void onAppKeyBindSent(final ProvisionedMeshNode node);

    void onAppKeyBindStatusReceived(final ProvisionedMeshNode node, final boolean success, int status, final int elementAddress, final int appKeyIndex, final int modelIdentifier);

    void onPublicationSetSent(final ProvisionedMeshNode node);

    void onPublicationStatusReceived(final ProvisionedMeshNode node, final boolean success, final int status, final byte[] elementAddress, final byte[] publishAddress, final int modelIdentifier);

    void onSubscriptionAddSent(final ProvisionedMeshNode node);

    void onSubscriptionStatusReceived(final ProvisionedMeshNode node, final boolean success, final int status, final byte[] elementAddress, final byte[] subscriptionAddress, final int modelIdentifier);
}
