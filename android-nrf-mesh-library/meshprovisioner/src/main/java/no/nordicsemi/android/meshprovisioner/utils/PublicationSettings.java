package no.nordicsemi.android.meshprovisioner.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Contains the publication settings of a mesh model
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class PublicationSettings implements Parcelable {

    private static final int DEFAULT_PUBLISH_TTL = 0x7F;
    private static final int DEFAULT_PUBLICATION_STEPS = 0;
    private static final int DEFAULT_PUBLICATION_RESOLUTION = 0b00;
    private static final int DEFAULT_PUBLICATION_RETRANSMIT_COUNT = 0b000;
    private static final int DEFAULT_PUBLICATION_RETRANSMIT_INTERVAL_STEPS = 0;

    @Expose
    private int publishAddress;

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
    private int publishRetransmitCount = DEFAULT_PUBLICATION_RETRANSMIT_COUNT;

    @Expose
    private int publishRetransmitIntervalSteps = DEFAULT_PUBLICATION_RETRANSMIT_INTERVAL_STEPS;

    public PublicationSettings() {

    }

    /**
     * Constructs a ConfigModelPublicationSet message
     *
     * @param publishAddress                 Address to which the element must publish
     * @param appKeyIndex                    Index of the application key
     * @param credentialFlag                 Credentials flag define which credentials to be used, set true to use friendship credentials and false for master credentials. Currently supports only master credentials
     * @param publishRetransmitCount         Number of publication retransmits
     * @param publishRetransmitIntervalSteps Publish retransmit interval steps
     * @deprecated in favour of {@link #PublicationSettings(int, int, boolean, int, int)}
     */
    @Deprecated
    public PublicationSettings(final byte[] publishAddress,
                               final int appKeyIndex,
                               final boolean credentialFlag,
                               final int publishRetransmitCount,
                               final int publishRetransmitIntervalSteps) {
        this(AddressUtils.getUnicastAddressInt(publishAddress), appKeyIndex, credentialFlag,
                DEFAULT_PUBLISH_TTL,
                DEFAULT_PUBLICATION_STEPS,
                DEFAULT_PUBLICATION_RESOLUTION,
                DEFAULT_PUBLICATION_RETRANSMIT_COUNT,
                DEFAULT_PUBLICATION_RETRANSMIT_INTERVAL_STEPS);
    }

    /**
     * Constructs a ConfigModelPublicationSet message
     *
     * @param publishAddress                 Address to which the element must publish
     * @param appKeyIndex                    Index of the application key
     * @param credentialFlag                 Credentials flag define which credentials to be used, set true to use friendship credentials and false for master credentials. Currently supports only master credentials
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
                DEFAULT_PUBLICATION_RETRANSMIT_COUNT,
                DEFAULT_PUBLICATION_RETRANSMIT_INTERVAL_STEPS);
    }

    /**
     * Constructs a ConfigModelPublicationSet message
     *
     * @param publishAddress                 Address to which the element must publish
     * @param appKeyIndex                    Index of the application key
     * @param credentialFlag                 Credentials flag define which credentials to be used, set true to use friendship credentials and false for master credentials. Currently supports only master credentials
     * @param publishTtl                     Publication ttl
     * @param publicationSteps               Publication steps for the publication period
     * @param publicationResolution          Publication resolution of the publication period
     * @param publishRetransmitCount         Number of publication retransmits
     * @param publishRetransmitIntervalSteps Publish retransmit interval steps
     * @deprecated in favour of {@link #PublicationSettings(int, int, boolean, int, int, int, int, int)}
     */
    @Deprecated
    public PublicationSettings(final byte[] publishAddress,
                               final int appKeyIndex,
                               final boolean credentialFlag,
                               final int publishTtl,
                               final int publicationSteps,
                               final int publicationResolution,
                               final int publishRetransmitCount,
                               final int publishRetransmitIntervalSteps) {
        this(AddressUtils.getUnicastAddressInt(publishAddress), appKeyIndex, credentialFlag,
                publishTtl, publicationSteps, publicationResolution, publishRetransmitCount, publishRetransmitIntervalSteps);
    }

    /**
     * Constructs a ConfigModelPublicationSet message
     *
     * @param publishAddress                 Address to which the element must publish
     * @param appKeyIndex                    Index of the application key
     * @param credentialFlag                 Credentials flag define which credentials to be used, set true to use friendship credentials and false for master credentials. Currently supports only master credentials
     * @param publishTtl                     Publication ttl
     * @param publicationSteps               Publication steps for the publication period
     * @param publicationResolution          Publication resolution of the publication period
     * @param publishRetransmitCount         Number of publication retransmits
     * @param publishRetransmitIntervalSteps Publish retransmit interval steps
     */
    public PublicationSettings(final int publishAddress,
                               final int appKeyIndex,
                               final boolean credentialFlag,
                               final int publishTtl,
                               final int publicationSteps,
                               final int publicationResolution,
                               final int publishRetransmitCount,
                               final int publishRetransmitIntervalSteps) {
        this.publishAddress = publishAddress;
        this.appKeyIndex = appKeyIndex;
        this.credentialFlag = credentialFlag;
        this.publishTtl = publishTtl;
        this.publicationSteps = publicationSteps;
        this.publicationResolution = publicationResolution;
        this.publishRetransmitCount = publishRetransmitCount;
        this.publishRetransmitIntervalSteps = publishRetransmitIntervalSteps;
    }

    /**
     * Constructs a ConfigModelPublicationSet message
     *
     * @param publishAddress                 Address to which the element must publish
     * @param appKeyIndex                    Index of the application key
     * @param credentialFlag                 Credentials flag define which credentials to be used, set true to use friendship credentials and false for master credentials. Currently supports only master credentials
     * @param publishTtl                     Publication ttl
     * @param publicationSteps               Publication steps for the publication period
     * @param publicationResolution          Publication resolution of the publication period
     * @param publishRetransmitCount         Number of publication retransmits
     * @param publishRetransmitIntervalSteps Publish retransmit interval steps
     * @deprecated in favour of {@link #PublicationSettings(int, byte[], int, int, int, int, int, int)}
     */
    @Deprecated
    public PublicationSettings(final byte[] publishAddress,
                               final byte[] appKeyIndex,
                               final int credentialFlag,
                               final int publishTtl,
                               final int publicationSteps,
                               final int publicationResolution,
                               final int publishRetransmitCount,
                               final int publishRetransmitIntervalSteps) {
        this(AddressUtils.getUnicastAddressInt(publishAddress), appKeyIndex, credentialFlag,
                publishTtl, publicationSteps, publicationResolution, publishRetransmitCount, publishRetransmitIntervalSteps);
    }

    /**
     * Constructs a ConfigModelPublicationSet message
     *
     * @param publishAddress                 Address to which the element must publish
     * @param appKeyIndex                    Index of the application key
     * @param credentialFlag                 Credentials flag define which credentials to be used, set true to use friendship credentials and false for master credentials. Currently supports only master credentials
     * @param publishTtl                     Publication ttl
     * @param publicationSteps               Publication steps for the publication period
     * @param publicationResolution          Publication resolution of the publication period
     * @param publishRetransmitCount         Number of publication retransmits
     * @param publishRetransmitIntervalSteps Publish retransmit interval steps
     */
    public PublicationSettings(final int publishAddress,
                               final byte[] appKeyIndex,
                               final int credentialFlag,
                               final int publishTtl,
                               final int publicationSteps,
                               final int publicationResolution,
                               final int publishRetransmitCount,
                               final int publishRetransmitIntervalSteps) {
        this.publishAddress = publishAddress;
        this.appKeyIndex = ByteBuffer.wrap(appKeyIndex).order(ByteOrder.BIG_ENDIAN).getShort();
        this.credentialFlag = credentialFlag == 1;
        this.publishTtl = publishTtl;
        this.publicationSteps = publicationSteps;
        this.publicationResolution = publicationResolution;
        this.publishRetransmitCount = publishRetransmitCount;
        this.publishRetransmitIntervalSteps = publishRetransmitIntervalSteps;
    }

    private PublicationSettings(Parcel in) {
        publishAddress = in.readInt();
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
    public void setCredentialFlag(final boolean credentialFlag) {
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
    public void setPublishTtl(final int publishTtl) {
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
    public void setPublishRetransmitCount(final int publishRetransmitCount) {
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
    public void setPublishRetransmitIntervalSteps(final int publishRetransmitIntervalSteps) {
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
    public void setPublicationSteps(final int publicationSteps) {
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

    public void setPublicationResolution(final int publicationResolution) {
        this.publicationResolution = publicationResolution;
    }

    /**
     * Returns the publication period
     */
    public int calculatePublicationPeriod() {
        return ((publicationSteps << 6) | publicationResolution);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(publishAddress);
        dest.writeInt(appKeyIndex);
        dest.writeInt(credentialFlag ? 1 : 0);
        dest.writeInt(publishTtl);
        dest.writeInt(publicationSteps);
        dest.writeInt(publicationResolution);
        dest.writeInt(publishRetransmitCount);
        dest.writeInt(publishRetransmitIntervalSteps);
    }
}
