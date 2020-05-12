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

import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;


/**
 * To be used as a wrapper class for when creating the ConfigNetworkTransmitSet message.
 */
@SuppressWarnings({"unused"})
public final class ConfigNetworkTransmitSet extends ConfigMessage {

    private static final String TAG = ConfigNetworkTransmitSet.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_NETWORK_TRANSMIT_SET;

    private final int mNetworkTransmitCount;
    private final int mNetworkTransmitIntervalSteps;

    /**
     * Constructs a ConfigNetworkTransmitSet message.
     *
     * @param networkTransmitCount         The Network Transmit Count to be set
     * @param networkTransmitIntervalSteps The Network Transmit Interval Steps to be set
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public ConfigNetworkTransmitSet(final int networkTransmitCount,
                                    final int networkTransmitIntervalSteps) throws IllegalArgumentException {
        if (networkTransmitCount < 0 || 0b111 < networkTransmitCount) {
            throw new IllegalArgumentException("Network Transmit Count must be between 0 and 7 (inclusive)");
        }
        if (networkTransmitIntervalSteps < 0 || 0b11111 < networkTransmitIntervalSteps) {
            throw new IllegalArgumentException("Network Transmit Interval Steps must be between 0 and 31 (inclusive)");
        }
        this.mNetworkTransmitCount = networkTransmitCount;
        this.mNetworkTransmitIntervalSteps = networkTransmitIntervalSteps;
        assembleMessageParameters();
    }

    @Override
    final void assembleMessageParameters() {
        mParameters = new byte[]{(byte) (((mNetworkTransmitIntervalSteps << 3) & 0xFF) | (mNetworkTransmitCount & 0xFF))};
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    /**
     * Returns the Network Transmit Count set in this message
     *
     * @return Network Transmit Count
     */
    public int getNetworkTransmitCount() {
        return mNetworkTransmitCount;
    }

    /**
     * Returns the Network Transmit Interval Steps set in this message
     *
     * @return Network Transmit Interval Steps
     */
    public int getNetworkTransmitIntervalSteps() {
        return mNetworkTransmitIntervalSteps;
    }
}
