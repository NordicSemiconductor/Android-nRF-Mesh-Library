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

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.mesh.utils.CompositionDataParser;
import no.nordicsemi.android.mesh.utils.MeshAddress;

/**
 * To be used as a wrapper class to create a ConfigModelPublicationSet message.
 */
@SuppressWarnings({"unused"})
public class ConfigModelPublicationGet extends ConfigMessage {

    private static final String TAG = ConfigModelPublicationGet.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_MODEL_PUBLICATION_GET;

    private static final int SIG_MODEL_PUBLISH_GET_PARAMS_LENGTH = 4;
    private static final int VENDOR_MODEL_PUBLISH_GET_PARAMS_LENGTH = 6;

    private final int elementAddress;
    private final int modelIdentifier;

    /**
     * Constructs a ConfigModelPublicationGet message
     *
     * @param elementAddress                 Element address that should publish
     * @param modelIdentifier                identifier for this model that will do publication
     * @throws IllegalArgumentException for invalid arguments
     */
    public ConfigModelPublicationGet(final int elementAddress,
                                     final int modelIdentifier) throws IllegalArgumentException {
        if (!MeshAddress.isValidUnicastAddress(elementAddress))
            throw new IllegalArgumentException("Invalid unicast address, unicast address must be a 16-bit value, and must range from 0x0001 to 0x7FFF");
        this.elementAddress = elementAddress;
        this.modelIdentifier = modelIdentifier;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }


    @Override
    void assembleMessageParameters() {
        final ByteBuffer paramsBuffer;
        Log.v(TAG, "Element address: " + MeshAddress.formatAddress(elementAddress, true));
        Log.v(TAG, "Model: " + CompositionDataParser.formatModelIdentifier(modelIdentifier, false));

        //We check if the model identifier value is within the range of a 16-bit value here. If it is then it is a sigmodel
        if (modelIdentifier >= Short.MIN_VALUE && modelIdentifier <= Short.MAX_VALUE) {
            paramsBuffer = ByteBuffer.allocate(SIG_MODEL_PUBLISH_GET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.putShort((short) elementAddress);
            paramsBuffer.putShort((short) modelIdentifier);
            mParameters = paramsBuffer.array();
        } else {
            paramsBuffer = ByteBuffer.allocate(VENDOR_MODEL_PUBLISH_GET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.putShort((short) elementAddress);
            final byte[] modelIdentifier = new byte[]{(byte) ((this.modelIdentifier >> 24) & 0xFF), (byte) ((this.modelIdentifier >> 16) & 0xFF),
                    (byte) ((this.modelIdentifier >> 8) & 0xFF), (byte) (this.modelIdentifier & 0xFF)};
            paramsBuffer.put(modelIdentifier[1]);
            paramsBuffer.put(modelIdentifier[0]);
            paramsBuffer.put(modelIdentifier[3]);
            paramsBuffer.put(modelIdentifier[2]);
            mParameters = paramsBuffer.array();
        }
    }

    /**
     * Returns the element address to which the app key must be bound.
     *
     * @return element address
     */
    public int getElementAddress() {
        return elementAddress;
    }

    /**
     * Returns the model identifier to which the key is to be bound.
     *
     * @return 16-bit or 32-bit vendor model identifier
     */
    public int getModelIdentifier() {
        return modelIdentifier;
    }
}
