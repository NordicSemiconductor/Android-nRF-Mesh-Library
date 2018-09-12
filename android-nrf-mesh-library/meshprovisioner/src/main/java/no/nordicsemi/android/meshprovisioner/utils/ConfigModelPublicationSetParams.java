package no.nordicsemi.android.meshprovisioner.utils;


import android.support.annotation.NonNull;

import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;

public class ConfigModelPublicationSetParams {

    private final ProvisionedMeshNode meshNode;
    private final byte[] src;
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
                                        @NonNull final Integer modelIdentifier,
                                        @NonNull final byte[] publishAddress,
                                        @NonNull final Integer appKeyIndex) throws IllegalArgumentException{
        if(mProvisionedMeshNode == null)
            throw new IllegalArgumentException("Mesh node cannot be null");

        this.meshNode = mProvisionedMeshNode;
        this.src = mProvisionedMeshNode.getConfigurationSrc();

        if(elementAddress == null)
            throw new IllegalArgumentException("Element address cannot be null");
        this.elementAddress = elementAddress;

        if(modelIdentifier == null)
            throw new IllegalArgumentException("Model identifier cannot be null");
        this.modelIdentifier = modelIdentifier;

        if(publishAddress == null)
            throw new IllegalArgumentException("Publish address cannot be null");
        this.publishAddress = publishAddress;

        if(appKeyIndex == null)
            throw new IllegalArgumentException("App key index cannot be null");
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
