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

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * To be used as a wrapper class to create the ConfigAppKeyAdd message.
 */
@SuppressWarnings("unused")
public class ConfigAppKeyDelete extends ConfigMessage {

    private static final String TAG = ConfigAppKeyDelete.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_APPKEY_DELETE;

    private final NetworkKey mNetKey;
    private final ApplicationKey mAppKey;

    /**
     * Constructs ConfigAppKeyAdd message.
     *
     * @param appKey application key for this message
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public ConfigAppKeyDelete(@NonNull final NetworkKey networkKey, @NonNull final ApplicationKey appKey) throws IllegalArgumentException {
        if (networkKey.getKey().length != 16)
            throw new IllegalArgumentException("Network key must be 16 bytes");

        if (appKey.getKey().length != 16)
            throw new IllegalArgumentException("App key must be 16 bytes");

        this.mNetKey = networkKey;
        this.mAppKey = appKey;
        assembleMessageParameters();
    }

    /**
     * Returns the Network key that is needs to be sent to the node
     *
     * @return app key
     */
    public NetworkKey getNetKey() {
        return mNetKey;
    }

    /**
     * Returns the application key that is needs to be sent to the node
     *
     * @return app key
     */
    public ApplicationKey getAppKey() {
        return mAppKey;
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }


    @Override
    void assembleMessageParameters() {
        Log.v(TAG, "NetKeyIndex: " + mNetKey.getKeyIndex());
        Log.v(TAG, "AppKeyIndex: " + mAppKey.getKeyIndex());
        final byte[] netKeyIndex = MeshParserUtils.addKeyIndexPadding(mNetKey.getKeyIndex());
        final byte[] appKeyIndex = MeshParserUtils.addKeyIndexPadding(mAppKey.getKeyIndex());

        final ByteBuffer paramsBuffer = ByteBuffer.allocate(3).order(ByteOrder.LITTLE_ENDIAN);
        paramsBuffer.put(netKeyIndex[1]);
        paramsBuffer.put((byte) (((appKeyIndex[1] & 0xFF) << 4) | (netKeyIndex[0] & 0xFF) & 0x0F));
        paramsBuffer.put((byte) (((appKeyIndex[0] & 0xFF) << 4) | (appKeyIndex[1] & 0xFF) >> 4));
        mParameters = paramsBuffer.array();
    }
}
