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

package no.nordicsemi.android.meshprovisioner.configuration;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.messages.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messages.ControlMessage;
import no.nordicsemi.android.meshprovisioner.messages.Message;
import no.nordicsemi.android.meshprovisioner.models.VendorModel;
import no.nordicsemi.android.meshprovisioner.transport.LowerTransportLayerCallbacks;
import no.nordicsemi.android.meshprovisioner.transport.NetworkLayer;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

final class MeshTransport extends NetworkLayer {

    private static final String TAG = MeshTransport.class.getSimpleName();

    MeshTransport(final Context context, final ProvisionedMeshNode unprovisionedMeshNode) {
        super();
        this.mContext = context;
        this.mMeshNode = unprovisionedMeshNode;
        initHandler();
    }

    @Override
    protected void initHandler() {
        this.mHandler = new Handler(mContext.getMainLooper());
    }

    @Override
    public final void setLowerTransportLayerCallbacks(final LowerTransportLayerCallbacks callbacks) {
        super.setLowerTransportLayerCallbacks(callbacks);
    }

    @Override
    protected int incrementSequenceNumber() {
        return SequenceNumber.incrementAndStore(mContext);
    }

    @Override
    protected int incrementSequenceNumber(final byte[] sequenceNumber) {
        return SequenceNumber.incrementAndStore(mContext, sequenceNumber);
    }

    /**
     * Creates the an acknowledgement message for the received segmented messages
     *
     * @param controlMessage Control message containing the required opcodes and parameters to create the message
     * @return Control message containing the acknowledgement message pdu
     */
    public ControlMessage createSegmentBlockAcknowledgementMessage(final ControlMessage controlMessage) {
        createLowerTransportControlPDU(controlMessage);
        createNetworkLayerPDU(controlMessage);
        return controlMessage;
    }

    /**
     * Creates an access message to be sent to the peripheral node
     * <p>
     * This method will create the access message and propagate the message through the transport layers to create the final mesh pdu.
     * The message created will use the node's unicast address as the destination for the message to be sent
     * </p>
     *
     * @param provisionedMeshNode     mesh node to which the message is to be sent
     * @param src                     Source address of the provisioner/configurator.
     * @param key                     Key could be application key or device key.
     * @param akf                     Application key flag defines which key to be used to decrypt the message i.e device key or application key.
     * @param aid                     Identifier of the application key.
     * @param aszmic                  Defines the length of the transport mic length where 1 will encrypt withn 64 bit and 0 with 32 bit encryption.
     * @param accessOpCode            Operation code for the access message.
     * @param accessMessageParameters Parameters for the access message.
     * @return access message containing the mesh pdu
     */
    AccessMessage createMeshMessage(final ProvisionedMeshNode provisionedMeshNode, final byte[] src,
                                    final byte[] key, final int akf, final int aid, final int aszmic,
                                    final int accessOpCode, final byte[] accessMessageParameters) {

        final int sequenceNumber = incrementSequenceNumber();
        final byte[] sequenceNum = MeshParserUtils.getSequenceNumberBytes(sequenceNumber);

        Log.v(TAG, "Src address: " + MeshParserUtils.bytesToHex(src, false));
        Log.v(TAG, "Dst address: " + MeshParserUtils.bytesToHex(provisionedMeshNode.getUnicastAddress(), false));
        Log.v(TAG, "Key: " + MeshParserUtils.bytesToHex(key, false));
        Log.v(TAG, "akf: " + akf);
        Log.v(TAG, "aid: " + aid);
        Log.v(TAG, "aszmic: " + aszmic);
        Log.v(TAG, "Access message opcode: " + accessOpCode);
        Log.v(TAG, "Access message parameters: " + MeshParserUtils.bytesToHex(accessMessageParameters, false));


        final AccessMessage message = new AccessMessage();
        message.setSrc(src);
        message.setDst(provisionedMeshNode.getUnicastAddress());
        message.setIvIndex(provisionedMeshNode.getIvIndex());
        message.setSequenceNumber(sequenceNum);
        message.setKey(key);
        message.setAkf(akf);
        message.setAid(aid);
        message.setAszmic(aszmic);
        message.setOpCode(accessOpCode);
        message.setParameters(accessMessageParameters);
        message.setPduType(NETWORK_PDU);

        super.createMeshMessage(message);
        return message;
    }

