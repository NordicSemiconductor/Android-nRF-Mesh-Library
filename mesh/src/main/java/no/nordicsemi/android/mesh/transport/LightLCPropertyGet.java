package no.nordicsemi.android.mesh.transport;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.sensorutils.DeviceProperty;

/**
 * LightLCPropertyGet
 */
@SuppressWarnings("unused")
public class LightLCPropertyGet extends ApplicationMessage {

    private static final String TAG = LightLCPropertyGet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.LIGHT_LC_PROPERTY_GET;
    private final DeviceProperty property;

    /**
     * Constructs LightLCPropertyGet message.
     *
     * @param appKey {@link ApplicationKey} key for this message
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public LightLCPropertyGet(@NonNull final ApplicationKey appKey, @NonNull final DeviceProperty property) throws IllegalArgumentException {
        super(appKey);
        this.property = property;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mAid = (byte) mAppKey.getAid();
        mParameters = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(property.getPropertyId()).array();
    }
}
