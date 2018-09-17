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
import java.util.List;

import no.nordicsemi.android.meshprovisioner.messages.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messages.ControlMessage;
import no.nordicsemi.android.meshprovisioner.messages.Message;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

abstract class UpperTransportLayer extends AccessLayer {

    private static final int APPLICATION_KEY_IDENTIFIER = 0; //Identifies that the device key is to be used
    private static final int MAX_UNSEGMENTED_ACCESS_PAYLOAD_LENGTH = 15;
    static final int MAX_SEGMENTED_ACCESS_PAYLOAD_LENGTH = 12;
    static final int MAX_UNSEGMENTED_CONTROL_PAYLOAD_LENGTH = 11;
    static final int MAX_SEGMENTED_CONTROL_PAYLOAD_LENGTH = 8;
    /**
     * Nonce types
     **/
    static final int NONCE_TYPE_NETWORK = 0x00;
    private static final int NONCE_TYPE_APPLICATION = 0x01;
    private static final int NONCE_TYPE_DEVICE = 0x02;
    static final int NONCE_TYPE_PROXY = 0x03;
    /**
     * Nonce paddings
     **/
    static final int PAD_NETWORK_NONCE = 0x00;
    private static final int PAD_APPLICATION_DEVICE_NONCE = 0b0000000;
    static final int PAD_PROXY_NONCE = 0x00;
    private static final String TAG = UpperTransportLayer.class.getSimpleName();
    private static final int SZMIC = 1; //Transmic becomes 8 bytes
    private static final int TRANSPORT_SAR_SEQZERO_MASK = 8191;
    private static final int DEFAULT_UNSEGMENTED_MIC_LENGTH = 4; //octets
    private static final int MINIMUM_TRANSMIC_LENGTH = 4; // bytes
    private static final int MAXIMUM_TRANSMIC_LENGTH = 8; // bytes

    /**
     * Creates a mesh message containing an upper transport access pdu
     * @param message The access message required to create the encrypted upper transport pdu
     */
    void createMeshMessage(final Message message) { //Access message
        super.createMeshMessage(message);
        final AccessMessage accessMessage = (AccessMessage) message;
        final byte[] encryptedTransportPDU = encryptUpperTransportPDU(accessMessage);
        Log.v(TAG, "Encrypted upper transport pdu: " + MeshParserUtils.bytesToHex(encryptedTransportPDU, false));
        accessMessage.setUpperTransportPdu(encryptedTransportPDU);
    }

    /**
     * Creates a vendor model mesh message containing an upper transport access pdu
     * @param message The access message required to create the encrypted upper transport pdu
     */
    void createVendorMeshMessage(final Message message) { //Access message
        super.createVendorMeshMessage(message);
        final AccessMessage accessMessage = (AccessMessage) message;
        final byte[] encryptedTransportPDU = encryptUpperTransportPDU(accessMessage);
        Log.v(TAG, "Encrypted upper transport pdu: " + MeshParserUtils.bytesToHex(encryptedTransportPDU, false));
        accessMessage.setUpperTransportPdu(encryptedTransportPDU);
    }

    /**
     * Creates the upper transport access pdu
     * @param accessMessage The access message required to create the encrypted upper transport pdu
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public void createUpperTransportPDU(final AccessMessage accessMessage) { //Access message
        final byte[] encryptedTransportPDU = encryptUpperTransportPDU(accessMessage);
        Log.v(TAG, "Encrypted upper transport pdu: " + MeshParserUtils.bytesToHex(encryptedTransportPDU, false));
        accessMessage.setUpperTransportPdu(encryptedTransportPDU);
    }

    /**
     * Creates lower transport pdu
     */
    abstract void createLowerTransportAccessPDU(final AccessMessage message);

    /**
     * Creates lower transport pdu
     */
    abstract void createLowerTransportControlPDU(final ControlMessage message);

    /**
     * Removes the lower transport layer header and reassembles a segented lower transport access pdu in to one message
     *
     * @param accessMessage access message containing the lower transport pdus
     */
    abstract void reassembleLowerTransportAccessPDU(final AccessMessage accessMessage);


    /**
     * Removes the lower transport layer header and reassembles a segented lower transport control pdu in to one message
     *
     * @param controlMessage control message containing the lower transport pdus
     */
    abstract void reassembleLowerTransportControlPDU(final ControlMessage controlMessage);

