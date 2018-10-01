package no.nordicsemi.android.meshprovisioner.messages;

import android.support.annotation.NonNull;

import no.nordicsemi.android.meshprovisioner.meshmessagestates.ProvisionedMeshNode;

abstract class GenericMessage extends MeshMessage {

    public static final int GENERIC_TRANSITION_STEP_0 = 0;
    public static final int GENERIC_TRANSITION_STEP_1 = 1;
    public static final int GENERIC_TRANSITION_STEP_2 = 2;
    public static final int GENERIC_TRANSITION_STEP_3 = 3;

    final int mAppKeyIndex;
    final byte[] mAppKey;
    byte mAid;

    GenericMessage(final ProvisionedMeshNode node, @NonNull final byte[] appKey, final int appKeyIndex, final int aszmic) {
        super(node, aszmic);
        if(appKey.length !=  16)
            throw new IllegalArgumentException("Application key must be 16 bytes");
        this.mAppKey = appKey;
        this.mAppKeyIndex = appKeyIndex;
    }


    @Override
    public final int getAkf() {
        return 1;
    }

    @Override
    public final int getAid() {
        return mAid;
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
