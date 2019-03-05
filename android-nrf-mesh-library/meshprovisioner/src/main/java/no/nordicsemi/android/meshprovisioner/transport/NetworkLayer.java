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

import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.util.SparseArray;

import org.spongycastle.crypto.InvalidCipherTextException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.meshprovisioner.Provisioner;
import no.nordicsemi.android.meshprovisioner.utils.ExtendedInvalidCipherTextException;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

/**
 * NetworkLayer implementation of the mesh network
 * Do not touch this class, it's public because it has to be
 */
public abstract class NetworkLayer extends LowerTransportLayer {

    private static final String TAG = NetworkLayer.class.getSimpleName();
    NetworkLayerCallbacks mNetworkLayerCallbacks;
    private SparseArray<byte[]> segmentedAccessMessagesMessages;
    private SparseArray<byte[]> segmentedControlMessagesMessages;

    /**
     * Creates a mesh message
     *
     * @param message Message could be of type access or control message.
     */
    protected final void createMeshMessage(final Message message) {
        if (message instanceof AccessMessage) {
            super.createMeshMessage(message);
        } else {
            super.createMeshMessage(message);
        }
        createNetworkLayerPDU(message);
    }

    /**
     * Creates a vendor model mesh message
     *
     * @param message Message could be of type access or control message.
     */
    protected final void createVendorMeshMessage(final Message message) {
        if (message instanceof AccessMessage) {
            super.createVendorMeshMessage(message);
        } else {
            super.createVendorMeshMessage(message);
        }
        createNetworkLayerPDU(message);
    }

