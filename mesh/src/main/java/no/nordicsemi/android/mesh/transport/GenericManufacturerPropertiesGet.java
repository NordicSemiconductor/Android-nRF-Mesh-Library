package no.nordicsemi.android.mesh.transport;

import androidx.annotation.NonNull;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.sensorutils.DeviceProperty;
import no.nordicsemi.android.mesh.utils.SecureUtils;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;

public class GenericManufacturerPropertiesGet extends ApplicationMessage {

    private static final String TAG = GenericManufacturerPropertiesGet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.GENERIC_MANUFACTURER_PROPERTIES_GET;

    /**
     * Constructs GenericManufacturerPropertiesGet message.
     *
     * @param appKey {@link ApplicationKey} key for this message
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public GenericManufacturerPropertiesGet(@NonNull final ApplicationKey appKey) throws IllegalArgumentException {
        super(appKey);
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
    }
}
