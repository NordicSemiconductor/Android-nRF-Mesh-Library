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

package no.nordicsemi.android.meshprovisioner.utils;

import android.util.Log;

public class ParseInputOOBActions {
    private static final String TAG = ParseInputOOBActions.class.getSimpleName();

    private static final short NO_INPUT = 0x0000;
    private static final short PUSH = 0x0001;
    private static final short TWIST = 0x0002;
    private static final short INPUT_NUMBER = 0x0004;
    private static final short INPUT_ALPHA_NUMBERIC = 0x0008;

    /**
     * Returns the Input OOB Action description
     *
     * @param type Input OOB type
     * @return Input OOB type descrption
     */
    public static String getInputOOBActionDescription(final short type) {
        switch (type) {
            case NO_INPUT:
                return "Not supported";
            case PUSH:
                return "Push";
            case TWIST:
                return "Twist";
            case INPUT_NUMBER:
                return "Input Number";
            case INPUT_ALPHA_NUMBERIC:
                return "Input Alpha Numeric";
            default:
                return "Unknown";
        }
    }

    public static void parseInputActionsFromBitMask(final int inputAction) {
        final byte[] inputActions = {PUSH, TWIST, INPUT_NUMBER, INPUT_ALPHA_NUMBERIC};
        for(byte action : inputActions){
            if((inputAction & action) == action){
                Log.v(TAG, "Input oob action type value: " + getInputOOBActionDescription(action));
            }
        }
    }

    /**
     * Parses the Input OOB Action
     *
     * @param type Input OOB type
     * @return Input OOB type descrption
     */
    public static int parseInputOOBActionValue(final int type) {
        switch (type) {
            case NO_INPUT:
                return 0;
            case PUSH:
                return 1;
            case TWIST:
                return 2;
            case INPUT_NUMBER:
                return 4;
            case INPUT_ALPHA_NUMBERIC:
                return 8;
            default:
                return -1;
        }
    }

    /**
     * Returns the Input OOB Action
     *
     * @param type input OOB type
     * @return Output OOB type description
     */
    public static int getOuputOOBActionValue(final short type) {
        switch (type) {
            case PUSH:
                return 0;
            case TWIST:
                return 1;
            case INPUT_NUMBER:
                return 2;
            case INPUT_ALPHA_NUMBERIC:
                return 3;
            default:
                return -1;
        }
    }
}
