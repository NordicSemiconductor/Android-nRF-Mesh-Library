package no.nordicsemi.android.mesh.transport;



import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.SecureUtils;

/**
 * To be used as a wrapper class when creating a MagicLevelGet message.
 */
@SuppressWarnings("unused")
public class MagicLevelGet extends ApplicationMessage {

    private static final String TAG = MagicLevelGet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.MAGIC_LEVEL_GET;
    private static final int MAGIC_LEVEL_GET_PARAMS_LENGTH = 8;

    private final int mIO;
    private final int mIndex;
    private final Integer mCorrelation;
    private final int tId;

    /**
     * Constructs MagicLevelGet message.
     *
     * @param appKey               {@link ApplicationKey} key for this message
     * @param io                    Target IO
     * @param index                 Configuration data index
     * @param correlation           Correlation to link request / response
     * @param tId                  Transaction id
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public MagicLevelGet(final ApplicationKey appKey,
                         final int io,
                         final int index,
                         final int correlation,
                         final int tId) throws IllegalArgumentException {
        super(appKey);
        this.mIO = io;
        this.mIndex = index;
        this.tId = tId;
        this.mCorrelation = correlation;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        final ByteBuffer paramsBuffer;
        paramsBuffer = ByteBuffer.allocate(MAGIC_LEVEL_GET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        paramsBuffer.put((byte) mIO);
        paramsBuffer.putShort((short) (mIndex));
        paramsBuffer.putInt(mCorrelation);
        paramsBuffer.put((byte) tId);
        mParameters = paramsBuffer.array();
    }
}