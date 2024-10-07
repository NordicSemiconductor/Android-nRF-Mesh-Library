package no.nordicsemi.android.mesh.transport;



import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.SecureUtils;

/**
 * To be used as a wrapper class when creating a DoozEpochSet message.
 */
@SuppressWarnings("unused")
public class DoozEpochSet extends ApplicationMessage {

    private static final String TAG = DoozEpochSet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.DOOZ_EPOCH_SET;
    private static final int DOOZ_EPOCH_SET_PARAMS_LENGTH = 13;

    private final int mPacked;
    private final int mEpoch;
    private final int mCorrelation;
    private final Integer mExtra;
    private final int tId;

    /**
     * Constructs DoozEpochSet message.
     *
     * @param appKey               {@link ApplicationKey} key for this message
     * @param packed               A bitmap containing the time zone, the command (2: read current epoch time and timezone, 8: update epoch and timezone only if it's greater than the device, 15: override epoch and timezone) and the io of this message
     * @param epoch                The current Epoch
     * @param correlation          Correlation to link request / response
     * @param extra                RFU
     * @param tId                  Transaction id
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public DoozEpochSet(@NonNull final ApplicationKey appKey,
                         final int packed,
                         final int epoch,
                         final int correlation,
                         @Nullable final Integer extra,
                         final int tId) throws IllegalArgumentException {
        super(appKey);
        this.mPacked = packed;
        this.mEpoch = epoch;
        this.mCorrelation = correlation;
        this.mExtra = extra;
        this.tId = tId;
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
        paramsBuffer = ByteBuffer.allocate(DOOZ_EPOCH_SET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        paramsBuffer.put((byte) tId);
        Log.v(TAG, "mPacked: " + mPacked + " (" + Integer.toString(mPacked, 2) + ")");
        Log.v(TAG, "mEpoch: " + mEpoch);
        Log.v(TAG, "mCorrelation: " + mCorrelation);
        paramsBuffer.putShort((short) mPacked);
        paramsBuffer.putInt(mEpoch);
        paramsBuffer.putInt(mCorrelation);
        if(null != mExtra) {
            paramsBuffer.putShort(mExtra.shortValue());
            Log.v(TAG, "mExtra: " + mExtra);
        }
        mParameters = paramsBuffer.array();
    }
}