    /**
     * Parse upper transport pdu
     *
     * @param message access message containing the upper transport pdu
     */
    final void parseUpperTransportPDU(final AccessMessage message) {
        final int ctl = message.getCtl();
        if (ctl == 0) { //Access message
            reassembleLowerTransportAccessPDU(message);
            final byte[] decryptedUpperTransportControlPdu = decryptUpperTransportPDU(message);
            message.setAccessPdu(decryptedUpperTransportControlPdu);
        }
    }

    /**
     * Encrypts upper transport pdu
     *
     * @return encrypted upper transport pdu
     */
    private byte[] encryptUpperTransportPDU(final byte[] sequenceNumber, final byte[] src, final byte[] dst, final byte[] ivIndex, final byte[] key, final int akf, final int transMicLength, final byte[] accessPDU) {
        final int aszmic = 0; // upper transport layer will alaways have the aszmic as 0 because the mic is always 32bit
        byte[] nonce;

        if (akf == APPLICATION_KEY_IDENTIFIER) {
            nonce = createDeviceNonce(aszmic, sequenceNumber, src, dst, ivIndex);
        } else {
            nonce = createApplicationNonce(aszmic, sequenceNumber, src, dst, ivIndex);
        }
        return SecureUtils.encryptCCM(accessPDU, key, nonce, transMicLength);
    }

    /**
     * Encrypts upper transport pdu
     *
     * @param message access message object containing the upper transport pdu
     * @return encrypted upper transport pdu
     */
    private byte[] encryptUpperTransportPDU(final AccessMessage message) {
        final byte[] accessPDU = message.getAccessPdu();
        final int akf = message.getAkf();
        final int aszmic = message.getAszmic(); // upper transport layer will alaways have the aszmic as 0 because the mic is always 32bit

        final byte[] sequenceNumber = message.getSequenceNumber();
        final byte[] src = message.getSrc();
        final byte[] dst = message.getDst();
        final byte[] ivIndex = message.getIvIndex();
        final byte[] key = message.getKey();

        byte[] nonce;
        if (akf == APPLICATION_KEY_IDENTIFIER) {
            nonce = createDeviceNonce(aszmic, sequenceNumber, src, dst, ivIndex);
            Log.v(TAG, "Device nonce: " + MeshParserUtils.bytesToHex(nonce, false));
        } else {
            nonce = createApplicationNonce(aszmic, sequenceNumber, src, dst, ivIndex);
            Log.v(TAG, "Application nonce: " + MeshParserUtils.bytesToHex(nonce, false));
        }

        int transMicLength;
        final int encryptedPduLength = accessPDU.length + MINIMUM_TRANSMIC_LENGTH;

        if (encryptedPduLength <= MAX_UNSEGMENTED_ACCESS_PAYLOAD_LENGTH) {
            transMicLength = SecureUtils.getTransMicLength(message.getCtl());
        } else {
            transMicLength = SecureUtils.getTransMicLength(message.getAszmic());
        }

        return SecureUtils.encryptCCM(accessPDU, key, nonce, transMicLength);
    }

    /**
     * Decrypts upper transport pdu
     *
     * @param accessMessage access message object containing the upper transport pdu
     * @return decrypted upper transport pdu
     */
    private byte[] decryptUpperTransportPDU(final AccessMessage accessMessage) {
        byte[] decryptedUpperTansportPDU;
        final byte[] key;
        //Check if the key used for encryption is an application key or a device key
        final byte[] nonce;
        if(APPLICATION_KEY_IDENTIFIER == accessMessage.getAkf()) {
            key = mMeshNode.getDeviceKey();
            //If its a device key that was used to encrypt the message we need to create a device nonce to decrypt it
            nonce = createDeviceNonce(accessMessage.getAszmic(), accessMessage.getSequenceNumber(), accessMessage.getSrc(), accessMessage.getDst(), accessMessage.getIvIndex());
        } else {
            //mMeshNode.getAddedAppKeys()
            key = getApplicationKey(accessMessage.getAid());//mUpperTransportLayerCallbacks.getApplicationKey();
            if(key == null)
                throw new IllegalArgumentException("Unable to find the app key to decrypt the message");

            final int aid = SecureUtils.calculateK4(key);
            if(aid != accessMessage.getAid()) {
                throw new IllegalArgumentException("Unable to decrypt the message, invalid application key identifier");
            }
            //If its an application key that was used to encrypt the message we need to create a application nonce to decrypt it
            nonce = createApplicationNonce(accessMessage.getAszmic(), accessMessage.getSequenceNumber(), accessMessage.getSrc(), accessMessage.getDst(), accessMessage.getIvIndex());
        }

        if (accessMessage.getAszmic() == SZMIC) {
            decryptedUpperTansportPDU = SecureUtils.decryptCCM(accessMessage.getUpperTransportPdu(), key, nonce, MAXIMUM_TRANSMIC_LENGTH);
        } else {
            decryptedUpperTansportPDU = SecureUtils.decryptCCM(accessMessage.getUpperTransportPdu(), key, nonce, MINIMUM_TRANSMIC_LENGTH);
        }

        final byte[] tempBytes = new byte[decryptedUpperTansportPDU.length];
        ByteBuffer decryptedBuffer = ByteBuffer.wrap(tempBytes);
        decryptedBuffer.order(ByteOrder.LITTLE_ENDIAN);
        decryptedBuffer.put(decryptedUpperTansportPDU);
        decryptedUpperTansportPDU = decryptedBuffer.array();
        return decryptedUpperTansportPDU;
    }

