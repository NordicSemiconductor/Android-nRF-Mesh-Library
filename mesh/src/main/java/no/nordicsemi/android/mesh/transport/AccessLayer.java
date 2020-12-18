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

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import static no.nordicsemi.android.mesh.utils.MeshParserUtils.bytesToHex;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.createVendorOpCode;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.getOpCode;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.getOpCodeLength;

/**
 * AccessLayer implementation of the mesh network architecture as per the mesh profile specification.
 * <p>
 * AccessLayer class generates/parses a raw mesh message containing the specific OpCode and Parameters.
 * </p>
 */
abstract class AccessLayer {

    private static final String TAG = AccessLayer.class.getSimpleName();
    protected Context mContext;
    Handler mHandler;
    ProvisionedMeshNode mMeshNode;

    protected abstract void initHandler();

    /**
     * Creates an access message
     *
     * @param message Access message containing the required opcodes and parameters to create access message pdu.
     */
    void createMeshMessage(@NonNull final Message message) {
        createAccessMessage((AccessMessage) message);
    }

    /**
     * Creates a vendor model access message
     *
     * @param message Access message containing the required opcodes and parameters to create access message pdu.
     */
    void createVendorMeshMessage(@NonNull final Message message) {
        createCustomAccessMessage((AccessMessage) message);
    }

    /**
     * Creates an access message
     *
     * @param accessMessage Access message containing the required opcodes and parameters to create access message pdu.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    final void createAccessMessage(@NonNull final AccessMessage accessMessage) {
        final byte[] opCodes = getOpCode(accessMessage.getOpCode());
        final byte[] parameters = accessMessage.getParameters();
        final ByteBuffer accessMessageBuffer;
        if (parameters != null) {
            accessMessageBuffer = ByteBuffer.allocate(opCodes.length + parameters.length);
            accessMessageBuffer.put(opCodes).put(parameters);
        } else {
            accessMessageBuffer = ByteBuffer.allocate(opCodes.length);
            accessMessageBuffer.put(opCodes);
        }
        final byte[] accessPdu = accessMessageBuffer.array();

        Log.v(TAG, "Created Access PDU " + bytesToHex(accessPdu, false));
        accessMessage.setAccessPdu(accessMessageBuffer.array());
    }

    /**
     * Creates an access message
     *
     * @param accessMessage Access message containing the required opcodes and parameters to create access message pdu.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    final void createCustomAccessMessage(@NonNull final AccessMessage accessMessage) {
        final byte[] parameters = accessMessage.getParameters();
        final byte[] vendorOpcode = createVendorOpCode(accessMessage.getOpCode(),
                accessMessage.getCompanyIdentifier());
        final ByteBuffer accessMessageBuffer;
        if (parameters != null) {
            accessMessageBuffer = ByteBuffer.allocate(vendorOpcode.length + parameters.length);
            accessMessageBuffer.put(vendorOpcode);
            accessMessageBuffer.put(parameters);
        } else {
            accessMessageBuffer = ByteBuffer.allocate(vendorOpcode.length);
            accessMessageBuffer.put(vendorOpcode);
        }
        final byte[] accessPdu = accessMessageBuffer.array();
        Log.v(TAG, "Created Access PDU " + bytesToHex(accessPdu, false));
        accessMessage.setAccessPdu(accessPdu);
    }

    /**
     * Parse access pdu
     *
     * @param message underlying message containing the access pdu
     */
    final void parseAccessLayerPDU(@NonNull final AccessMessage message) {
        //MSB of the first octet defines the length of opcodes.
        //if MSB = 0 length is 1 and so forth
        final byte[] accessPayload = message.getAccessPdu();
        final int opCodeLength = getOpCodeLength(accessPayload[0] & 0xFF);
        message.setOpCode(getOpCode(accessPayload, opCodeLength));
        final int length = accessPayload.length - opCodeLength;
        final ByteBuffer paramsBuffer = ByteBuffer.allocate(length).order(ByteOrder.BIG_ENDIAN);
        paramsBuffer.put(accessPayload, opCodeLength, length);
        message.setParameters(paramsBuffer.array());
        Log.v(TAG, "Received Access PDU " + bytesToHex(accessPayload, false));
    }
}
