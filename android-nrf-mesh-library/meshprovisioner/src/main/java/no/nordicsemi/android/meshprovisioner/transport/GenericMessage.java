package no.nordicsemi.android.meshprovisioner.transport;

import android.support.annotation.NonNull;

abstract class GenericMessage extends MeshMessage {

    public static final int GENERIC_TRANSITION_STEP_0 = 0;
    public static final int GENERIC_TRANSITION_STEP_1 = 1;
    public static final int GENERIC_TRANSITION_STEP_2 = 2;
    public static final int GENERIC_TRANSITION_STEP_3 = 3;

    final byte[] mAppKey;
    byte mAid;

    /**
     * Constracts a generic message
     *
     * @param appKey application key
     */
    GenericMessage(@NonNull final byte[] appKey) {
        if (appKey.length != 16)
            throw new IllegalArgumentException("Application key must be 16 bytes");
        this.mAppKey = appKey;
    }

    @Override
    public final int getAkf() {
        return 1;
    }

    @Override
    public final int getAid() {
        return mAid;
    }

    /**
     * Returns the app key used in this message.
     *
     * @return app key
     */
    public final byte[] getAppKey() {
        return mAppKey;
    }

    @Override
    public final byte[] getParameters() {
        return mParameters;
    }

    /**
     * Creates the parameters for a given mesh message.
     */
    abstract void assembleMessageParameters();
}
