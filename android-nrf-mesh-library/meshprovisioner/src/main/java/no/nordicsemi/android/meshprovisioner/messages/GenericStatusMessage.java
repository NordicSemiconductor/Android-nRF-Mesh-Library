package no.nordicsemi.android.meshprovisioner.messages;

import android.support.annotation.NonNull;

import no.nordicsemi.android.meshprovisioner.meshmessagestates.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.messagetypes.AccessMessage;

import static no.nordicsemi.android.meshprovisioner.messages.ConfigStatusMessage.StatusCodeNames.fromStatusCode;

public abstract class GenericStatusMessage extends MeshMessage {

    AccessMessage mMessage;

    public GenericStatusMessage(@NonNull final ProvisionedMeshNode node, @NonNull final AccessMessage message) {
        this(node, message.getAszmic());
        mMessage = message;
    }

    private GenericStatusMessage(final ProvisionedMeshNode node, final int aszmic) {
        super(node, aszmic);
    }

    /**
     * Parses the status parameters returned by a status message
     */
    abstract void parseStatusParameters();

    @Override
    public final int getAkf() {
        return 1;
    }

    @Override
    public final int getAid() {
        return mMessage.getAid();
    }

    @Override
    public final byte[] getParameters() {
        return mParameters;
    }
}
