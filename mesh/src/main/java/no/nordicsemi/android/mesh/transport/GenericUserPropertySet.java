package no.nordicsemi.android.mesh.transport;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.SecureUtils;

public class GenericUserPropertySet extends ApplicationMessage {

    private static final int PROPERTY_SET_PARAMS_LENGTH = 2;

    private final short propertyId;
    final byte[] values;

    public GenericUserPropertySet(@NonNull final ApplicationKey appKey, final short propertyId, final byte[] values) {
        super(appKey);
        this.propertyId = propertyId;
        this.values = values;
        assembleMessageParameters();
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        mParameters = ByteBuffer.allocate(PROPERTY_SET_PARAMS_LENGTH + values.length)
                .order(LITTLE_ENDIAN)
                .putShort(propertyId)
                .put(values)
                .array();
    }

    @Override
    public int getOpCode() {
        return ApplicationMessageOpCodes.GENERIC_USER_PROPERTY_SET;
    }
}
