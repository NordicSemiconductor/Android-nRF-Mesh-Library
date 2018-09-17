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

import java.util.ArrayList;

public class ParseOutputOOBActions {
    private static final String TAG = ParseOutputOOBActions.class.getSimpleName();

    /**
     * Input OOB Actions
     */
    public static final short NO_OUTPUT = 0x0000;
    private static final short BLINK = 0x0001;
    private static final short BEEP = 0x0002;
    private static final short VIBRATE = 0x0004;
    private static final short OUTPUT_NUMERIC = 0x0008;
    private static final short OUTPUT_ALPHA_NUMERIC = 0x0010;

    /**
     * Returns the Output OOB Action description
     *
     * @param type Output OOB type
     * @return Input OOB type descrption
     */
    public static String getOuputOOBActionDescription(final short type) {
        switch (type) {
            case NO_OUTPUT:
                return "Not Supported";
            case BLINK:
                return "Blink";
            case BEEP:
                return "Beep";
            case VIBRATE:
                return "Vibrate";
            case OUTPUT_NUMERIC:
                return "Output Numeric";
            case OUTPUT_ALPHA_NUMERIC:
                return "Output Alpha Numeric";
            default:
                return "Unknown";
        }
    }

    /**
     * Parses the Output OOB Action
     *
     * @param type output OOB type
     * @return Output OOB type descrption
     */
    public static int parseOuputOOBActionValue(final int type) {
        switch (type) {
            case NO_OUTPUT:
                return 0;
            case BLINK:
                return 1;
            case BEEP:
                return 2;
            case VIBRATE:
                return 3;
            case OUTPUT_NUMERIC:
                return 4;
            case OUTPUT_ALPHA_NUMERIC:
                return 10;
            default:
                return -1;
        }
    }

    /**
     * Parses the output oob action value
     *
     * @param outputAction type of output action
     * @return selected output action type
     */
    public static void parseOutputActionsFromBitMask(final int outputAction) {
        final byte[] outputActions = {BLINK, BEEP, VIBRATE, OUTPUT_NUMERIC, OUTPUT_ALPHA_NUMERIC};
        final ArrayList<Byte> suppportedActionValues = new ArrayList<>();
        for(byte action : outputActions){
            if((outputAction & action) == action){
                suppportedActionValues.add(action);
                Log.v(TAG, "Supported output oob action type: " + getOuputOOBActionDescription(action));
            }
        }
    }

    /**
     * Selects the output oob action value
     *
     * @param outputAction type of output action
     * @return selected output action type
     */
    public static short selectOutputActionsFromBitMask(final int outputAction) {
        final byte[] outputActions = {BLINK, BEEP, VIBRATE, OUTPUT_NUMERIC, OUTPUT_ALPHA_NUMERIC};
        final ArrayList<Byte> suppportedActionValues = new ArrayList<>();
        for(byte action : outputActions){
            if((outputAction & action) == action){
                suppportedActionValues.add(action);
                Log.v(TAG, "Supported output oob action type: " + getOuputOOBActionDescription(action));
            }
        }

        if(!suppportedActionValues.isEmpty()) {
            return suppportedActionValues.get(0);
        } else {
            return NO_OUTPUT;
        }
    }

    /**
     * Returns the Output OOB Action
     *
     * @param type output OOB type
     * @return Output OOB type descrption
     */
    public static int getOuputOOBActionValue(final short type) {
        switch (type) {
            case BLINK:
                return 0;
            case BEEP:
                return 1;
            case VIBRATE:
                return 2;
            case OUTPUT_NUMERIC:
                return 3;
            case OUTPUT_ALPHA_NUMERIC:
                return 4;
            default:
                return 0;
        }
    }
}
