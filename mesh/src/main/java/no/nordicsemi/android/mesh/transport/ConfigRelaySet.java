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
import no.nordicsemi.android.mesh.utils.RelaySettings;


/**
 * To be used as a wrapper class for when creating the ConfigRelaySet message.
 */
@SuppressWarnings({"unused"})
public final class ConfigRelaySet extends ConfigMessage {
    // Relay features
    public static final int RELAY_FEATURE_DISABLED = 0x00;
    public static final int RELAY_FEATURE_ENABLED = 0x01;
    public static final int RELAY_FEATURE_SUPPORTED = 0x02;

    private static final String TAG = ConfigRelaySet.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_RELAY_SET;

    private final int mRelay;
    private final int mRelayRetransmitCount;
    private final int mRelayRetransmitIntervalSteps;

    /**
     * Constructs a ConfigNetworkTransmitSet message.
     *
     * @param relay                        {@link RelaySettings.RelayState} to be set if the relay feature is supported by the node.
     * @param relayRetransmitCount         Relay retransmit count to be set
     * @param relayRetransmitIntervalSteps Relay Retransmit Interval Steps to be set
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public ConfigRelaySet(@RelaySettings.RelayState final int relay,
                          final int relayRetransmitCount,
                          final int relayRetransmitIntervalSteps) throws IllegalArgumentException {
        mRelay = relay;
        if (relayRetransmitCount < 0 || 0b111 < relayRetransmitCount) {
            throw new IllegalArgumentException("Network Transmit Count must be between 0 and 7 (inclusive)");
        }
        if (relayRetransmitIntervalSteps < 0 || 0b11111 < relayRetransmitIntervalSteps) {
            throw new IllegalArgumentException("Network Transmit Interval Steps must be between 0 and 31 (inclusive)");
        }
        this.mRelayRetransmitCount = relayRetransmitCount;
        this.mRelayRetransmitIntervalSteps = relayRetransmitIntervalSteps;
        assembleMessageParameters();
    }

    @Override
    final void assembleMessageParameters() {
        mParameters = new byte[]{
                (byte) mRelay,
                (byte) ((mRelayRetransmitIntervalSteps << 3) | mRelayRetransmitCount)
        };
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
        return mRelayRetransmitCount;
    }

    /**
     * Returns the Network Transmit Interval Steps set in this message
     *
     * @return Network Transmit Interval Steps
     */
    public int getNetworkTransmitIntervalSteps() {
        return mRelayRetransmitIntervalSteps;
    }
}
