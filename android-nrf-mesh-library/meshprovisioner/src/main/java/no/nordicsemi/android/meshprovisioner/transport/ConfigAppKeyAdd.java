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

import android.support.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.meshprovisioner.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * To be used as a wrapper class to create the ConfigAppKeyAdd message.
 */
@SuppressWarnings("unused")
public class ConfigAppKeyAdd extends ConfigMessage {

    private static final String TAG = ConfigAppKeyAdd.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_APPKEY_ADD;

    private final byte[] mAppKey;
    private final int mAppKeyIndex;
    private byte[] mDeviceKey;

    /**
     * Constructs ConfigAppKeyAdd message.
     *
     * @param node        Mesh node this message is to be sent to
     * @param appKey      application key for this message
     * @param appKeyIndex application key index of this message
     * @param aszmic      size of message integrity check
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public ConfigAppKeyAdd(@NonNull final ProvisionedMeshNode node, @NonNull final byte[] appKey, final int appKeyIndex, final int aszmic) throws IllegalArgumentException {
        super(node, aszmic);
        if (appKey.length != 16)
            throw new IllegalArgumentException("App key must be 16 bytes");

        this.mAppKey = appKey;
        this.mAppKeyIndex = appKeyIndex;
        assembleMessageParameters();
    }

    /**
     * Returns the application key that is needs to be sent to the node
     *
     * @return app key
     */
    public byte[] getAppKey() {
        return mAppKey;
    }

    /**
     * Returns the application key index.
     *
     * @return app key index
     */
    public int getAppKeyIndex() {
        return mAppKeyIndex;
    }

    /**
     * Returns the device key that is to be used to encrypt this message
     *
     * @return device key
     */
    public byte[] getDeviceKey() {
        return mDeviceKey;
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }


    @Override
    void assembleMessageParameters() {
        mDeviceKey = mNode.getDeviceKey();
        final byte[] networkKeyIndex = mNode.getKeyIndex();
        final byte[] applicationKeyIndex = MeshParserUtils.addKeyIndexPadding(mAppKeyIndex);

        final ByteBuffer paramsBuffer = ByteBuffer.allocate(19).order(ByteOrder.BIG_ENDIAN);
        paramsBuffer.put(networkKeyIndex[1]);
        paramsBuffer.put((byte) ((applicationKeyIndex[1] << 4) | networkKeyIndex[0] & 0x0F));
        paramsBuffer.put((byte) ((applicationKeyIndex[0] << 4) | applicationKeyIndex[1] >> 4));
        paramsBuffer.put(mAppKey);
        mParameters = paramsBuffer.array();
    }
}
