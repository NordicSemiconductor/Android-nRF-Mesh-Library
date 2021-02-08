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

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

import static no.nordicsemi.android.mesh.NetworkKey.KeyRefreshPhaseTransition;
import static no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_KEY_REFRESH_PHASE_SET;

/**
 * Creates the ConfigKeyRefreshPhaseSet message.
 */
public class ConfigKeyRefreshPhaseSet extends ConfigMessage {

    private static final String TAG = ConfigKeyRefreshPhaseSet.class.getSimpleName();
    private static final int OP_CODE = CONFIG_KEY_REFRESH_PHASE_SET;
    private final NetworkKey mNetKey;
    private final @KeyRefreshPhaseTransition
    int transition;

    /**
     * Constructs ConfigKeyRefreshPhaseSet message.
     *
     * @param networkKey {@link NetworkKey}
     */
    public ConfigKeyRefreshPhaseSet(@NonNull final NetworkKey networkKey, @KeyRefreshPhaseTransition final int transition) {
        mNetKey = networkKey;
        this.transition = transition;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        Log.d(TAG, "NetKeyIndex: " + mNetKey.getKeyIndex());
        final byte[] netKeyIndex = MeshParserUtils.addKeyIndexPadding(mNetKey.getKeyIndex());
        mParameters = new byte[]{netKeyIndex[1], (byte) ((netKeyIndex[0] & 0xFF) & 0x0F), (byte) transition};
    }
}