    /**
     * Creates an access message to be sent to the peripheral node
     * <p>
     * This method will create the access message and propagate the message through the transport layers to create the final mesh pdu.
     * </p>
     *
     * @param provisionedMeshNode     mesh node to which the message is to be sent
     * @param src                     Source address of the provisioner/configurator.
     * @param dst                     destination address to be sent to
     * @param key                     Key could be application key or device key.
     * @param akf                     Application key flag defines which key to be used to decrypt the message i.e device key or application key.
     * @param aid                     Identifier of the application key.
     * @param aszmic                  Defines the length of the transport mic length where 1 will encrypt withn 64 bit and 0 with 32 bit encryption.
     * @param accessOpCode            Operation code for the access message.
     * @param accessMessageParameters Parameters for the access message.
     * @return access message containing the mesh pdu
     */
    AccessMessage createMeshMessage(final ProvisionedMeshNode provisionedMeshNode, final byte[] src, final byte[] dst,
                                    final byte[] key, final int akf, final int aid, final int aszmic,
                                    final int accessOpCode, final byte[] accessMessageParameters) {

        final int sequenceNumber = incrementSequenceNumber();
        final byte[] sequenceNum = MeshParserUtils.getSequenceNumberBytes(sequenceNumber);

        Log.v(TAG, "Src address: " + MeshParserUtils.bytesToHex(src, false));
        Log.v(TAG, "Dst address: " + MeshParserUtils.bytesToHex(dst, false));
        Log.v(TAG, "Key: " + MeshParserUtils.bytesToHex(key, false));
        Log.v(TAG, "akf: " + akf);
        Log.v(TAG, "aid: " + aid);
        Log.v(TAG, "aszmic: " + aszmic);
        Log.v(TAG, "Access message opcode: " + Integer.toHexString(accessOpCode));
        Log.v(TAG, "Access message parameters: " + MeshParserUtils.bytesToHex(accessMessageParameters, false));

        final AccessMessage message = new AccessMessage();
        message.setSrc(src);
        message.setDst(dst);
        message.setIvIndex(provisionedMeshNode.getIvIndex());
        message.setSequenceNumber(sequenceNum);
        message.setKey(key);
        message.setAkf(akf);
        message.setAid(aid);
        message.setAszmic(aszmic);
        message.setOpCode(accessOpCode);
        message.setParameters(accessMessageParameters);
        message.setPduType(NETWORK_PDU);

        super.createMeshMessage(message);
        return message;
    }

    /**
     * Creates a vendor model access message to be sent to the peripheral node
     * <p>
     * This method will create the access message and propagate the message through the transport layers to create the final mesh pdu.
     * </p>
     *
     * @param provisionedMeshNode     mesh node to which the message is to be sent
     * @param mMeshModel
     * @param src                     Source address of the provisioner/configurator.
     * @param dst                     destination address to be sent to
     * @param key                     Key could be application key or device key.
     * @param akf                     Application key flag defines which key to be used to decrypt the message i.e device key or application key.
     * @param aid                     Identifier of the application key.
     * @param aszmic                  Defines the length of the transport mic length where 1 will encrypt withn 64 bit and 0 with 32 bit encryption.
     * @param accessOpCode            Operation code for the access message.
     * @param accessMessageParameters Parameters for the access message.
     * @return access message containing the mesh pdu
     */
    AccessMessage createVendorMeshMessage(final ProvisionedMeshNode provisionedMeshNode, final VendorModel mMeshModel, final byte[] src, final byte[] dst,
                                          final byte[] key, final int akf, final int aid, final int aszmic,
                                          final int accessOpCode, final byte[] accessMessageParameters) {

        final int sequenceNumber = incrementSequenceNumber();
        final byte[] sequenceNum = MeshParserUtils.getSequenceNumberBytes(sequenceNumber);

        Log.v(TAG, "Src address: " + MeshParserUtils.bytesToHex(src, false));
        Log.v(TAG, "Dst address: " + MeshParserUtils.bytesToHex(dst, false));
        Log.v(TAG, "Key: " + MeshParserUtils.bytesToHex(key, false));
        Log.v(TAG, "akf: " + akf);
        Log.v(TAG, "aid: " + aid);
        Log.v(TAG, "aszmic: " + aszmic);
        Log.v(TAG, "Access message opcode: " + Integer.toHexString(accessOpCode));
        Log.v(TAG, "Access message parameters: " + MeshParserUtils.bytesToHex(accessMessageParameters, false));

        final AccessMessage message = new AccessMessage();
        message.setCompanyIdentifier(mMeshModel.getCompanyIdentifier());
        message.setSrc(src);
        message.setDst(dst);
        message.setIvIndex(provisionedMeshNode.getIvIndex());
        message.setSequenceNumber(sequenceNum);
        message.setKey(key);
        message.setAkf(akf);
        message.setAid(aid);
        message.setAszmic(aszmic);
        message.setOpCode(accessOpCode);
        message.setParameters(accessMessageParameters);
        message.setPduType(NETWORK_PDU);

        super.createVendorMeshMessage(message);
        return message;
    }

    Message createRetransmitMeshMessage(final Message message, final int segment){
        createRetransmitNetworkLayerPDU(message, segment);
        return message;
    }

    /**
     * Parses the received pdu
     *
     * @param configurationSrc Src address where the original message was sent from
     * @param pdu              pdu received
     * @return Message
     */
    public Message parsePdu(final byte[] configurationSrc, final byte[] pdu) {
        return parseMeshMessage(configurationSrc, pdu);
    }

    /**
     * Parses the received pdu
     *
     * @param pdu pdu received
     * @return Message
     */
    @VisibleForTesting
    public Message parsePdu(final byte[] pdu) {
        return parseMeshMessage(pdu);
    }

}
