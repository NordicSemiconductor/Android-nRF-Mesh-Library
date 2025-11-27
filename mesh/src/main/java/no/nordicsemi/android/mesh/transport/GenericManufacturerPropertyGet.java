package no.nordicsemi.android.mesh.transport;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.sensorutils.DeviceProperty;
import no.nordicsemi.android.mesh.utils.SecureUtils;

public class GenericManufacturerPropertyGet extends ApplicationMessage {

    private static final String TAG = GenericManufacturerPropertyGet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.GENERIC_MANUFACTURER_PROPERTY_GET;

    private final DeviceProperty property;

    /**
     * Constructs GenericManufacturerPropertiesGet message.
     *
     * @param appKey {@link ApplicationKey} key for this message
     * @param property  {@link DeviceProperty} device property.
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public GenericManufacturerPropertyGet(@NonNull final ApplicationKey appKey, @NonNull final DeviceProperty property) throws IllegalArgumentException {
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
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        mParameters = ByteBuffer.allocate(2).order(LITTLE_ENDIAN)
                .putShort(property.getPropertyId())
                .array();
    }
}
