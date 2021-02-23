package no.nordicsemi.android.mesh.transport;

import androidx.annotation.NonNull;

public abstract class SensorStatusMessage extends ApplicationStatusMessage {
    SensorStatusMessage(@NonNull final AccessMessage message) {
        super(message);
    }
}
