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

import static no.nordicsemi.android.mesh.utils.Heartbeat.PERIOD_LOG_MIN;
import static no.nordicsemi.android.mesh.utils.Heartbeat.isValidHeartbeatPeriodLog;
import static no.nordicsemi.android.mesh.utils.MeshAddress.UNASSIGNED_ADDRESS;
import static no.nordicsemi.android.mesh.utils.MeshAddress.isValidHeartbeatSubscriptionDestination;
import static no.nordicsemi.android.mesh.utils.MeshAddress.isValidHeartbeatSubscriptionSource;

/**
 * ConfigHeartbeatSubscriptionSet message.
 */
public class ConfigHeartbeatSubscriptionSet extends ConfigMessage {

    private static final String TAG = ConfigHeartbeatSubscriptionSet.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_HEARTBEAT_SUBSCRIPTION_SET;
    private int srcAddress;
    private int dstAddress;
    private byte periodLog;

    /**
     * Constructs ConfigHeartbeatSubscriptionSet message. Use this constructor to clear Heartbeat Subscriptions.
     */
    public ConfigHeartbeatSubscriptionSet() throws IllegalArgumentException {
        this(UNASSIGNED_ADDRESS, UNASSIGNED_ADDRESS, (byte) PERIOD_LOG_MIN);
    }

    /**
     * Constructs ConfigHeartbeatSubscriptionSet message.
     *
     * @param srcAddress Source address identifies the Heartbeat Subscription Source,
     *                   where the address shall be an unassigned address or a unicast address.
     *                   All other values are prohibited.
     * @param dstAddress Destination address identifies the Heartbeat Subscription destination,
     *                   where the address shall be an unassigned address,
     *                   the primary unicast address of the node, or a group address,
     *                   all other values are Prohibited.
     * @param periodLog  Period for sending Heartbeat messages.
     * @throws IllegalArgumentException if any illegal arguments are passed.
     */
    public ConfigHeartbeatSubscriptionSet(final int srcAddress,
                                          final int dstAddress,
                                          final byte periodLog) throws IllegalArgumentException {
        if (isValidHeartbeatSubscriptionSource(srcAddress))
            this.srcAddress = srcAddress;
        if (isValidHeartbeatSubscriptionDestination(dstAddress))
            this.dstAddress = dstAddress;
        if (isValidHeartbeatPeriodLog(periodLog))
            this.periodLog = periodLog;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        Log.d(TAG, "Source address: " + Integer.toHexString(srcAddress));
        Log.d(TAG, "Destination address: " + Integer.toHexString(dstAddress));
        Log.d(TAG, "Period Log: " + Integer.toHexString(periodLog));
        final ByteBuffer paramsBuffer = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN);
        paramsBuffer.putShort((short) srcAddress);
        paramsBuffer.putShort((short) dstAddress);
        paramsBuffer.put(periodLog);
        mParameters = paramsBuffer.array();
    }
}
