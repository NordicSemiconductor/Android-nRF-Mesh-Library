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
