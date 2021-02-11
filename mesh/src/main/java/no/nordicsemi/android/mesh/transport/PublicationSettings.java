package no.nordicsemi.android.mesh.transport;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

import java.util.UUID;

import androidx.annotation.Nullable;
import no.nordicsemi.android.mesh.utils.MeshAddress;

import static no.nordicsemi.android.mesh.utils.MeshParserUtils.RESOLUTION_100_MS;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.RESOLUTION_10_M;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.RESOLUTION_10_S;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.RESOLUTION_1_S;

/**
 * Contains the publication settings of a mesh model
 */
public class PublicationSettings implements Parcelable {

    private static final int DEFAULT_PUBLISH_TTL = 0x7F;
    private static final int DEFAULT_PUBLICATION_STEPS = 0;
    private static final int DEFAULT_PUBLICATION_RESOLUTION = 0b00;
    public static final int MIN_PUBLICATION_RETRANSMIT_COUNT = 0b000;
    public static final int MAX_PUBLICATION_RETRANSMIT_COUNT = 0b111;
    private static final int DEFAULT_PUBLICATION_RETRANSMIT_INTERVAL_STEPS = 0b00000;
    private static final int MAX_PUBLICATION_RETRANSMIT_INTERVAL_STEPS = 0b11111;

    @Expose
    private int publishAddress;
    @Expose
    private UUID labelUUID;
    @Expose
    private int appKeyIndex;
    @Expose
    private boolean credentialFlag;
    @Expose
    private int publishTtl = DEFAULT_PUBLISH_TTL;
    @Expose
    private int publicationSteps = DEFAULT_PUBLICATION_STEPS;
    @Expose
    private int publicationResolution = DEFAULT_PUBLICATION_RESOLUTION;
    @Expose
    private int publishRetransmitCount = MIN_PUBLICATION_RETRANSMIT_COUNT;
    @Expose
    private int publishRetransmitIntervalSteps = DEFAULT_PUBLICATION_RETRANSMIT_INTERVAL_STEPS;

    PublicationSettings() {
    }

    /**
     * Constructs a PublicationSettings
     *
     * @param publishAddress                 Address to which the element must publish
     * @param appKeyIndex                    Index of the application key
     * @param credentialFlag                 Credentials flag define which credentials to be used, set true to use
     *                                       friendship credentials and false for master credentials. Currently supports only master credentials
     * @param publishRetransmitCount         Number of publication retransmits
     * @param publishRetransmitIntervalSteps Publish retransmit interval steps
     */
    public PublicationSettings(final int publishAddress,
                               final int appKeyIndex,
                               final boolean credentialFlag,
                               final int publishRetransmitCount,
                               final int publishRetransmitIntervalSteps) {
        this(publishAddress, appKeyIndex, credentialFlag,
                DEFAULT_PUBLISH_TTL,
                DEFAULT_PUBLICATION_STEPS,
                DEFAULT_PUBLICATION_RESOLUTION,
                MIN_PUBLICATION_RETRANSMIT_COUNT,
                DEFAULT_PUBLICATION_RETRANSMIT_INTERVAL_STEPS);
    }

    /**
     * Constructs a PublicationSettings
     *
     * @param publishAddress                 Address to which the element must publish
     * @param appKeyIndex                    Index of the application key
     * @param credentialFlag                 Credentials flag define which credentials to be used, set true to use
     *                                       friendship credentials and false for master credentials. Currently supports only master credentials
     * @param publishTtl                     Publication ttl
     * @param publicationSteps               Publication steps for the publication period
     * @param publicationResolution          Publication resolution of the publication period
     * @param publishRetransmitCount         Number of publication retransmits
     * @param publishRetransmitIntervalSteps Publish retransmit interval steps
     */
    PublicationSettings(final int publishAddress,
                        final int appKeyIndex,
                        final boolean credentialFlag,
                        final int publishTtl,
                        final int publicationSteps,
                        final int publicationResolution,
                        final int publishRetransmitCount,
                        final int publishRetransmitIntervalSteps) {
        this(publishAddress, null, appKeyIndex, credentialFlag, publishTtl,
                publicationSteps, publicationResolution, publishRetransmitCount, publishRetransmitIntervalSteps);
    }

