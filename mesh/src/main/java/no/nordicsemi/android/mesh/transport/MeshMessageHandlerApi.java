package no.nordicsemi.android.mesh.transport;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Mesh message handler api
 */
@SuppressWarnings("unused")
interface MeshMessageHandlerApi {

    /**
     * Sends a mesh message specified within the {@link MeshMessage} object
     *
     * @param label       Label UUID for destination address
     * @param meshMessage {@link MeshMessage} Mesh message containing the message opcode and message parameters
     */
    void createMeshMessage(final int src, final int dst, @Nullable final UUID label, @NonNull final MeshMessage meshMessage);
}
