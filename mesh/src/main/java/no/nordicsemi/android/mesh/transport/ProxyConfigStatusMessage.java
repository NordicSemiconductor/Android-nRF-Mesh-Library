package no.nordicsemi.android.mesh.transport;

import androidx.annotation.NonNull;

/**
 * Abstract wrapper class for mesh message.
 */
abstract class ProxyConfigStatusMessage extends MeshMessage {

    ProxyConfigStatusMessage(@NonNull final ControlMessage message) {
        mMessage = message;
    }

    /**
     * Parses the status parameters returned by a status message
     */
    abstract void parseStatusParameters();

    @Override
    int getAid() {
        return -1;
    }

    @Override
    int getAkf() {
        return -1;
    }

}