    @Override
    public final Message createNetworkLayerPDU(final Message message) {
        final SecureUtils.K2Output k2Output = getK2Output();
        final int nid = k2Output.getNid();
        final byte[] encryptionKey = k2Output.getEncryptionKey();
        Log.v(TAG, "Encryption key: " + MeshParserUtils.bytesToHex(encryptionKey, false));

        final byte[] privacyKey = k2Output.getPrivacyKey();
        Log.v(TAG, "Privacy key: " + MeshParserUtils.bytesToHex(privacyKey, false));
        final int ctl = message.getCtl();
        final int ttl = message.getTtl();
        final int ivi = message.getIvIndex()[3] & 0x01; // least significant bit of IV Index
        final byte iviNID = (byte) ((ivi << 7) | nid);
        final byte ctlTTL = (byte) ((ctl << 7) | ttl);

        final int src = message.getSrc();
        final SparseArray<byte[]> lowerTransportPduMap;
        final SparseArray<byte[]> encryptedNetworkPduPayloadMap = new SparseArray<>();
        final List<byte[]> sequenceNumbers = new ArrayList<>();

        final int pduType = message.getPduType();
        switch (message.getPduType()) {
            case MeshManagerApi.PDU_TYPE_NETWORK:
                if (message instanceof AccessMessage) {
                    lowerTransportPduMap = ((AccessMessage) message).getLowerTransportAccessPdu();
                } else {
                    lowerTransportPduMap = ((ControlMessage) message).getLowerTransportControlPdu();
                }
                for (int i = 0; i < lowerTransportPduMap.size(); i++) {
                    final byte[] lowerTransportPdu = lowerTransportPduMap.get(i);
                    if (i != 0) {
                        final int sequenceNumber = incrementSequenceNumber(message.getSrc(), message.getSequenceNumber());
                        final byte[] sequenceNum = MeshParserUtils.getSequenceNumberBytes(sequenceNumber);
                        message.setSequenceNumber(sequenceNum);
                    }
                    sequenceNumbers.add(message.getSequenceNumber());
                    Log.v(TAG, "Sequence Number: " + MeshParserUtils.bytesToHex(sequenceNumbers.get(i), false));
                    final byte[] encryptedPayload = encryptNetworkPduPayload(message, sequenceNumbers.get(i), lowerTransportPdu, encryptionKey);
                    encryptedNetworkPduPayloadMap.put(i, encryptedPayload);
                    Log.v(TAG, "Encrypted Network payload: " + MeshParserUtils.bytesToHex(encryptedPayload, false));
                }
                break;
            case MeshManagerApi.PDU_TYPE_PROXY_CONFIGURATION:
                lowerTransportPduMap = ((ControlMessage) message).getLowerTransportControlPdu();
                for (int i = 0; i < lowerTransportPduMap.size(); i++) {
                    final byte[] lowerTransportPdu = lowerTransportPduMap.get(i);
                    final int sequenceNumber = incrementSequenceNumber(message.getSrc());
                    final byte[] sequenceNum = MeshParserUtils.getSequenceNumberBytes(sequenceNumber);
                    message.setSequenceNumber(sequenceNum);
                    sequenceNumbers.add(message.getSequenceNumber());
                    final byte[] encryptedPayload = encryptProxyConfigurationPduPayload(message, lowerTransportPdu, encryptionKey);
                    encryptedNetworkPduPayloadMap.put(i, encryptedPayload);
                    Log.v(TAG, "Encrypted Network payload: " + MeshParserUtils.bytesToHex(encryptedPayload, false));
                }
                break;
        }

        final SparseArray<byte[]> networkPduMap = new SparseArray<>();
        for (int i = 0; i < encryptedNetworkPduPayloadMap.size(); i++) {
            //Create the privacy random
            final byte[] encryptedPayload = encryptedNetworkPduPayloadMap.get(i);
            final byte[] privacyRandom = createPrivacyRandom(encryptedPayload);
            //Next we create the PECB
            final byte[] pecb = createPECB(message.getIvIndex(), privacyRandom, privacyKey);


            final byte[] header = obfuscateNetworkHeader(ctlTTL, sequenceNumbers.get(i), src, pecb);
            final byte[] networkPdu = ByteBuffer.allocate(1 + 1 + header.length + encryptedPayload.length).order(ByteOrder.BIG_ENDIAN)
                    .put((byte) pduType)
                    .put(iviNID)
                    .put(header)
                    .put(encryptedPayload)
                    .array();
            networkPduMap.put(i, networkPdu);
            message.setNetworkPdu(networkPduMap);
        }


        return message;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    final Message createRetransmitNetworkLayerPDU(final Message message, final int segment) {
        final SecureUtils.K2Output k2Output = getK2Output();
        final int nid = k2Output.getNid();
        final byte[] encryptionKey = k2Output.getEncryptionKey();
        Log.v(TAG, "Encryption key: " + MeshParserUtils.bytesToHex(encryptionKey, false));

        final byte[] privacyKey = k2Output.getPrivacyKey();
        Log.v(TAG, "Privacy key: " + MeshParserUtils.bytesToHex(privacyKey, false));
        final int ctl = message.getCtl();
        final int ttl = message.getTtl();
        final int ivi = message.getIvIndex()[3] & 0x01; // least significant bit of IV Index
        final byte iviNID = (byte) ((ivi << 7) | nid);
        final byte ctlTTL = (byte) ((ctl << 7) | ttl);

        final int src = message.getSrc();
        final SparseArray<byte[]> lowerTransportPduMap;
        if (message instanceof AccessMessage) {
            lowerTransportPduMap = ((AccessMessage) message).getLowerTransportAccessPdu();
        } else {
            lowerTransportPduMap = ((ControlMessage) message).getLowerTransportControlPdu();
        }

        byte[] encryptedNetworkPayload = null;
        final int pduType = message.getPduType();
        switch (message.getPduType()) {
            case MeshManagerApi.PDU_TYPE_NETWORK:
                final byte[] lowerTransportPdu = lowerTransportPduMap.get(segment);
                final int sequenceNumber = incrementSequenceNumber(message.getSrc(), message.getSequenceNumber());
                final byte[] sequenceNum = MeshParserUtils.getSequenceNumberBytes(sequenceNumber);
                message.setSequenceNumber(sequenceNum);

                Log.v(TAG, "Sequence Number: " + MeshParserUtils.bytesToHex(sequenceNum, false));
                encryptedNetworkPayload = encryptNetworkPduPayload(message, sequenceNum, lowerTransportPdu, encryptionKey);
                if (encryptedNetworkPayload == null)
                    return null;
                Log.v(TAG, "Encrypted Network payload: " + MeshParserUtils.bytesToHex(encryptedNetworkPayload, false));
                break;
        }

        final SparseArray<byte[]> networkPduMap = new SparseArray<>();
        final byte[] privacyRandom = createPrivacyRandom(encryptedNetworkPayload);
        //Next we create the PECB
        final byte[] pecb = createPECB(message.getIvIndex(), privacyRandom, privacyKey);

        if (encryptedNetworkPayload != null) {
            final byte[] header = obfuscateNetworkHeader(ctlTTL, message.getSequenceNumber(), src, pecb);
            final byte[] networkPdu = ByteBuffer.allocate(1 + 1 + header.length + encryptedNetworkPayload.length).order(ByteOrder.BIG_ENDIAN)
                    .put((byte) pduType)
                    .put(iviNID)
                    .put(header)
                    .put(encryptedNetworkPayload)
                    .array();
            networkPduMap.put(segment, networkPdu);
            message.setNetworkPdu(networkPduMap);
            return message;
        } else {
            return null;
        }

    }

    /**
     * Encrypts the network payload of a network pdu
     *
     * @param message           mesh message containing network layer pdu
     * @param lowerTransportPdu lower transport pdu to be encrypted
     * @param encryptionKey     key used to encrypt the payload.
     * @return encrypted payload
     */
    private byte[] encryptNetworkPduPayload(final Message message, final byte[] sequenceNumber, final byte[] lowerTransportPdu, final byte[] encryptionKey) {

        final byte ctlTTL = (byte) ((message.getCtl() << 7) | message.getTtl());
        final byte[] networkNonce = createNetworkNonce(ctlTTL, sequenceNumber, message.getSrc(), message.getIvIndex());
        Log.v(TAG, "Network nonce: " + MeshParserUtils.bytesToHex(networkNonce, false));

        final int dst = message.getDst();
        //Adding the destination address on network layer
        final byte[] unencryptedNetworkPayload = ByteBuffer.allocate(2 + lowerTransportPdu.length).order(ByteOrder.BIG_ENDIAN).putShort((short) dst).put(lowerTransportPdu).array();

        //Network layer encryption
        return SecureUtils.encryptCCM(unencryptedNetworkPayload, encryptionKey, networkNonce, SecureUtils.getNetMicLength(message.getCtl()));
    }

    /**
     * Encrypts the network of a proxy configuration pdu.
     *
     * @param message           mesh message containing network layer pdu
     * @param lowerTransportPdu lower transport pdu to be encrypted
     * @param encryptionKey     key used to encrypt the payload
     * @return encrypted payload
     */
    private byte[] encryptProxyConfigurationPduPayload(final Message message, final byte[] lowerTransportPdu, final byte[] encryptionKey) {

        final byte[] proxyNonce = createProxyNonce(message.getSequenceNumber(), message.getSrc(), message.getIvIndex());
        Log.v(TAG, "Proxy nonce: " + MeshParserUtils.bytesToHex(proxyNonce, false));

        final int dst = message.getDst();
        //Adding the destination address on network layer
        final byte[] unencryptedNetworkPayload = ByteBuffer.allocate(2 + lowerTransportPdu.length).order(ByteOrder.BIG_ENDIAN)
                .putShort((short) dst)
                .put(lowerTransportPdu).array();
        //Network layer encryption
        return SecureUtils.encryptCCM(unencryptedNetworkPayload, encryptionKey, proxyNonce, SecureUtils.getNetMicLength(message.getCtl()));
    }

    /**
     * Obfuscates the network header
     *
     * @param ctlTTL         message type and ttl bit
     * @param sequenceNumber sequence number of the message
     * @param src            source address
     * @param pecb           value derived from the privacy random
     * @return obfuscted network header
     */
    private byte[] obfuscateNetworkHeader(final byte ctlTTL, final byte[] sequenceNumber, final int src, final byte[] pecb) {

        final ByteBuffer buffer = ByteBuffer.allocate(1 + sequenceNumber.length + 2).order(ByteOrder.BIG_ENDIAN);
        buffer.put(ctlTTL);
        buffer.put(sequenceNumber);   //sequence number
        buffer.putShort((short) src);       //source address

        final byte[] headerBuffer = buffer.array();

        final ByteBuffer bufferPECB = ByteBuffer.allocate(6);
        bufferPECB.put(pecb, 0, 6);

        final byte[] obfuscated = new byte[6];
        for (int i = 0; i < 6; i++)
            obfuscated[i] = (byte) (headerBuffer[i] ^ pecb[i]);

        return obfuscated;
    }

    /**
     * De-obfuscates the network header
     *
     * @param pdu received from the node
     * @return obfuscted network header
     */
    private byte[] deobfuscateNetworkHeader(final byte[] pdu) {
        final byte[] privacyKey = getK2Output().getPrivacyKey();
        final ByteBuffer obfuscatedNetworkBuffer = ByteBuffer.allocate(6);
        obfuscatedNetworkBuffer.order(ByteOrder.BIG_ENDIAN);
        obfuscatedNetworkBuffer.put(pdu, 2, 6);
        final byte[] obfuscatedData = obfuscatedNetworkBuffer.array();

        final ByteBuffer privacyRandomBuffer = ByteBuffer.allocate(7);
        privacyRandomBuffer.order(ByteOrder.BIG_ENDIAN);
        privacyRandomBuffer.put(pdu, 8, 7);
        final byte[] privacyRandom = createPrivacyRandom(privacyRandomBuffer.array());

        final byte[] pecb = createPECB(privacyRandom, privacyKey);
        final byte[] deobfuscatedData = new byte[6];

        for (int i = 0; i < 6; i++)
            deobfuscatedData[i] = (byte) (obfuscatedData[i] ^ pecb[i]);

        return deobfuscatedData;
    }

    /**
     * Creates the network nonce
     *
     * @param ctlTTL         combined ctl and ttl value
     * @param sequenceNumber sequence number of the message
     * @param src            source address
     * @return Network nonce
     */
    private byte[] createNetworkNonce(final byte ctlTTL, final byte[] sequenceNumber, final int src, final byte[] ivIndex) {
        final ByteBuffer networkNonce = ByteBuffer.allocate(13);
        networkNonce.put((byte) NONCE_TYPE_NETWORK); //Nonce type
        networkNonce.put(ctlTTL); // CTL and TTL
        networkNonce.put(sequenceNumber);
        networkNonce.putShort((short) src);
        networkNonce.put(new byte[]{PAD_NETWORK_NONCE, PAD_NETWORK_NONCE}); //PAD
        networkNonce.put(ivIndex);
        return networkNonce.array();
    }

    /**
     * Creates the proxy nonce
     *
     * @param sequenceNumber sequence number of the message
     * @param src            source address
     * @return Proxy nonce
     */
    private byte[] createProxyNonce(final byte[] sequenceNumber, final int src, final byte[] ivIndex) {
        final ByteBuffer applicationNonceBuffer = ByteBuffer.allocate(13);
        applicationNonceBuffer.put((byte) NONCE_TYPE_PROXY); //Nonce type
        applicationNonceBuffer.put((byte) PAD_PROXY_NONCE); //PAD
        applicationNonceBuffer.put(sequenceNumber);
        applicationNonceBuffer.putShort((short) src);
        applicationNonceBuffer.put(new byte[]{PAD_PROXY_NONCE, PAD_PROXY_NONCE});
        applicationNonceBuffer.put(ivIndex);
        return applicationNonceBuffer.array();
    }

    /**
     * Creates the privacy random.
     *
     * @param encryptedUpperTransportPDU encrypted transport pdu
     * @return Privacy random
     */
    private byte[] createPrivacyRandom(final byte[] encryptedUpperTransportPDU) {
        final byte[] privacyRandom = new byte[7];
        System.arraycopy(encryptedUpperTransportPDU, 0, privacyRandom, 0, privacyRandom.length);
        return privacyRandom;
    }

    private byte[] createPECB(final byte[] privacyRandom, final byte[] privacyKey) {
        final byte[] ivIndex = mUpperTransportLayerCallbacks.getIvIndex();
        final ByteBuffer buffer = ByteBuffer.allocate(5 + privacyRandom.length + ivIndex.length);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.put(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00});
        buffer.put(ivIndex);
        buffer.put(privacyRandom);
        final byte[] temp = buffer.array();
        Log.v(TAG, "Privacy Random: " + MeshParserUtils.bytesToHex(temp, false));
        return SecureUtils.encryptWithAES(temp, privacyKey);
    }

