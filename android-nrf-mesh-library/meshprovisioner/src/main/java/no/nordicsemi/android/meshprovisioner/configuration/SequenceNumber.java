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

package no.nordicsemi.android.meshprovisioner.configuration;

import android.content.Context;
import android.content.SharedPreferences;

public final class SequenceNumber {

    private static final String PREFS_SEQUENCE_NUMBER = "PREFS_SEQUENCE_NUMBER";
    private static final String KEY = "NRF_MESH_SEQUENCE_NUMBER";

    private static Integer mSequenceNumber;

    //TODO check this class
    private static void initSequenceNumber(final Context context) {
        if (mSequenceNumber == null) {
            final SharedPreferences preferences = context.getSharedPreferences(PREFS_SEQUENCE_NUMBER, Context.MODE_PRIVATE);
            if(preferences == null) { // Unit test started fail being unable to fetch preferences
                mSequenceNumber = 0;
            } else {
                mSequenceNumber = preferences.getInt(KEY, 0);
            }
        }
    }

    public static int getSequenceNumber() {
        return mSequenceNumber;
    }

    static int incrementAndStore(final Context context) {
        if(mSequenceNumber == null)
            initSequenceNumber(context);

        mSequenceNumber++;
        SharedPreferences preferences = context.getSharedPreferences(PREFS_SEQUENCE_NUMBER, Context.MODE_PRIVATE);
        if(preferences != null) { // Unit test started fail being unable to fetch preferences
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(KEY, mSequenceNumber);
            editor.apply();
        }
        return mSequenceNumber;
    }

    static int incrementAndStore(final Context context, final byte[] sequenceNumber) {
        final int tempSeq = getSequenceNumber(sequenceNumber) + 1; //Increment it here
        mSequenceNumber = tempSeq;
        final SharedPreferences preferences = context.getSharedPreferences(PREFS_SEQUENCE_NUMBER, Context.MODE_PRIVATE);
        if(preferences != null) { // Unit test started fail being unable to fetch preferences
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(KEY, tempSeq);
            editor.apply();
        }
        return mSequenceNumber;
    }

    private static int getSequenceNumber(final byte[] sequenceNumber) {
        return (((sequenceNumber[0] & 0xFF) << 16) | ((sequenceNumber[1] & 0xFF) << 8) | (sequenceNumber[2] & 0xFF));
    }

    public static void resetSequenceNumber(final Context context) {
        mSequenceNumber = 0;
        final SharedPreferences preferences = context.getSharedPreferences(PREFS_SEQUENCE_NUMBER, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY, mSequenceNumber);
        editor.apply();
    }
}
