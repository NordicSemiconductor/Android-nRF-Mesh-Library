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

package no.nordicsemi.android.mesh.provisionerstates;

import android.content.Context;
import androidx.annotation.NonNull;

import no.nordicsemi.android.mesh.R;

public class ProvisioningFailedState extends ProvisioningState {

    private int error;

    public ProvisioningFailedState() {
        super();
    }

    @Override
    public State getState() {
        return State.PROVISIONING_FAILED;
    }

    @Override
    public void executeSend() {

    }

    @Override
    public boolean parseData(@NonNull final byte[] data) {
        error = data[2];

        return true;
    }

    public int getErrorCode() {
        return error;
    }

    public static String parseProvisioningFailure(final Context context, final int errorCode) {
        switch (ProvisioningFailureCode.fromErrorCode(errorCode)) {
            case PROHIBITED:
                return context.getString(R.string.error_prohibited);
            case INVALID_PDU:
                return context.getString(R.string.error_invalid_pdu);
            case INVALID_FORMAT:
                return context.getString(R.string.error_invalid_format);
            case UNEXPECTED_PDU:
                return context.getString(R.string.error_prohibited);
            case CONFIRMATION_FAILED:
                return context.getString(R.string.error_confirmation_failed);
            case OUT_OF_RESOURCES:
                return context.getString(R.string.error_prohibited);
            case DECRYPTION_FAILED:
                return context.getString(R.string.error_decryption_failed);
            case UNEXPECTED_ERROR:
                return context.getString(R.string.error_unexpected_error);
            case CANNOT_ASSIGN_ADDRESSES:
                return context.getString(R.string.error_cannot_assign_addresses);
            case UNKNOWN_ERROR_CODE:
            default:
                return context.getString(R.string.error_rfu);
        }
    }

    public enum ProvisioningFailureCode {
        PROHIBITED(0x00),
        INVALID_PDU(0x01),
        INVALID_FORMAT(0x02),
        UNEXPECTED_PDU(0x03),
        CONFIRMATION_FAILED(0x04),
        OUT_OF_RESOURCES(0x05),
        DECRYPTION_FAILED(0x06),
        UNEXPECTED_ERROR(0x07),
        CANNOT_ASSIGN_ADDRESSES(0x08),
        UNKNOWN_ERROR_CODE(0x09);

        private final int errorCode;

        ProvisioningFailureCode(final int errorCode) {
            this.errorCode = errorCode;
        }

        public static ProvisioningFailureCode fromErrorCode(final int errorCode) {
            for (ProvisioningFailureCode failureCode : ProvisioningFailureCode.values()) {
                if (failureCode.getErrorCode() == errorCode) {
                    return failureCode;
                }
            }
            return UNKNOWN_ERROR_CODE;
            //throw new RuntimeException("Enum not found");
        }

        public final int getErrorCode() {
            return errorCode;
        }
    }

}
