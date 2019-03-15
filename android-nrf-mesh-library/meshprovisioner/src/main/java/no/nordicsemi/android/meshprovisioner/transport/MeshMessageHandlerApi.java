package no.nordicsemi.android.meshprovisioner.transport;

import android.support.annotation.NonNull;

/**
 * Mesh message handler api
 */
@SuppressWarnings("unused")
interface MeshMessageHandlerApi {

    /**
     * Sends a mesh message specified within the {@link MeshMessage} object
     *
     * @param meshMessage {@link MeshMessage} Mesh message containing the message opcode and message parameters
     */
    void sendMeshMessage(final int src, final int dst, @NonNull final MeshMessage meshMessage);
}