    /**
     * Constructs a PublicationSettings
     *
     * @param publishAddress                 Address to which the element must publish
     * @param appKeyIndex                    Index of the application key
     * @param credentialFlag                 Credentials flag define which credentials to be used, set true to use
     *                                       friendship credentials and false for master credentials. Currently supports only master credentials
     * @param publishTtl                     Publication ttl
     * @param publicationSteps               Publication steps for the publication period
     * @param publicationResolution          Publication resolution of the publication period
     * @param publishRetransmitCount         Number of publication retransmits
     * @param publishRetransmitIntervalSteps Publish retransmit interval steps
     */
    PublicationSettings(final int publishAddress,
                        @Nullable final UUID labelUUID,
                        final int appKeyIndex,
                        final boolean credentialFlag,
                        final int publishTtl,
                        final int publicationSteps,
                        final int publicationResolution,
                        final int publishRetransmitCount,
                        final int publishRetransmitIntervalSteps) {
        this.publishAddress = publishAddress;
        this.labelUUID = labelUUID;
        this.appKeyIndex = appKeyIndex;
        this.credentialFlag = credentialFlag;
        this.publishTtl = publishTtl;
        this.publicationSteps = publicationSteps;
        this.publicationResolution = publicationResolution;
        this.publishRetransmitCount = publishRetransmitCount;
        this.publishRetransmitIntervalSteps = publishRetransmitIntervalSteps;
    }

    private PublicationSettings(Parcel in) {
        publishAddress = in.readInt();
        if (MeshAddress.isValidVirtualAddress(publishAddress)) {
            final ParcelUuid parcelUuid = in.readParcelable(ParcelUuid.class.getClassLoader());
            if (parcelUuid != null) {
                labelUUID = parcelUuid.getUuid();
            }
        }
        appKeyIndex = in.readInt();
        credentialFlag = in.readInt() == 1;
        publishTtl = in.readInt();
        publicationSteps = in.readInt();
        publicationResolution = in.readInt();
        publishRetransmitCount = in.readInt();
        publishRetransmitIntervalSteps = in.readInt();
    }

    public static final Parcelable.Creator<PublicationSettings> CREATOR = new Creator<PublicationSettings>() {
        @Override
        public PublicationSettings createFromParcel(Parcel in) {
            return new PublicationSettings(in);
        }

        @Override
        public PublicationSettings[] newArray(int size) {
            return new PublicationSettings[size];
        }
    };

    /**
     * Returns the publish address, this is the address the model may publish messages when set
     *
     * @return publish address
     */
    public int getPublishAddress() {
        return publishAddress;
    }

    /**
     * Sets a publish address for this model
     *
     * @param publishAddress publish address
     */
    public void setPublishAddress(final int publishAddress) {
        this.publishAddress = publishAddress;
    }

    /**
     * Returns the label uuid for thi model
     */
    @Nullable
    public UUID getLabelUUID() {
        return labelUUID;
    }

    /**
     * Sets the label uuid for the publication settings of the model
     *
     * @param labelUUID 16-byte label uuid
     */
    void setLabelUUID(@Nullable final UUID labelUUID) {
        this.labelUUID = labelUUID;
    }

    /**
     * Returns the app key index used for publishing by this model
     *
     * @return Global app key index
     */
    public int getAppKeyIndex() {
        return appKeyIndex;
    }

    /**
     * Set app key index to be used when publishing messages.
     *
     * @param appKeyIndex global application key index
     */
    public void setAppKeyIndex(final int appKeyIndex) {
        this.appKeyIndex = appKeyIndex;
    }

    public boolean getCredentialFlag() {
        return credentialFlag;
    }

    /**
     * Sets the credential flags true if friendship credentials is to be used or false if master credentials flags must be used.
     *
     * @param credentialFlag credential flag
     */
    void setCredentialFlag(final boolean credentialFlag) {
        this.credentialFlag = credentialFlag;
    }

    /**
     * Returns the ttl used for publication.
     *
     * @return publication ttl
     */
    public int getPublishTtl() {
        return publishTtl & 0xFF;
    }

    /**
     * Sets the ttl used for publication.
     */
    void setPublishTtl(final int publishTtl) {
        this.publishTtl = publishTtl;
    }

    /**
     * Returns the retransmit count used in publication
     *
     * @return publication retransmit count
     */
    public int getPublishRetransmitCount() {
        return publishRetransmitCount;
    }

    /**
     * Sets the retransmit count used in publication
     */
    void setPublishRetransmitCount(final int publishRetransmitCount) {
        this.publishRetransmitCount = publishRetransmitCount;
    }

    /**
     * Returns the retransmit interval steps used in publication
     *
     * @return publication retransmit interval steps
     */
    public int getPublishRetransmitIntervalSteps() {
        return publishRetransmitIntervalSteps;
    }

    /**
     * Sets the retransmit interval steps used in publication
     */
    void setPublishRetransmitIntervalSteps(final int publishRetransmitIntervalSteps) {
        this.publishRetransmitIntervalSteps = publishRetransmitIntervalSteps;
    }

