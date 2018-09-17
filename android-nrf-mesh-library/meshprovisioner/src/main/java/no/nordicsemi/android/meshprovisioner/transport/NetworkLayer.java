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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nordicsemi.android.meshprovisioner.messages.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messages.ControlMessage;
import no.nordicsemi.android.meshprovisioner.messages.Message;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

public abstract class NetworkLayer extends LowerTransportLayer {

    protected static final int MESH_BEACON_PDU = 0x01;
    private static final int PROXY_CONFIGURATION_PDU = 0x02;
    private static final String TAG = NetworkLayer.class.getSimpleName();
    private byte[] mEncryptionKey;
    private byte[] mPrivacyKey;
    private int key;
    private HashMap<Integer, byte[]> segmentedAccessMessagesMessages;
    private HashMap<Integer, byte[]> segmentedControlMessagesMessages;
    private byte[] mSrc;

    /**
     * Creates a mesh message
     * @param message Message could be of type access or control message.
     */
    protected final void createMeshMessage(final Message message) {
        if(message instanceof AccessMessage) {
            super.createMeshMessage(message);
        } else {
            super.createMeshMessage(message);
        }
        createNetworkLayerPDU(message);
    }

    /**
     * Creates a vendor model mesh message
     * @param message Message could be of type access or control message.
     */
    protected final void createVendorMeshMessage(final Message message) {
        if(message instanceof AccessMessage) {
            super.createVendorMeshMessage(message);
        } else {
            super.createVendorMeshMessage(message);
        }
        createNetworkLayerPDU(message);
    }

