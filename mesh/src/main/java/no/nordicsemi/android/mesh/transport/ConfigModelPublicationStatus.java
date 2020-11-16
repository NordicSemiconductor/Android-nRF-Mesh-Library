/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.mesh.transport;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * To be used as a wrapper class for when creating the ConfigModelAppStatus Message.
 */
@SuppressWarnings({"WeakerAccess"})
public class ConfigModelPublicationStatus extends ConfigStatusMessage implements Parcelable {

    private static final String TAG = ConfigModelPublicationStatus.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_MODEL_PUBLICATION_STATUS;
    private static final int CONFIG_MODEL_PUBLICATION_STATUS_SIG_MODEL_PDU_LENGTH = 12;
    private static final int CONFIG_MODEL_APP_BIND_STATUS_VENDOR_MODEL_PDU_LENGTH = 14;
    private int mElementAddress;
    private int publishAddress;
    private int mAppKeyIndex;
    private boolean credentialFlag;
    private int publishTtl;
    private int publicationSteps;
    private int publicationResolution;
    private int publishRetransmitCount;
    private int publishRetransmitIntervalSteps;
    private int mModelIdentifier; //16-bit SIG Model or 32-bit Vendor Model identifier

    private static final Creator<ConfigModelPublicationStatus> CREATOR = new Creator<ConfigModelPublicationStatus>() {
        @Override
        public ConfigModelPublicationStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            return new ConfigModelPublicationStatus(message);
        }

        @Override
        public ConfigModelPublicationStatus[] newArray(int size) {
            return new ConfigModelPublicationStatus[size];
        }
    };

    /**
     * Constructs the ConfigModelAppStatus mMessage.
     *
     * @param message Access Message
     */
    public ConfigModelPublicationStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    final void parseStatusParameters() {
        final AccessMessage message = (AccessMessage) mMessage;
        mStatusCode = mParameters[0];
        mStatusCodeName = getStatusCodeName(mStatusCode);
        mElementAddress = MeshParserUtils.unsignedBytesToInt(mParameters[1], mParameters[2]);
        publishAddress = MeshParserUtils.unsignedBytesToInt(mParameters[3], mParameters[4]);
        final byte[] appKeyIndex = new byte[]{(byte) (mParameters[6] & 0x0F), mParameters[5]};
        mAppKeyIndex = ByteBuffer.wrap(appKeyIndex).order(ByteOrder.BIG_ENDIAN).getShort();
        credentialFlag = (mParameters[6] & 0xF0) >> 4 == 1;
        publishTtl = MeshParserUtils.unsignedByteToInt(mParameters[7]);
        final int publishPeriod = MeshParserUtils.unsignedByteToInt(mParameters[8]);
        publicationSteps = publishPeriod & 0x3F;
        publicationResolution = publishPeriod >> 6;
        final int publishRetransmission = MeshParserUtils.unsignedByteToInt(mParameters[9]);
        publishRetransmitCount = publishRetransmission & 0x07;
        publishRetransmitIntervalSteps = publishRetransmission >> 3;

        final byte[] modelIdentifier;
        if (mParameters.length == CONFIG_MODEL_PUBLICATION_STATUS_SIG_MODEL_PDU_LENGTH) {
            mModelIdentifier = MeshParserUtils.unsignedBytesToInt(mParameters[10], mParameters[11]);
        } else {
            modelIdentifier = new byte[]{mParameters[11], mParameters[10], mParameters[13], mParameters[12]};
            mModelIdentifier = ByteBuffer.wrap(modelIdentifier).order(ByteOrder.BIG_ENDIAN).getInt();
        }

        Log.v(TAG, "Status code: " + mStatusCode);
        Log.v(TAG, "Status message: " + mStatusCodeName);
        Log.v(TAG, "Element address: " + MeshAddress.formatAddress(mElementAddress, false));
        Log.v(TAG, "Publish Address: " + MeshAddress.formatAddress(publishAddress, false));
        Log.v(TAG, "App key index: " + MeshParserUtils.bytesToHex(appKeyIndex, false));
        Log.v(TAG, "Credential Flag: " + credentialFlag);
        Log.v(TAG, "Publish TTL: " + publishTtl);
        Log.v(TAG, "Publish Period where steps: " + publicationSteps + " and resolution: " + publicationResolution);
        Log.v(TAG, "Publish Retransmit Count: " + publishRetransmitCount);
        Log.v(TAG, "Publish Retransmit Interval Steps: " + publishRetransmitIntervalSteps);
        Log.v(TAG, "Model Identifier: " + Integer.toHexString(mModelIdentifier));
        Log.v(TAG, "Publication status: " + MeshParserUtils.bytesToHex(mParameters, false));
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    /**
     * Returns the element address that the key was bound to
     *
     * @return element address
     */
    public int getElementAddress() {
        return mElementAddress;
    }

    /**
     * Returns the global app key index.
     *
     * @return appkey index
     */
    public final int getAppKeyIndex() {
        return mAppKeyIndex;
    }

    /**
     * Returns if the message was successful or not.
     *
     * @return true if successful or false otherwise
     */
    public boolean isSuccessful() {
        return mStatusCode == 0x00;
    }

    /**
     * Returns the publish address to which the model must publish to
     */
    public int getPublishAddress() {
        return publishAddress;
    }

    /**
     * Returns the credential flag to be used for this message.
     *
     * @return true if friendship credentials to be used or false if master credentials is to be used.
     */
    public boolean getCredentialFlag() {
        return credentialFlag;
    }

    /**
     * Returns the ttl of publication messages
     *
     * @return publication ttl
     */
    public int getPublishTtl() {
        return publishTtl;
    }

    /**
     * Returns the number of publication steps.
     *
     * @return number of steps
     */
    public int getPublicationSteps() {
        return publicationSteps;
    }

    /**
     * Returns the resolution for the publication steps.
     *
     * @return resolution
     */
    public int getPublicationResolution() {
        return publicationResolution;
    }

    /**
     * Returns the number of retransmissions for each published message.
     *
     * @return number of retransmits
     */
    public int getPublishRetransmitCount() {
        return publishRetransmitCount;
    }

    /**
     * Returns the number of 50-milliseconds steps between retransmissions.
     *
     * @return retransmit interval steps
     */
    public int getPublishRetransmitIntervalSteps() {
        return publishRetransmitIntervalSteps;
    }

    /**
     * Returns the model identifier to which the key is to be bound.
     *
     * @return 16-bit or 32-bit vendor model identifier
     */
    public int getModelIdentifier() {
        return mModelIdentifier;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        final AccessMessage message = (AccessMessage) mMessage;
        dest.writeParcelable(message, flags);
    }
}