    private byte[] createPECB(final byte[] ivIndex, final byte[] privacyRandom, final byte[] privacyKey) {
        final ByteBuffer buffer = ByteBuffer.allocate(5 + privacyRandom.length + ivIndex.length);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.put(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00});
        buffer.put(ivIndex);
        buffer.put(privacyRandom);
        final byte[] temp = buffer.array();
        return SecureUtils.encryptWithAES(temp, privacyKey);
    }

    /**
     * Parse received mesh message
     * <p>
     * This method will drop messages with an invalid sequence number as all mesh messages are supposed to have a sequence
     * </p>
     *
     * @param data pdu received from the mesh node
     * @return complete {@link Message} that was successfully parsed or null otherwise
     */
    final Message parseMeshMessage(final byte[] data) throws ExtendedInvalidCipherTextException {
        final Provisioner provisioner = mNetworkLayerCallbacks.getProvisioner();

        //D-eobfuscate network header
        final byte[] networkHeader = deobfuscateNetworkHeader(data);
        final int ctlTtl = networkHeader[0];
        final int ctl = (ctlTtl >> 7) & 0x01;
        final int ttl = ctlTtl & 0x7F;
        Log.v(TAG, "TTL for received message: " + ttl);

        final int micLength = SecureUtils.getNetMicLength(ctl);
        final byte[] sequenceNumber = ByteBuffer.allocate(3).order(ByteOrder.BIG_ENDIAN).put(networkHeader, 1, 3).array();
        final int src = MeshParserUtils.unsignedBytesToInt(networkHeader[5], networkHeader[4]);
        if (mMeshNode == null || mMeshNode.getUnicastAddress() != src) {
            mMeshNode = mNetworkLayerCallbacks.getProvisionedNode(src);
            if (mMeshNode == null) {
                return null;
            }
        }

        //Check if the sequence number has been incremented since the last message sent and return null if not
        final int sequenceNo = MeshParserUtils.getSequenceNumber(sequenceNumber);
        Log.v(TAG, "Sequence number of received access message: " + MeshParserUtils.getSequenceNumber(sequenceNumber));
        if (sequenceNo > mMeshNode.getReceivedSequenceNumber()) {
            if (!MeshParserUtils.isValidSequenceNumber(sequenceNo)) {
                return null;
            }
            mMeshNode.setReceivedSequenceNumber(sequenceNo);
        }

        final byte[] nonce;

        final byte[] ivIndex = mUpperTransportLayerCallbacks.getIvIndex();
        switch (data[0]) {
            case MeshManagerApi.PDU_TYPE_NETWORK:
                nonce = createNetworkNonce((byte) ctlTtl, sequenceNumber, src, ivIndex);
                break;
            case MeshManagerApi.PDU_TYPE_PROXY_CONFIGURATION:
                nonce = createProxyNonce(sequenceNumber, src, ivIndex);
                break;
            default:
                return null;

        }

        if (ctl == 1) {
            return parseControlMessage(provisioner.getProvisionerAddress(), data, networkHeader, nonce, src, sequenceNumber, micLength);
        } else {
            return parseAccessMessage(data, networkHeader, nonce, src, sequenceNumber, micLength);
        }
    }

    /**
     * Parses access message
     *
     * @param data           received from the node
     * @param networkHeader  de-obfuscated network header
     * @param networkNonce   network nonce
     * @param src            source address
     * @param sequenceNumber sequence number of the received message
     * @param micLength      network mic length of the received message
     * @return access message
     */
    @VisibleForTesting
    private AccessMessage parseAccessMessage(final byte[] data,
                                             final byte[] networkHeader,
                                             final byte[] networkNonce,
                                             final int src,
                                             final byte[] sequenceNumber,
                                             final int micLength) throws ExtendedInvalidCipherTextException {
        try {

            final SecureUtils.K2Output k2Output = getK2Output();

            final byte[] encryptionKey = k2Output.getEncryptionKey();

            final int ttl = networkHeader[0] & 0x7F;

            final int networkPayloadLength = data.length - (2 + networkHeader.length);
            final byte[] transportPdu = new byte[networkPayloadLength];
            System.arraycopy(data, 8, transportPdu, 0, networkPayloadLength);
            final byte[] decryptedNetworkPayload;
            try {
                decryptedNetworkPayload = SecureUtils.decryptCCM(transportPdu, encryptionKey, networkNonce, micLength);
            } catch (InvalidCipherTextException ex) {
                throw new ExtendedInvalidCipherTextException(ex.getMessage(), ex.getCause(), TAG);
            }
            final int dst = MeshParserUtils.unsignedBytesToInt(decryptedNetworkPayload[1], decryptedNetworkPayload[0]);

            Log.v(TAG, "Dst: " + MeshAddress.formatAddress(dst, true));

            if (isSegmentedMessage(decryptedNetworkPayload[2])) {
                Log.v(TAG, "Received a segmented access message from: " + MeshAddress.formatAddress(src, false));

                //Check if the received segmented message is from the same src as the previous segment
                if (src != mMeshNode.getUnicastAddress()) {
                    Log.v(TAG, "Segment received is from a different src than the one we are processing, let's drop it");
                    return null;
                }

                if (segmentedAccessMessagesMessages == null) {
                    segmentedAccessMessagesMessages = new SparseArray<>();
                    segmentedAccessMessagesMessages.put(0, data);
                } else {
                    final int k = segmentedAccessMessagesMessages.size();
                    segmentedAccessMessagesMessages.put(k, data);
                }
                //Removing the mDst here
                final byte[] pdu = ByteBuffer.allocate(2 + networkHeader.length + decryptedNetworkPayload.length)
                        .order(ByteOrder.BIG_ENDIAN)
                        .put(data, 0, 2)
                        .put(networkHeader)
                        .put(decryptedNetworkPayload)
                        .array();
                final AccessMessage message = parseSegmentedAccessLowerTransportPDU(pdu);
                if (message != null) {
                    final SparseArray<byte[]> segmentedMessages = segmentedAccessMessagesMessages.clone();
                    segmentedAccessMessagesMessages = null;
                    message.setIvIndex(mUpperTransportLayerCallbacks.getIvIndex());
                    message.setNetworkPdu(segmentedMessages);
                    message.setTtl(ttl);
                    message.setSrc(src);
                    message.setDst(dst);

                    parseUpperTransportPDU(message);
                    parseAccessLayerPDU(message);
                }
                return message;

            } else {
                final AccessMessage message = new AccessMessage();
                message.setIvIndex(mUpperTransportLayerCallbacks.getIvIndex());
                final SparseArray<byte[]> networkPduMap = new SparseArray<>();
                networkPduMap.put(0, data);
                message.setNetworkPdu(networkPduMap);
                message.setTtl(ttl);
                message.setSrc(src);
                message.setDst(dst);
                message.setSequenceNumber(sequenceNumber);

                //Removing the mDst here
                final byte[] pdu = ByteBuffer.allocate(2 + networkHeader.length + decryptedNetworkPayload.length)
                        .order(ByteOrder.BIG_ENDIAN)
                        .put(data, 0, 2)
                        .put(networkHeader)
                        .put(decryptedNetworkPayload)
                        .array();
                parseUnsegmentedAccessLowerTransportPDU(message, pdu);
                parseUpperTransportPDU(message);
                parseAccessLayerPDU(message);

                return message;
            }
        } catch (InvalidCipherTextException ex) {
            throw new ExtendedInvalidCipherTextException(ex.getMessage(), ex.getCause(), TAG);
        }
    }

    /**
     * Parses control message
     *
     * @param provisionerAddress Provisioner address
     * @param data               Data received from the node
     * @param networkHeader      De-obfuscated network header
     * @param nonce              Nonce depending on the pdu type
     * @param src                Source address where the pdu originated from
     * @param sequenceNumber     Sequence number of the received message
     * @param micLength          Network mic length of the received message
     * @return a complete {@link ControlMessage} or null if the message was unable to parsed
     */
    private ControlMessage parseControlMessage(final int provisionerAddress,
                                               final byte[] data,
                                               final byte[] networkHeader,
                                               final byte[] nonce,
                                               final int src,
                                               final byte[] sequenceNumber,
                                               final int micLength) throws ExtendedInvalidCipherTextException {
        try {
            final SecureUtils.K2Output k2Output = getK2Output();
            final byte[] encryptionKey = k2Output.getEncryptionKey();
            final int ttl = networkHeader[0] & 0x7F;
            final int networkPayloadLength = data.length - (2 + networkHeader.length);
            final byte[] transportPdu = new byte[networkPayloadLength];
            System.arraycopy(data, 8, transportPdu, 0, networkPayloadLength);
            final byte[] decryptedNetworkPayload;
            try {
                decryptedNetworkPayload = SecureUtils.decryptCCM(transportPdu, encryptionKey, nonce, micLength);
            } catch (InvalidCipherTextException ex) {
                throw new ExtendedInvalidCipherTextException(ex.getMessage(), ex.getCause(), TAG);
            }
            final int dst = MeshParserUtils.unsignedBytesToInt(decryptedNetworkPayload[1], decryptedNetworkPayload[0]);

            //Removing the mDst here
            final byte[] decryptedProxyPdu = ByteBuffer.allocate(2 + networkHeader.length + decryptedNetworkPayload.length)
                    .order(ByteOrder.BIG_ENDIAN)
                    .put(data, 0, 2)
                    .put(networkHeader)
                    .put(decryptedNetworkPayload)
                    .array();

            //We check the pdu type
            final int pduType = data[0];
            switch (pduType) {
                case MeshManagerApi.PDU_TYPE_NETWORK:
                    //Check if the message is directed to us, if its not ignore the message
                    if (provisionerAddress == dst) {
                        Log.v(TAG, "Received a control message that was not directed to us, so we drop it");
                        return null;
                    }

                    if (isSegmentedMessage(decryptedNetworkPayload[2])) {
                        return parseSegmentedControlMessage(data, decryptedProxyPdu, ttl, src, dst);
                    } else {
                        return parseUnsegmentedControlMessage(data, decryptedProxyPdu, ttl, src, dst, sequenceNumber);
                    }
                case MeshManagerApi.PDU_TYPE_PROXY_CONFIGURATION:
                    //Proxy configuration messages are segmented only at the gatt level
                    return parseUnsegmentedControlMessage(data, decryptedProxyPdu, ttl, src, dst, sequenceNumber);
                default:
                    return null;
            }
        } catch (InvalidCipherTextException ex) {
            throw new ExtendedInvalidCipherTextException(ex.getMessage(), ex.getCause(), TAG);
        }
    }

    /**
     * Parses an unsegmented control message
     *
     * @param data              Received pdu data
     * @param decryptedProxyPdu Decrypted proxy pdu
     * @param ttl               TTL of the pdu
     * @param src               Source address where the pdu originated from
     * @param dst               Destination address to which the pdu was sent
     * @param sequenceNumber    Sequence number of the pdu
     * @return a complete {@link ControlMessage} or null if the message was unable to parsed
     */
    private ControlMessage parseUnsegmentedControlMessage(final byte[] data,
                                                          final byte[] decryptedProxyPdu,
                                                          final int ttl,
                                                          final int src,
                                                          final int dst,
                                                          final byte[] sequenceNumber) throws ExtendedInvalidCipherTextException {
        final ControlMessage message = new ControlMessage();
        message.setIvIndex(mUpperTransportLayerCallbacks.getIvIndex());
        final SparseArray<byte[]> proxyPduArray = new SparseArray<>();
        proxyPduArray.put(0, data);
        message.setNetworkPdu(proxyPduArray);
        message.setTtl(ttl);
        message.setSrc(src);
        message.setDst(dst);
        message.setSequenceNumber(sequenceNumber);
        message.setSegmented(false);
        parseUnsegmentedControlLowerTransportPDU(message, decryptedProxyPdu);

        return message;
    }

    /**
     * Parses a unsegmented control message
     *
     * @param data              Received pdu data
     * @param decryptedProxyPdu Decrypted proxy pdu
     * @param ttl               TTL of the pdu
     * @param src               Source address where the pdu originated from
     * @param dst               Destination address to which the pdu was sent
     * @return a complete {@link ControlMessage} or null if the message was unable to parsed
     */
    private ControlMessage parseSegmentedControlMessage(final byte[] data, final byte[] decryptedProxyPdu, final int ttl, final int src, final int dst) {
        if (segmentedControlMessagesMessages == null) {
            segmentedControlMessagesMessages = new SparseArray<>();
            segmentedControlMessagesMessages.put(0, data);
        } else {
            final int k = segmentedControlMessagesMessages.size();
            segmentedAccessMessagesMessages.put(k, data);
        }

        final ControlMessage message = parseSegmentedControlLowerTransportPDU(decryptedProxyPdu);
        if (message != null) {
            final SparseArray<byte[]> segmentedMessages = segmentedControlMessagesMessages.clone();
            segmentedControlMessagesMessages = null;
            message.setIvIndex(mUpperTransportLayerCallbacks.getIvIndex());
            message.setNetworkPdu(segmentedMessages);
            message.setTtl(ttl);
            message.setSrc(src);
            message.setDst(dst);
        }
        return message;
    }

    /**
     * Returns the master credentials {@link SecureUtils.K2Output}
     */
    private SecureUtils.K2Output getK2Output() {
        final NetworkKey networkKey = mNetworkLayerCallbacks.getPrimaryNetworkKey();
        return SecureUtils.calculateK2(networkKey.getKey(), SecureUtils.K2_MASTER_INPUT);
    }
}