    @Override
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public final Message createNetworkLayerPDU(final Message message) {
        final SecureUtils.K2Output k2Output = mMeshNode.getK2Output();
        final int nid = k2Output.getNid();
        final byte[] encryptionKey = mEncryptionKey = k2Output.getEncryptionKey();
        Log.v(TAG, "Encryption key: " + MeshParserUtils.bytesToHex(encryptionKey, false));

        final byte[] privacyKey = mPrivacyKey = k2Output.getPrivacyKey();
        Log.v(TAG, "Privacy key: " + MeshParserUtils.bytesToHex(privacyKey, false));
        final int ctl = message.getCtl();
        final int ttl = message.getTtl();
        final int ivi = message.getIvIndex()[3] & 0x01; // least significant bit of IV Index
        final byte iviNID = (byte) ((ivi << 7) | nid);
        final byte ctlTTL = (byte) ((ctl << 7) | ttl);

        final byte[] src = message.getSrc();
        final Map<Integer, byte[]> lowerTransportPduMap;
        if (ctl == 0) {
            lowerTransportPduMap = message.getLowerTransportAccessPdu();
        } else {
            lowerTransportPduMap = message.getLowerTransportControlPdu();
        }

        final HashMap<Integer, byte[]> encryptedNetworkPduPayloadMap = new HashMap<>();
        final List<byte[]> sequenceNumbers = new ArrayList<>();

        final int pduType = message.getPduType();
        switch (message.getPduType()) {
            case NETWORK_PDU:
                for (int i = 0; i < lowerTransportPduMap.size(); i++) {
                    final byte[] lowerTransportPdu = lowerTransportPduMap.get(i);
                    if (i != 0) {
                        final int sequenceNumber = incrementSequenceNumber(message.getSequenceNumber());
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
            case PROXY_CONFIGURATION_PDU:
                for (int i = 0; i < lowerTransportPduMap.size(); i++) {
                    final byte[] lowerTransportPdu = lowerTransportPduMap.get(i);
                    final int sequenceNumber = incrementSequenceNumber();
                    final byte[] sequenceNum = MeshParserUtils.getSequenceNumberBytes(sequenceNumber);
                    message.setSequenceNumber(sequenceNum);
                    final byte[] encryptedPayload = encryptProxyConfigurationPduPayload(message, lowerTransportPdu, encryptionKey);
                    encryptedNetworkPduPayloadMap.put(i, encryptedPayload);
                    Log.v(TAG, "Encrypted Network payload: " + MeshParserUtils.bytesToHex(encryptedPayload, false));
                }
                break;
        }

        final HashMap<Integer, byte[]> networkPduMap = new HashMap<>();
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
    public final Message createRetransmitNetworkLayerPDU(final Message message, final int segment) {
        final SecureUtils.K2Output k2Output = mMeshNode.getK2Output();
        final int nid = k2Output.getNid();
        final byte[] encryptionKey = mEncryptionKey = k2Output.getEncryptionKey();
        Log.v(TAG, "Encryption key: " + MeshParserUtils.bytesToHex(encryptionKey, false));

        final byte[] privacyKey = mPrivacyKey = k2Output.getPrivacyKey();
        Log.v(TAG, "Privacy key: " + MeshParserUtils.bytesToHex(privacyKey, false));
        final int ctl = message.getCtl();
        final int ttl = message.getTtl();
        final int ivi = message.getIvIndex()[3] & 0x01; // least significant bit of IV Index
        final byte iviNID = (byte) ((ivi << 7) | nid);
        final byte ctlTTL = (byte) ((ctl << 7) | ttl);

        final byte[] src = message.getSrc();
        final Map<Integer, byte[]> lowerTransportPduMap;
        if (ctl == 0) {
            lowerTransportPduMap = message.getLowerTransportAccessPdu();
        } else {
            lowerTransportPduMap = message.getLowerTransportControlPdu();
        }

        byte[] encryptedNetworkPayload = null;
        final int pduType = message.getPduType();
        switch (message.getPduType()) {
            case NETWORK_PDU:
                final byte[] lowerTransportPdu = lowerTransportPduMap.get(segment);
                final int sequenceNumber = incrementSequenceNumber(message.getSequenceNumber());
                final byte[] sequenceNum = MeshParserUtils.getSequenceNumberBytes(sequenceNumber);
                message.setSequenceNumber(sequenceNum);

                Log.v(TAG, "Sequence Number: " + MeshParserUtils.bytesToHex(sequenceNum, false));
                encryptedNetworkPayload = encryptNetworkPduPayload(message, sequenceNum, lowerTransportPdu, encryptionKey);
                Log.v(TAG, "Encrypted Network payload: " + MeshParserUtils.bytesToHex(encryptedNetworkPayload, false));
                break;
            case PROXY_CONFIGURATION_PDU:
                break;
        }

        final HashMap<Integer, byte[]> networkPduMap = new HashMap<>();
        final byte[] privacyRandom = createPrivacyRandom(encryptedNetworkPayload);
        //Next we create the PECB
        final byte[] pecb = createPECB(message.getIvIndex(), privacyRandom, privacyKey);


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
    }

    /**
     * Encrypts the network payload of a network pdu
     *
     * @param message mesh message containing network layer pdu
     * @param lowerTransportPdu lower transport pdu to be encrypted
     * @param encryptionKey     key used to encrypt the payload.
     * @return encrypted payload
     */
    private byte[] encryptNetworkPduPayload(final Message message, final byte[] sequenceNumber, final byte[] lowerTransportPdu, final byte[] encryptionKey) {

        final byte ctlTTL = (byte) ((message.getCtl() << 7) | message.getTtl());
        final byte[] networkNonce = createNetworkNonce(ctlTTL, sequenceNumber, message.getSrc(), message.getIvIndex());
        Log.v(TAG, "Network nonce: " + MeshParserUtils.bytesToHex(networkNonce, false));

        final byte[] dst = message.getDst();
        //Adding the destination address on network layer
        final byte[] unencryptedNetworkPayload = ByteBuffer.allocate(dst.length + lowerTransportPdu.length).order(ByteOrder.BIG_ENDIAN).put(dst).put(lowerTransportPdu).array();

        //Network layer encryption
        return SecureUtils.encryptCCM(unencryptedNetworkPayload, encryptionKey, networkNonce, SecureUtils.getNetMicLength(message.getCtl()));
    }

    /**
     * Encrypts the network of a proxy configuration pdu.
     *
     * @param message mesh message containing network layer pdu
     * @param lowerTransportPdu lower transport pdu to be encrypted
     * @param encryptionKey     key used to encrypt the payload
     * @return encrypted payload
     */
    private byte[] encryptProxyConfigurationPduPayload(final Message message, final byte[] lowerTransportPdu, final byte[] encryptionKey) {

        final byte[] proxyNonce = createProxyNonce(message.getSequenceNumber(), message.getSrc(), message.getIvIndex());
        Log.v(TAG, "Proxy nonce: " + MeshParserUtils.bytesToHex(proxyNonce, false));

        final byte[] dst = message.getDst();
        //Adding the destination address on network layer
        final byte[] unencryptedNetworkPayload = ByteBuffer.allocate(dst.length + lowerTransportPdu.length).order(ByteOrder.BIG_ENDIAN).put(dst).put(lowerTransportPdu).array();
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
    private byte[] obfuscateNetworkHeader(final byte ctlTTL, final byte[] sequenceNumber, final byte[] src, final byte[] pecb) {

        final ByteBuffer buffer = ByteBuffer.allocate(1 + sequenceNumber.length + src.length).order(ByteOrder.BIG_ENDIAN);
        buffer.put(ctlTTL);
        buffer.put(sequenceNumber);   //sequence number
        buffer.put(src);       //source address

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
        final byte[] privacyKey = mMeshNode.getK2Output().getPrivacyKey();
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
    private byte[] createNetworkNonce(final byte ctlTTL, final byte[] sequenceNumber, final byte[] src, final byte[] ivIndex) {
        final ByteBuffer networkNonce = ByteBuffer.allocate(13);
        networkNonce.put((byte) NONCE_TYPE_NETWORK); //Nonce type
        networkNonce.put(ctlTTL); // CTL and TTL
        networkNonce.put(sequenceNumber);
        networkNonce.put(src);
        networkNonce.put(new byte[]{PAD_NETWORK_NONCE, PAD_NETWORK_NONCE}); //PAD
        networkNonce.put(ivIndex);
        return networkNonce.array();
    }

    private byte[] createNetworkNonce(final byte ctlTTL, final byte[] sequenceNumber, final byte[] srcAddress) {
        final ByteBuffer networkNonce = ByteBuffer.allocate(13);
        networkNonce.put((byte) 0x00); //Nonce type
        networkNonce.put(ctlTTL); // CTL and TTL
        networkNonce.put(sequenceNumber);
        networkNonce.put(srcAddress);
        networkNonce.put(new byte[]{0x00, 0x00}); //PAD
        networkNonce.put(mMeshNode.getIvIndex());
        return networkNonce.array();
    }

    /**
     * Creates the proxy nonce
     *
     * @param sequenceNumber sequence number of the message
     * @param src            source address
     * @return Proxy nonce
     */
    private byte[] createProxyNonce(final byte[] sequenceNumber, final byte[] src, final byte[] ivIndex) {
        final ByteBuffer applicationNonceBuffer = ByteBuffer.allocate(13);
        applicationNonceBuffer.put((byte) NONCE_TYPE_PROXY); //Nonce type
        applicationNonceBuffer.put((byte) PAD_PROXY_NONCE); //PAD
        applicationNonceBuffer.put(sequenceNumber);
        applicationNonceBuffer.put(src);
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
        final byte[] ivIndex = mMeshNode.getIvIndex();
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
     * @param configurationSrc source address of the configurator
     * @param data             pdu received from the mesh node
     * @return complete {@link Message} that was successfully parsed or null otherwise
     */
    protected final Message parseMeshMessage(final byte[] configurationSrc, final byte[] data) {
        final SecureUtils.K2Output k2Output = mMeshNode.getK2Output();
        mEncryptionKey = k2Output.getEncryptionKey();
        mPrivacyKey = k2Output.getPrivacyKey();

        //D-eobfuscate network header
        final byte[] networkHeader = deobfuscateNetworkHeader(data);
        final int ctlTtl = networkHeader[0];
        final int ctl = (ctlTtl >> 7) & 0x01;
        final int ttl = ctlTtl & 0x7F;
        Log.v(TAG, "TTL for received message: " + ttl);

        final int micLength = SecureUtils.getNetMicLength(ctl);
        final byte[] sequenceNumber = ByteBuffer.allocate(3).order(ByteOrder.BIG_ENDIAN).put(networkHeader, 1, 3).array();
        final byte[] src = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).put(networkHeader, 4, 2).array();
        final byte[] networkNonce = createNetworkNonce((byte) ctlTtl, sequenceNumber, src);

        //Check if the sequence number has been incremented since the last message sent and return null if not
        final int sequenceNo = MeshParserUtils.getSequenceNumber(sequenceNumber);
        Log.v(TAG, "Sequence number of received access message: " + MeshParserUtils.getSequenceNumber(sequenceNumber));
        if (sequenceNo > mMeshNode.getSequenceNumber()) {
            if (!MeshParserUtils.isValidSequenceNumber(sequenceNo)) {
                return null;
            }
            mMeshNode.setSequenceNumber(sequenceNo);
        }

        if (ctl == 1) {
            return parseControlMessage(configurationSrc, data, networkHeader, networkNonce, src, sequenceNumber, micLength);
        } else {
            return parseAccessMessage(configurationSrc, data, networkHeader, networkNonce, src, sequenceNumber, micLength);
        }
    }

    @VisibleForTesting
    protected final Message parseMeshMessage(final byte[] data) {
        final SecureUtils.K2Output k2Output = mMeshNode.getK2Output();

        final byte[] encryptionKey = mEncryptionKey = k2Output.getEncryptionKey();
        final byte[] privacyKey = mPrivacyKey = k2Output.getPrivacyKey();

        //D-eobfuscate network header
        final byte[] networkHeader = deobfuscateNetworkHeader(data);
        final int ctlTtl = networkHeader[0];
        final int ctl = (ctlTtl >> 7) & 0x01;
        final int ttl = ctlTtl & 0x7F;
        Log.v(TAG, "TTL for received message: " + ttl);

        final int micLength = SecureUtils.getNetMicLength(ctl);
        final byte[] sequenceNumber = ByteBuffer.allocate(3).order(ByteOrder.BIG_ENDIAN).put(networkHeader, 1, 3).array();
        final byte[] src = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).put(networkHeader, 4, 2).array();
        final byte[] networkNonce = createNetworkNonce((byte) ctlTtl, sequenceNumber, src);

        if (ctl == 1) {
            return parseControlMessage(data, networkHeader, networkNonce, src, sequenceNumber, micLength);
        } else {
            Log.v(TAG, "Sequence number of received access message: " + MeshParserUtils.getSequenceNumber(sequenceNumber));
            return parseAccessMessage(data, networkHeader, networkNonce, src, sequenceNumber, micLength);
        }
    }

    /**
     * Parses access message
     *
     * @param configurationSrc source address of the configurator
     * @param data             receieved from the node
     * @param networkHeader    de-obfuscated network header
     * @param networkNonce     network nonce
     * @param src              source address
     * @param sequenceNumber   sequence number of the received message
     * @param micLength        network mic length of the received message
     * @return access message
     */
    private AccessMessage parseAccessMessage(final byte[] configurationSrc, final byte[] data, final byte[] networkHeader, final byte[] networkNonce, final byte[] src, final byte[] sequenceNumber, final int micLength) {
        final SecureUtils.K2Output k2Output = mMeshNode.getK2Output();

        final byte[] encryptionKey = mEncryptionKey = k2Output.getEncryptionKey();
        mPrivacyKey = k2Output.getPrivacyKey();

        final int ttl = networkHeader[0] & 0x7F;

        final int networkPayloadLength = data.length - (2 + networkHeader.length);
        final byte[] transportPdu = new byte[networkPayloadLength];
        System.arraycopy(data, 8, transportPdu, 0, networkPayloadLength);
        final byte[] decryptedNetworkPayload = SecureUtils.decryptCCM(transportPdu, encryptionKey, networkNonce, micLength);
        final byte[] dst = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).put(decryptedNetworkPayload, 0, 2).array();

        //Check if the message is directed to us, if its not ignore the message
        if (!Arrays.equals(configurationSrc, dst)) {
            Log.v(TAG, "Received an access message that was not directed to us, let's drop it");
            return null;
        }

        if (isSegmentedMessage(decryptedNetworkPayload[2])) {
            Log.v(TAG, "Received a segmented access message from: " + MeshParserUtils.bytesToHex(src, false));

            //Store the src address of the received message
            //This is to ignore messages from a different source address while processing segmented messages.
            if(mSrc == null) {
                mSrc = src;
            }

            //Check if the received segmented message is from the same src as the previous segment
            if (!Arrays.equals(src, mSrc)) {
                Log.v(TAG, "Segment received is from a different src than the one we are processing, let's drop it");
                return null;
            }

            if (segmentedAccessMessagesMessages == null) {
                segmentedAccessMessagesMessages = new HashMap<>();
                segmentedAccessMessagesMessages.put(0, data);
            } else {
                final int k = segmentedAccessMessagesMessages.size();
                segmentedAccessMessagesMessages.put(k, data);
            }
            //Removing the dst here
            final byte[] pdu = ByteBuffer.allocate(2 + networkHeader.length + decryptedNetworkPayload.length).order(ByteOrder.BIG_ENDIAN).put(data, 0, 2).put(networkHeader).put(decryptedNetworkPayload).array();
            final AccessMessage message = parseSegmentedAccessLowerTransportPDU(pdu);
            if (message != null) {
                //The segmented message is complete, lets clear the stored src address
                mSrc = null;

                final HashMap<Integer, byte[]> segmentedMessages = segmentedAccessMessagesMessages;
                segmentedAccessMessagesMessages = null;
                key = 0;
                message.setIvIndex(mMeshNode.getIvIndex());
                message.setNetworkPdu(segmentedMessages);
                message.setCtl(0);
                message.setTtl(ttl);
                message.setSrc(src);
                message.setDst(dst);

                parseUpperTransportPDU(message);
                parseAccessLayerPDU(message);
            }
            return message;

        } else {
            final AccessMessage message = new AccessMessage();
            message.setIvIndex(mMeshNode.getIvIndex());
            final HashMap<Integer, byte[]> networkPduMap = new HashMap<>();
            networkPduMap.put(0, data);
            message.setNetworkPdu(networkPduMap);
            message.setTtl(ttl);
            message.setSrc(src);
            message.setDst(dst);
            message.setSequenceNumber(sequenceNumber);

            //Removing the dst here
            final byte[] pdu = ByteBuffer.allocate(2 + networkHeader.length + decryptedNetworkPayload.length).order(ByteOrder.BIG_ENDIAN).put(data, 0, 2).put(networkHeader).put(decryptedNetworkPayload).array();
            parseUnsegmentedAccessLowerTransportPDU(message, pdu);
            parseUpperTransportPDU(message);
            parseAccessLayerPDU(message);

            return message;
        }
    }

    /**
     * Parses access message
     *
     * @param data           received from the node
     * @param networkHeader  deobfuscated network header
     * @param networkNonce   network nonce
     * @param src            source address
     * @param sequenceNumber sequence number of the received message
     * @param micLength      network mic length of the received message
     * @return access message
     */
    @VisibleForTesting
    private AccessMessage parseAccessMessage(final byte[] data, final byte[] networkHeader, final byte[] networkNonce, final byte[] src, final byte[] sequenceNumber, final int micLength) {
        final SecureUtils.K2Output k2Output = mMeshNode.getK2Output();

        final byte[] encryptionKey = mEncryptionKey = k2Output.getEncryptionKey();
        mPrivacyKey = k2Output.getPrivacyKey();

        final int ttl = networkHeader[0] & 0x7F;

        final int networkPayloadLength = data.length - (2 + networkHeader.length);
        final byte[] transportPdu = new byte[networkPayloadLength];
        System.arraycopy(data, 8, transportPdu, 0, networkPayloadLength);
        final byte[] decryptedNetworkPayload = SecureUtils.decryptCCM(transportPdu, encryptionKey, networkNonce, micLength);
        final byte[] dst = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).put(decryptedNetworkPayload, 0, 2).array();

        if (isSegmentedMessage(decryptedNetworkPayload[2])) {

            if (segmentedAccessMessagesMessages == null) {
                segmentedAccessMessagesMessages = new HashMap<>();
                segmentedAccessMessagesMessages.put(0, data);
            } else {
                final int k = segmentedAccessMessagesMessages.size();
                segmentedAccessMessagesMessages.put(k, data);
            }
            //Removing the dst here
            final byte[] pdu = ByteBuffer.allocate(2 + networkHeader.length + decryptedNetworkPayload.length).order(ByteOrder.BIG_ENDIAN).put(data, 0, 2).put(networkHeader).put(decryptedNetworkPayload).array();
            final AccessMessage message = parseSegmentedAccessLowerTransportPDU(pdu);
            if (message != null) {
                final HashMap<Integer, byte[]> segmentedMessages = segmentedAccessMessagesMessages;
                segmentedAccessMessagesMessages = null;
                key = 0;
                message.setIvIndex(mMeshNode.getIvIndex());
                message.setNetworkPdu(segmentedMessages);
                message.setCtl(0);
                message.setTtl(ttl);
                message.setSrc(src);
                message.setDst(dst);

                parseUpperTransportPDU(message);
                parseAccessLayerPDU(message);
            }
            return message;

        } else {
            final AccessMessage message = new AccessMessage();
            message.setIvIndex(mMeshNode.getIvIndex());
            final HashMap<Integer, byte[]> networkPduMap = new HashMap<>();
            networkPduMap.put(0, data);
            message.setNetworkPdu(networkPduMap);
            message.setTtl(ttl);
            message.setSrc(src);
            message.setDst(dst);
            message.setSequenceNumber(sequenceNumber);

            //Removing the dst here
            final byte[] pdu = ByteBuffer.allocate(2 + networkHeader.length + decryptedNetworkPayload.length).order(ByteOrder.BIG_ENDIAN).put(data, 0, 2).put(networkHeader).put(decryptedNetworkPayload).array();
            parseUnsegmentedAccessLowerTransportPDU(message, pdu);
            parseUpperTransportPDU(message);

            return message;
        }
    }

    /**
     * Parses control message
     *
     * @param configurationSrc source address of the configurator
     * @param data             data receieved from the node
     * @param networkHeader    de-obfuscated network header
     * @param networkNonce     network nonce
     * @param src              source address
     * @param sequenceNumber   sequence number of the received message
     * @param micLength        network mic length of the received message
     * @return access message
     */
    private ControlMessage parseControlMessage(final byte[] configurationSrc, final byte[] data, final byte[] networkHeader, final byte[] networkNonce, final byte[] src, final byte[] sequenceNumber, final int micLength) {
        final SecureUtils.K2Output k2Output = mMeshNode.getK2Output();

        final byte[] encryptionKey = mEncryptionKey = k2Output.getEncryptionKey();
        mPrivacyKey = k2Output.getPrivacyKey();

        final int ttl = networkHeader[0] & 0x7F;

        final int networkPayloadLength = data.length - (2 + networkHeader.length);
        final byte[] transportPdu = new byte[networkPayloadLength];
        System.arraycopy(data, 8, transportPdu, 0, networkPayloadLength);
        final byte[] decryptedNetworkPayload = SecureUtils.decryptCCM(transportPdu, encryptionKey, networkNonce, micLength);
        final byte[] dst = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).put(decryptedNetworkPayload, 0, 2).array();

        //Check if the message is directed to us, if its not ignore the message
        if (!Arrays.equals(configurationSrc, dst)) {
            Log.v(TAG, "Received a control message that was not directed to us, so we drop it");
            return null;
        }

        if (isSegmentedMessage(decryptedNetworkPayload[2])) {

            if (segmentedControlMessagesMessages == null) {
                segmentedControlMessagesMessages = new HashMap<>();
                segmentedControlMessagesMessages.put(0, data);
            } else {
                final int k = segmentedControlMessagesMessages.size();
                segmentedAccessMessagesMessages.put(k, data);
            }
            //Removing the dst here
            final byte[] pdu = ByteBuffer.allocate(2 + networkHeader.length + decryptedNetworkPayload.length).order(ByteOrder.BIG_ENDIAN).put(data, 0, 2).put(networkHeader).put(decryptedNetworkPayload).array();
            final ControlMessage message = parseSegmentedControlLowerTransportPDU(pdu);
            if (message != null) {
                final HashMap<Integer, byte[]> segmentedMessages = segmentedControlMessagesMessages;
                segmentedControlMessagesMessages = null;
                key = 0;
                message.setIvIndex(mMeshNode.getIvIndex());
                message.setNetworkPdu(segmentedMessages);
                message.setCtl(1);
                message.setTtl(ttl);
                message.setSrc(src);
                message.setDst(dst);

            }
            return message;

        } else {
            final ControlMessage message = new ControlMessage();
            message.setIvIndex(mMeshNode.getIvIndex());
            final HashMap<Integer, byte[]> networkPduMap = new HashMap<>();
            networkPduMap.put(0, data);
            message.setNetworkPdu(networkPduMap);
            message.setTtl(ttl);
            message.setSrc(src);
            message.setDst(dst);
            message.setSequenceNumber(sequenceNumber);

            //Removing the dst here
            final byte[] pdu = ByteBuffer.allocate(2 + networkHeader.length + decryptedNetworkPayload.length).order(ByteOrder.BIG_ENDIAN).put(data, 0, 2).put(networkHeader).put(decryptedNetworkPayload).array();
            parseUnsegmentedControlLowerTransportPDU(message, pdu);

            return message;
        }
    }

    /**
     * Parses control message
     *
     * @param data           data received from the node
     * @param networkHeader  de-obfuscated network header
     * @param networkNonce   network nonce
     * @param src            source address
     * @param sequenceNumber sequence number of the received message
     * @param micLength      network mic length of the received message
     * @return access message
     */
    @VisibleForTesting
    private ControlMessage parseControlMessage(final byte[] data, final byte[] networkHeader, final byte[] networkNonce, final byte[] src, final byte[] sequenceNumber, final int micLength) {
        final SecureUtils.K2Output k2Output = mMeshNode.getK2Output();

        final byte[] encryptionKey = mEncryptionKey = k2Output.getEncryptionKey();
        mPrivacyKey = k2Output.getPrivacyKey();

        final int ttl = networkHeader[0] & 0x7F;

        final int networkPayloadLength = data.length - (2 + networkHeader.length);
        final byte[] transportPdu = new byte[networkPayloadLength];
        System.arraycopy(data, 8, transportPdu, 0, networkPayloadLength);
        final byte[] decryptedNetworkPayload = SecureUtils.decryptCCM(transportPdu, encryptionKey, networkNonce, micLength);
        final byte[] dst = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).put(decryptedNetworkPayload, 0, 2).array();

        if (isSegmentedMessage(decryptedNetworkPayload[2])) {

            if (segmentedControlMessagesMessages == null) {
                segmentedControlMessagesMessages = new HashMap<>();
                segmentedControlMessagesMessages.put(0, data);
            } else {
                final int k = segmentedControlMessagesMessages.size();
                segmentedAccessMessagesMessages.put(k, data);
            }
            //Removing the dst here
            final byte[] pdu = ByteBuffer.allocate(2 + networkHeader.length + decryptedNetworkPayload.length).order(ByteOrder.BIG_ENDIAN).put(data, 0, 2).put(networkHeader).put(decryptedNetworkPayload).array();
            final ControlMessage message = parseSegmentedControlLowerTransportPDU(pdu);
            if (message != null) {
                final HashMap<Integer, byte[]> segmentedMessages = segmentedControlMessagesMessages;
                segmentedControlMessagesMessages = null;
                key = 0;
                message.setIvIndex(mMeshNode.getIvIndex());
                message.setNetworkPdu(segmentedMessages);
                message.setCtl(1);
                message.setTtl(ttl);
                message.setSrc(src);
                message.setDst(dst);

            }
            return message;

        } else {
            final ControlMessage message = new ControlMessage();
            message.setIvIndex(mMeshNode.getIvIndex());
            final HashMap<Integer, byte[]> networkPduMap = new HashMap<>();
            networkPduMap.put(0, data);
            message.setNetworkPdu(networkPduMap);
            message.setTtl(ttl);
            message.setSrc(src);
            message.setDst(dst);
            message.setSequenceNumber(sequenceNumber);

            //Removing the dst here
            final byte[] pdu = ByteBuffer.allocate(2 + networkHeader.length + decryptedNetworkPayload.length).order(ByteOrder.BIG_ENDIAN).put(data, 0, 2).put(networkHeader).put(decryptedNetworkPayload).array();
            parseUnsegmentedControlLowerTransportPDU(message, pdu);

            return message;
        }
    }
}