    private byte[] getApplicationKey(final int receivedAid){
        final List<String> keys = new ArrayList<>(mMeshNode.getAddedAppKeys().values());
        for(String key : keys){
            final byte[] k = MeshParserUtils.toByteArray(key);
            final int aid = SecureUtils.calculateK4(k);
            if(receivedAid == aid){
                return k;
            }
        }
        return null;
    }

    /**
     * Creates the application nonce
     *
     * @param aszmic         aszmic (szmic if a segmented access message)
     * @param sequenceNumber sequence number of the message
     * @param src            source address
     * @param dst            destination address
     * @return Application nonce
     */
    private byte[] createApplicationNonce(final int aszmic, final byte[] sequenceNumber, final byte[] src, final byte[] dst, final byte[] ivIndex) {
        final ByteBuffer applicationNonceBuffer = ByteBuffer.allocate(13);
        applicationNonceBuffer.put((byte) NONCE_TYPE_APPLICATION); //Nonce type
        applicationNonceBuffer.put((byte) ((aszmic << 7) | PAD_APPLICATION_DEVICE_NONCE)); //ASZMIC (SZMIC if a segmented access message) and PAD
        applicationNonceBuffer.put(sequenceNumber);
        applicationNonceBuffer.put(src);
        applicationNonceBuffer.put(dst);
        applicationNonceBuffer.put(ivIndex);
        return applicationNonceBuffer.array();
    }

    /**
     * Creates the device nonce
     *
     * @param aszmic         aszmic (szmic if a segmented access message)
     * @param sequenceNumber sequence number of the message
     * @param src            source address
     * @param dst            destination address
     * @return Device  nonce
     */
    private byte[] createDeviceNonce(final int aszmic, final byte[] sequenceNumber, final byte[] src, final byte[] dst, final byte[] ivIndex) {
        final ByteBuffer deviceNonceBuffer = ByteBuffer.allocate(13);
        deviceNonceBuffer.put((byte) NONCE_TYPE_DEVICE); //Nonce type
        deviceNonceBuffer.put((byte) ((aszmic << 7) | PAD_APPLICATION_DEVICE_NONCE)); //ASZMIC (SZMIC if a segmented access message) and PAD
        deviceNonceBuffer.put(sequenceNumber);
        deviceNonceBuffer.put(src);
        deviceNonceBuffer.put(dst);
        deviceNonceBuffer.put(ivIndex);
        return deviceNonceBuffer.array();
    }

    /**
     * Derives the original transport layer sequence number from the network layer sequence number that was received with every segment
     *
     * @param networkLayerSequenceNumber sequence number on network layer which is a part of the original pdu received
     * @param seqZero                    the lower 13 bits of the sequence number. This is a part of the lower transport pdu header and is the same value for all segments
     * @return original transport layer sequence number that was used to encrypt the transport layer pdu
     */
    final int getTransportLayerSequenceNumber(final int networkLayerSequenceNumber, final int seqZero) {
        if ((networkLayerSequenceNumber & TRANSPORT_SAR_SEQZERO_MASK) < seqZero) {
            return ((networkLayerSequenceNumber - ((networkLayerSequenceNumber & TRANSPORT_SAR_SEQZERO_MASK) - seqZero) - (TRANSPORT_SAR_SEQZERO_MASK + 1)));
        } else {
            return ((networkLayerSequenceNumber - ((networkLayerSequenceNumber & TRANSPORT_SAR_SEQZERO_MASK) - seqZero)));
        }
    }

}
