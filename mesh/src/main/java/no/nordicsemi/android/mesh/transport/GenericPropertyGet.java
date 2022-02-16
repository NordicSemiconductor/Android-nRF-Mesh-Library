package no.nordicsemi.android.mesh.transport;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.sensorutils.DeviceProperty;
import no.nordicsemi.android.mesh.utils.SecureUtils;

public class GenericPropertyGet extends ApplicationMessage {

    private final int opCode;
    private final short propertyId;

    /**
     * Constructs SensorSettingsGet message.
     *
     * @param appKey     {@link ApplicationKey} Key for this message.
     * @param propertyId property ID.
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public GenericPropertyGet(@NonNull final int opcode, @NonNull final ApplicationKey appKey, final short propertyId) {
        super(appKey);
        this.opCode = opcode;
        this.propertyId = propertyId;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return opCode;
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        mParameters = ByteBuffer.allocate(2).order(LITTLE_ENDIAN).putShort(propertyId).array();
    }

    public short getPropertyId() {
        return propertyId;
    }
}
