package no.nordicsemi.android.mesh.transport;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.SecureUtils;

public class GenericAdminPropertySet extends ApplicationMessage {

    private static final int PROPERTY_SET_PARAMS_LENGTH = 3;

    private final short propertyId;
    private final byte userAccess;
    final byte[] values;

    public GenericAdminPropertySet(@NonNull final ApplicationKey appKey, final short propertyId, final byte userAccess, final byte[] values) {
        super(appKey);
        this.propertyId = propertyId;
        this.userAccess = userAccess;
        this.values = values;
        assembleMessageParameters();
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        mParameters = ByteBuffer.allocate(PROPERTY_SET_PARAMS_LENGTH + values.length)
                .order(LITTLE_ENDIAN)
                .putShort(propertyId)
                .put(userAccess)
                .put(values)
                .array();
    }

    @Override
    public int getOpCode() {
        return ApplicationMessageOpCodes.GENERIC_ADMIN_PROPERTY_SET;
    }
}