    /**
     * Returns the publication steps used for publication
     *
     * @return publication steps
     */
    public int getPublicationSteps() {
        return publicationSteps;
    }

    /**
     * Sets the publication steps used for publication
     */
    void setPublicationSteps(final int publicationSteps) {
        this.publicationSteps = publicationSteps;
    }

    /**
     * Returns the resolution bit-field of publication steps. The resolution can be 100ms, 1 second, 10 seconds or 10 minutes
     *
     * @return resolution
     */
    public int getPublicationResolution() {
        return publicationResolution;
    }

    void setPublicationResolution(final int publicationResolution) {
        this.publicationResolution = publicationResolution;
    }

    /**
     * Encodes the publication period as an interval based on the resolution.
     */
    int serializePublicationResolution() {
        switch (publicationResolution) {
            default:
            case RESOLUTION_100_MS:
                return 100;
            case RESOLUTION_1_S:
                return 1000;
            case RESOLUTION_10_S:
                return 10 * 1000;
            case RESOLUTION_10_M:
                return 10 * 1000 * 60;
        }
    }

    /**
     * Decodes the publication period resolution.
     *
     * @param resolution publication period resolution
     */
    public static int deserializePublicationResolution(final int resolution) {
        switch (resolution) {
            default:
            case 100:
                return RESOLUTION_100_MS;
            case 1000:
                return RESOLUTION_1_S;
            case 10000:
                return RESOLUTION_10_S;
            case 600000:
                return RESOLUTION_10_M;
        }
    }

    /**
     * Returns the publish period in seconds
     */
    public int getPublishPeriod() {
        switch (publicationResolution) {
            default:
            case RESOLUTION_100_MS:
                return ((100 * publicationSteps) / 1000);
            case RESOLUTION_1_S:
                return publicationSteps;
            case RESOLUTION_10_S:
                return (10 * publicationSteps);
            case RESOLUTION_10_M:
                return (10 * publicationSteps) * 60;
        }
    }

    /**
     * Returns the publish period in seconds
     */
    public static int getPublishPeriod(final int publicationResolution, final int publicationSteps) {
        switch (publicationResolution) {
            default:
            case 0b00:
                return 100 * publicationSteps;
            case 0b01:
                return publicationSteps;
            case 0b10:
                return 10 * publicationSteps;
            case 0b11:
                return 10 * publicationSteps * 60;
        }
    }

    /**
     * Returns the retransmission interval in milliseconds
     */
    public int getRetransmissionInterval() {
        return (publishRetransmitIntervalSteps + 1) * 50;
    }

    /**
     * Returns the retransmit interval for a given number of retransmit interval steps in milliseconds
     *
     * @param intervalSteps Retransmit interval steps
     */
    public static int getRetransmissionInterval(final int intervalSteps) {
        if (intervalSteps >= DEFAULT_PUBLICATION_RETRANSMIT_INTERVAL_STEPS && intervalSteps <= MAX_PUBLICATION_RETRANSMIT_INTERVAL_STEPS)
            return (intervalSteps + 1) * 50;
        return 0;
    }

    /**
     * Returns the minimum retransmit interval supported in milliseconds
     */
    @SuppressWarnings("PointlessArithmeticExpression")
    public static int getMinRetransmissionInterval() {
        return (DEFAULT_PUBLICATION_RETRANSMIT_INTERVAL_STEPS + 1) * 50;
    }

    /**
     * Returns the maximum retransmit interval supported in milliseconds
     */
    public static int getMaxRetransmissionInterval() {
        return (MAX_PUBLICATION_RETRANSMIT_INTERVAL_STEPS + 1) * 50;
    }

    /**
     * Returns the retransmit interval steps from the retransmit interval
     *
     * @param retransmitInterval Retransmit interval in milliseconds
     */
    public static int parseRetransmitIntervalSteps(final int retransmitInterval) {
        if (retransmitInterval >= 0 && retransmitInterval <= getMaxRetransmissionInterval()) {
            return ((retransmitInterval / 50) - 1);
        }
        throw new IllegalArgumentException("Invalid retransmit interval");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(publishAddress);
        if (MeshAddress.isValidVirtualAddress(publishAddress)) {
            dest.writeParcelable(new ParcelUuid(labelUUID), flags);
        }
        dest.writeInt(appKeyIndex);
        dest.writeInt(credentialFlag ? 1 : 0);
        dest.writeInt(publishTtl);
        dest.writeInt(publicationSteps);
        dest.writeInt(publicationResolution);
        dest.writeInt(publishRetransmitCount);
        dest.writeInt(publishRetransmitIntervalSteps);
    }
}
