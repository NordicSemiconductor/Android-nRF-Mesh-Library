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
import android.util.SparseArray;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.NetworkKey;

@SuppressWarnings({"WeakerAccess", "unused"})
abstract class Message implements Parcelable {

    protected int ctl;                              // If ctl = 0 access message and ctl = 1 control message
    protected SparseArray<byte[]> networkLayerPdu;  // Mesh pdu
    private int pduType;                            // PDU Type
    private int ttl = 100;                          // Time to live
    private int src;                                // Source address
    private int dst;                                // Destination address
    private byte[] mSequenceNumber;                 // unique 24-bit value for each message
    private byte[] deviceKey;                       // Used for transport layer encryption of configuration messages
    private ApplicationKey applicationKey;          // Used for transport layer encryption of application messages
    private NetworkKey networkKey;                  // Used for transport layer encryption of application messages
    private byte[] encryptionKey;                   // Derived from network key using k2 function
    private byte[] privacyKey;                      // Derived from privacy key using k2 function
    private int akf;                                // Use device key for encryption if akf = 0 or application key otherwise
    private int aid;                                // Used to identify the application key generated using k4 function
    private int aszmic;                             // if aszmic = 0 the transmic is 32-bits, if aszmic = 1 transmic 64-bits this is usually for a segmented message
    private int opCode;                             // Opcode of message
    private byte[] parameters;                      // Parameters of the message
    private int companyIdentifier;                  // Company identifier for vendor model messages
    private byte[] ivIndex;                         // IV Index of the network
    private boolean segmented;

    Message() {
    }

    protected Message(final Parcel source) {
        ctl = source.readInt();
        networkLayerPdu = readSparseArrayToParcelable(source);
        pduType = source.readInt();
        ttl = source.readInt();
        src = source.readInt();
        dst = source.readInt();
        mSequenceNumber = source.createByteArray();
        deviceKey = source.createByteArray();
        applicationKey = (ApplicationKey) source.readValue(ApplicationKey.class.getClassLoader());
        networkKey = (NetworkKey) source.readValue(NetworkKey.class.getClassLoader());
        encryptionKey = source.createByteArray();
        privacyKey = source.createByteArray();
        akf = source.readInt();
        aid = source.readInt();
        aszmic = source.readInt();
        opCode = source.readInt();
        parameters = source.createByteArray();
        companyIdentifier = source.readInt();
        ivIndex = source.createByteArray();
        segmented = source.readInt() == 1;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(ctl);
        writeSparseArrayToParcelable(dest, networkLayerPdu);
        dest.writeInt(pduType);
        dest.writeInt(ttl);
        dest.writeInt(src);
        dest.writeInt(dst);
        dest.writeByteArray(mSequenceNumber);
        dest.writeByteArray(deviceKey);
        dest.writeValue(applicationKey);
        dest.writeValue(networkKey);
        dest.writeByteArray(encryptionKey);
        dest.writeByteArray(privacyKey);
        dest.writeInt(akf);
        dest.writeInt(aid);
        dest.writeInt(aszmic);
        dest.writeInt(opCode);
        dest.writeByteArray(parameters);
        dest.writeInt(companyIdentifier);
        dest.writeByteArray(ivIndex);
        dest.writeInt(segmented ? 1 : 0);
    }

    public abstract int getCtl();

    int getPduType() {
        return pduType;
    }

    void setPduType(final int pduType) {
        this.pduType = pduType;
    }

    public final int getTtl() {
        return ttl;
    }

    public final void setTtl(final int ttl) {
        this.ttl = ttl;
    }

    public final int getSrc() {
        return src;
    }

    public final void setSrc(final int src) {
        this.src = src;
    }

    public final int getDst() {
        return dst;
    }

    public final void setDst(final int dst) {
        this.dst = dst;
    }

    public final byte[] getSequenceNumber() {
        return mSequenceNumber;
    }

    public final void setSequenceNumber(final byte[] sequenceNumber) {
        this.mSequenceNumber = sequenceNumber;
    }

    public final byte[] getDeviceKey() {
        return deviceKey;
    }

    public final void setDeviceKey(final byte[] deviceKey) {
        this.deviceKey = deviceKey;
    }

    public final ApplicationKey getApplicationKey() {
        return applicationKey;
    }

    public final void setApplicationKey(final ApplicationKey key) {
        this.applicationKey = key;
    }

    public NetworkKey getNetworkKey() {
        return networkKey;
    }

    public void setNetworkKey(final NetworkKey networkKey) {
        this.networkKey = networkKey;
    }

    public final byte[] getEncryptionKey() {
        return encryptionKey;
    }

    public final void setEncryptionKey(final byte[] encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public final byte[] getPrivacyKey() {
        return privacyKey;
    }

    public final void setPrivacyKey(final byte[] privacyKey) {
        this.privacyKey = privacyKey;
    }

    public final int getAkf() {
        return akf;
    }

    public final void setAkf(final int akf) {
        this.akf = akf;
    }

    public final int getAid() {
        return aid;
    }

    public final void setAid(final int aid) {
        this.aid = aid;
    }

    public final int getAszmic() {
        return aszmic;
    }

    public final void setAszmic(final int aszmic) {
        this.aszmic = aszmic;
    }

    public final int getOpCode() {
        return opCode;
    }

    public final void setOpCode(final int opCode) {
        this.opCode = opCode;
    }

    public final byte[] getParameters() {
        return parameters;
    }

    public final void setParameters(final byte[] parameters) {
        this.parameters = parameters;
    }

    public final int getCompanyIdentifier() {
        return companyIdentifier;
    }

    public final void setCompanyIdentifier(final int companyIdentifier) {
        this.companyIdentifier = companyIdentifier;
    }

    public final byte[] getIvIndex() {
        return ivIndex;
    }

    public final void setIvIndex(final byte[] ivIndex) {
        this.ivIndex = ivIndex;
    }

    public final boolean isSegmented() {
        return segmented;
    }

    final void setSegmented(final boolean segmented) {
        this.segmented = segmented;
    }

    public final SparseArray<byte[]> getNetworkLayerPdu() {
        return networkLayerPdu;
    }

    final void setNetworkLayerPdu(final SparseArray<byte[]> pdu) {
        networkLayerPdu = pdu;
    }

    protected final void writeSparseArrayToParcelable(final Parcel dest, final SparseArray<byte[]> array) {
        final int size = array.size();
        dest.writeInt(size);
        for (int i = 0; i < size; i++) {
            dest.writeByteArray(array.valueAt(i));
        }
    }

    protected final SparseArray<byte[]> readSparseArrayToParcelable(final Parcel src) {
        final SparseArray<byte[]> array = new SparseArray<>();
        final int size = src.readInt();
        for (int i = 0; i < size; i++) {
            array.put(i, src.createByteArray());
        }
        return array;
    }
}
