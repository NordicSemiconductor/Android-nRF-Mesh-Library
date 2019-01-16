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

package no.nordicsemi.android.meshprovisioner.transport;

import android.util.SparseArray;

@SuppressWarnings({"WeakerAccess", "unused"})
abstract class Message {

    /**
     * ctl, if ctl = 0 access message and ctl = 1 control message
     **/
    protected int ctl;
    protected SparseArray<byte[]> networkPdu;
    /**
     * pdu type
     **/
    private int pduType;
    /**
     * ttl, time to live
     **/
    private int ttl = 100;
    /**
     * src, source address
     **/
    private byte[] src;
    /**
     * mDst, destination address
     **/
    private byte[] dst;
    /**
     * sequence number, which is unique 24-bit value for each message
     **/
    private byte[] mSequenceNumber;
    /**
     * key, used for encryption in transport layer which could be application key or device key
     **/
    private byte[] key;
    /**
     * encryption key, derived from k2 using network key
     **/
    private byte[] encryptionKey;
    /**
     * privacy key, derived from k2 using network key
     **/
    private byte[] privacyKey;
    /**
     * akf if akf = 0 device key to be used for encryption in the transport layer if not use application key
     **/
    private int akf;
    /**
     * aid, if akf = 0 aid is also 0 if not aid is the identifier for the key used for encrytpion
     **/
    private int aid;
    /**
     * aszmic, if aszmic = 0 the transmic is 32-bits, if aszmic = 1 transmic 64-bits this is usually for a segmented message
     **/
    private int aszmic;
    /**
     * opcode, operation code for the message
     **/
    private int opCode;
    /**
     * parameters, opcode parameters
     **/
    private byte[] parameters;
    private int companyIdentifier;
    private byte[] ivIndex;
    private boolean segmented;

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

    public final byte[] getSrc() {
        return src;
    }

    public final void setSrc(final byte[] src) {
        this.src = src;
    }

    public final byte[] getDst() {
        return dst;
    }

    public final void setDst(final byte[] dst) {
        this.dst = dst;
    }

    public final byte[] getSequenceNumber() {
        return mSequenceNumber;
    }

    public final void setSequenceNumber(final byte[] sequenceNumber) {
        this.mSequenceNumber = sequenceNumber;
    }

    public final byte[] getKey() {
        return key;
    }

    public final void setKey(final byte[] key) {
        this.key = key;
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

    public final SparseArray<byte[]> getNetworkPdu() {
        return networkPdu;
    }

    final void setNetworkPdu(final SparseArray<byte[]> pdu) {
        networkPdu = pdu;
    }
}
