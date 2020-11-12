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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.models.SigModelParser;
import no.nordicsemi.android.mesh.models.VendorModel;
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.mesh.utils.DeviceFeatureUtils;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * To be used as a wrapper class for when creating the ConfigCompositionDataStatus Message.
 */
@SuppressWarnings({"WeakerAccess"})
public class ConfigCompositionDataStatus extends ConfigStatusMessage implements Parcelable {

    private static final String TAG = ConfigCompositionDataStatus.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_COMPOSITION_DATA_STATUS;
    private static final int ELEMENTS_OFFSET = 12;
    private int companyIdentifier;
    private int productIdentifier;
    private int versionIdentifier;
    private int crpl;
    private int features;
    private boolean relayFeatureSupported;
    private boolean proxyFeatureSupported;
    private boolean friendFeatureSupported;
    private boolean lowPowerFeatureSupported;
    private Map<Integer, Element> mElements = new LinkedHashMap<>();

    private static final Creator<ConfigCompositionDataStatus> CREATOR = new Creator<ConfigCompositionDataStatus>() {
        @Override
        public ConfigCompositionDataStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            return new ConfigCompositionDataStatus(message);
        }

        @Override
        public ConfigCompositionDataStatus[] newArray(int size) {
            return new ConfigCompositionDataStatus[size];
        }
    };

    /**
     * Constructs the ConfigCompositionDataStatus mMessage.
     *
     * @param message Access Message
     */
    public ConfigCompositionDataStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    final void parseStatusParameters() {
        parseCompositionDataPages();
    }

    /**
     * Parses composition data status received from the mesh node
     */
    private void parseCompositionDataPages() {
        final AccessMessage message = (AccessMessage) mMessage;
        final byte[] accessPayload = message.getAccessPdu();

        //Bluetooth SIG 16-bit company identifier
        companyIdentifier = MeshParserUtils.unsignedBytesToInt(accessPayload[2], accessPayload[3]);
        Log.v(TAG, "Company identifier: " + String.format(Locale.US, "%04X", companyIdentifier));

        //16-bit vendor-assigned product identifier;
        productIdentifier = MeshParserUtils.unsignedBytesToInt(accessPayload[4], accessPayload[5]);
        Log.v(TAG, "Product identifier: " + String.format(Locale.US, "%04X", productIdentifier));

        //16-bit vendor-assigned product version identifier;
        versionIdentifier = MeshParserUtils.unsignedBytesToInt(accessPayload[6], accessPayload[7]);
        Log.v(TAG, "Version identifier: " + String.format(Locale.US, "%04X", versionIdentifier));

        //16-bit representation of the minimum number of replay protection list entries in a device
        crpl = MeshParserUtils.unsignedBytesToInt(accessPayload[8], accessPayload[9]);
        Log.v(TAG, "crpl: " + String.format(Locale.US, "%04X", crpl));

        //16-bit device features
        features = MeshParserUtils.unsignedBytesToInt(accessPayload[10], accessPayload[11]);
        Log.v(TAG, "Features: " + String.format(Locale.US, "%04X", features));

        relayFeatureSupported = DeviceFeatureUtils.supportsRelayFeature(features);
        Log.v(TAG, "Relay feature: " + relayFeatureSupported);

        proxyFeatureSupported = DeviceFeatureUtils.supportsProxyFeature(features);
        Log.v(TAG, "Proxy feature: " + proxyFeatureSupported);

        friendFeatureSupported = DeviceFeatureUtils.supportsFriendFeature(features);
        Log.v(TAG, "Friend feature: " + friendFeatureSupported);

        lowPowerFeatureSupported = DeviceFeatureUtils.supportsLowPowerFeature(features);
        Log.v(TAG, "Low power feature: " + lowPowerFeatureSupported);

        // Parsing the elements which is a variable number of octets
        // Elements contain following
        // location descriptor,
        // Number of SIG model IDs in this element
        // Number of vendor model in this element
        // SIG model ID octents - Variable
        // Vendor model ID octents - Variable
        parseElements(accessPayload, message.getSrc());
        Log.v(TAG, "Number of elements: " + mElements.size());
    }

    /**
     * Parses the elements within the composition data status
     *
     * @param accessPayload underlying payload containing the elements
     * @param src           source address
     */
    private void parseElements(final byte[] accessPayload, final int src) {
        int tempOffset = ELEMENTS_OFFSET;
        int counter = 0;
        int elementAddress = 0;
        while (tempOffset < accessPayload.length) {
            final Map<Integer, MeshModel> models = new LinkedHashMap<>();
            final int locationDescriptor = accessPayload[tempOffset + 1] << 8 | accessPayload[tempOffset];
            Log.v(TAG, "Location identifier: " + String.format(Locale.US, "%04X", locationDescriptor));

            tempOffset = tempOffset + 2;
            final int numSigModelIds = accessPayload[tempOffset];
            Log.v(TAG, "Number of sig models: " + String.format(Locale.US, "%04X", numSigModelIds));

            tempOffset = tempOffset + 1;
            final int numVendorModelIds = accessPayload[tempOffset];
            Log.v(TAG, "Number of vendor models: " + String.format(Locale.US, "%04X", numVendorModelIds));

            tempOffset = tempOffset + 1;
            if (numSigModelIds > 0) {
                for (int i = 0; i < numSigModelIds; i++) {
                    final int modelId = MeshParserUtils.unsignedBytesToInt(accessPayload[tempOffset], accessPayload[tempOffset + 1]);
                    models.put(modelId, SigModelParser.getSigModel(modelId)); // sig models are 16-bit
                    Log.v(TAG, "Sig model ID " + i + " : " + String.format(Locale.US, "%04X", modelId));
                    tempOffset = tempOffset + 2;
                }
            }

            if (numVendorModelIds > 0) {
                for (int i = 0; i < numVendorModelIds; i++) {
                    // vendor models are 32-bit that contains a 16-bit company identifier and a 16-bit model identifier
                    final int companyIdentifier = MeshParserUtils.unsignedBytesToInt(accessPayload[tempOffset], accessPayload[tempOffset + 1]);
                    final int modelIdentifier = MeshParserUtils.unsignedBytesToInt(accessPayload[tempOffset + 2], accessPayload[tempOffset + 3]);
                    final int vendorModelIdentifier = companyIdentifier << 16 | modelIdentifier;
                    models.put(vendorModelIdentifier, new VendorModel(vendorModelIdentifier));
                    Log.v(TAG, "Vendor - model ID " + i + " : " + String.format(Locale.US, "%08X", vendorModelIdentifier));
                    tempOffset = tempOffset + 4;
                }
            }

            if (counter == 0) {
                elementAddress = src;
            } else {
                elementAddress++;
            }
            counter++;
            final Element element = new Element(elementAddress, locationDescriptor, models);
            final int unicastAddress = elementAddress;
            mElements.put(unicastAddress, element);
        }
    }

    /**
     * Returns the 16-bit company identifier assigned by Bluetooth SIG.
     *
     * @return company identifier
     */
    public int getCompanyIdentifier() {
        return companyIdentifier;
    }

    /**
     * Returns the 16-bit vendor assigned assigned product identifier.
     *
     * @return product identifier
     */
    public int getProductIdentifier() {
        return productIdentifier;
    }

    /**
     * Returns the 16-bit vendor assigned product version identifier.
     *
     * @return version identifier
     */
    public int getVersionIdentifier() {
        return versionIdentifier;
    }

    /**
     * Returns a 16-bit value representing the minimum number of replay protection list entries in a device.
     *
     * @return crpl
     */
    public int getCrpl() {
        return crpl;
    }

    /**
     * Returns a 16-bit features field indicating the device features.
     *
     * @return features field
     */
    public int getFeatures() {
        return features;
    }

    /**
     * Returns if the relay feature is supported.
     *
     * @return true if relay features is supported or false otherwise
     */
    public boolean isRelayFeatureSupported() {
        return relayFeatureSupported;
    }

    /**
     * Returns if the proxy feature is supported.
     *
     * @return true if proxy feature is supported or false otherwise
     */
    public boolean isProxyFeatureSupported() {
        return proxyFeatureSupported;
    }

    /**
     * Returns if the friend feature is supported.
     *
     * @return true if friend feature is supported or false otherwise
     */
    public boolean isFriendFeatureSupported() {
        return friendFeatureSupported;
    }

    /**
     * Returns if the low power feature is supported.
     *
     * @return true if low power feature is supported or false otherwise
     */
    public boolean isLowPowerFeatureSupported() {
        return lowPowerFeatureSupported;
    }

    /**
     * Returns the number of elements existing in this node.
     *
     * @return number of elements
     */
    public Map<Integer, Element> getElements() {
        return mElements;
    }

    private int parseCompanyIdentifier(final short companyIdentifier) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(companyIdentifier).getShort(0);
    }

    private int parseProductIdentifier(final short productIdentifier) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(productIdentifier).getShort(0);
    }

    private int parseVersionIdentifier(final short versionIdentifier) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(versionIdentifier).getShort(0);
    }

    private int parseCrpl(final short companyIdentifier) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(companyIdentifier).getShort(0);
    }

    private int parseFeatures(final short companyIdentifier) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(companyIdentifier).getShort(0);
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
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
