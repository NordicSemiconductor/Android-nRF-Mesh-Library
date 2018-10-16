package no.nordicsemi.android.meshprovisioner.message;

import android.support.annotation.NonNull;

import no.nordicsemi.android.meshprovisioner.message.type.AccessMessage;

public abstract class ConfigMessage extends MeshMessage {

    private AccessMessage mMessage;

    ConfigMessage(final ProvisionedMeshNode node, final int aszmic) {
        super(node, aszmic);
    }

    /**
     * Creates the parameters for a given mesh message.
     */
    abstract void assembleMessageParameters();

    @Override
    public final int getAkf() {
        return 0;
    }

    @Override
    public final int getAid() {
        return 0;
    }

    @Override
    public final byte[] getParameters() {
        return mParameters;
    }
}
