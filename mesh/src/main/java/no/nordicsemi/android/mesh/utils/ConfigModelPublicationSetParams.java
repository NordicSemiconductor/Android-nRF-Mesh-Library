package no.nordicsemi.android.mesh.utils;


import androidx.annotation.NonNull;

import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;

@SuppressWarnings("unused")
public class ConfigModelPublicationSetParams {

    private final ProvisionedMeshNode meshNode;
    private final byte[] elementAddress;
    private final byte[] publishAddress;
    private final int appKeyIndex;
    private int aszmic;
    private boolean credentialFlag;
    private int publishTtl;
    private int publicationSteps;
    private int publicationResolution;
    private int publishRetransmitCount;
    private int publishRetransmitIntervalSteps;
    private int modelIdentifier; //16-bit SIG Model or 32-bit Vendor Model identifier

    public ConfigModelPublicationSetParams(@NonNull final ProvisionedMeshNode mProvisionedMeshNode,
                                        @NonNull final byte[] elementAddress,
                                        final int modelIdentifier,
                                        @NonNull final byte[] publishAddress,
                                        final int appKeyIndex) throws IllegalArgumentException {
        this.meshNode = mProvisionedMeshNode;
        if(elementAddress.length != 2 )
            throw new IllegalArgumentException("Element address must be 2 bytes");
        this.elementAddress = elementAddress;
        this.modelIdentifier = modelIdentifier;
        if(publishAddress.length != 2 )
            throw new IllegalArgumentException("Publish address must be 2 bytes");
        this.publishAddress = publishAddress;
        this.appKeyIndex = appKeyIndex;

    }

    public byte[] getElementAddress() {
        return elementAddress;
    }

    public byte[] getPublishAddress() {
        return publishAddress;
    }

    public int getAppKeyIndex() {
        return appKeyIndex;
    }

    public int getAszmic() {
        return aszmic;
    }

    public boolean getCredentialFlag() {
        return credentialFlag;
    }

    public int getPublishTtl() {
        return publishTtl;
    }

    public int getPublicationSteps() {
        return publicationSteps;
    }

    public int getPublicationResolution() {
        return publicationResolution;
    }

    public int getPublishRetransmitCount() {
        return publishRetransmitCount;
    }

    public int getPublishRetransmitIntervalSteps() {
        return publishRetransmitIntervalSteps;
    }

    public int getModelIdentifier() {
        return modelIdentifier;
    }

    public void setCredentialFlag(final boolean credentialFlag) {
        this.credentialFlag = credentialFlag;
    }

    public void setPublishTtl(final int publishTtl) {
        this.publishTtl = publishTtl;
    }

    public void setPublicationSteps(final int publicationSteps) {
        this.publicationSteps = publicationSteps;
    }

    public void setPublicationResolution(final int publicationResolution) {
        this.publicationResolution = publicationResolution;
    }

    public void setPublishRetransmitCount(final int publisRetransmitCount) {
        this.publishRetransmitCount = publisRetransmitCount;
    }

    public void setPublishRetransmitIntervalSteps(final int retransmitIntervalSteps) {
        this.publishRetransmitIntervalSteps = retransmitIntervalSteps;
    }

    public ProvisionedMeshNode getMeshNode() {
        return meshNode;
    }
}
