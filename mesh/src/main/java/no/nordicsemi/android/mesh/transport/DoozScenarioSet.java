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
 * To be used as a wrapper class when creating a DoozScenarioSet message.
 */
@SuppressWarnings("unused")
public class DoozScenarioSet extends ApplicationMessage {

    private static final String TAG = DoozScenarioSet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.DOOZ_SCENARIO_SET;
    private static final int DOOZ_SCENARIO_SET_PARAMS_LENGTH = 14;

    private final int mScenarioId;
    private final int mCommand;
    private final int mIO;
    private final boolean mIsActive;
    private final int mUnused;
    private final int mValue;
    private final int mTransition;
    private final int mStartAt;
    private final int mDuration;
    private final int mDaysInWeek;
    private final int mCorrelation;
    private final Integer mExtra;
    private final int tId;

    /**
     * Constructs DoozScenarioSet message.
     *
     * @param appKey               {@link ApplicationKey} key for this message
     * @param scenarioId           The id for the scenario to add/modify
     * @param command              The command of this message (0: add/modify scenario, 1: read scenario data, 4: start scenario, 6: remove a scenario)
     * @param io                   Target IO
     * @param isActive             Whether the scenario should be active
     * @param unused               RFU
     * @param value                Value to use for the scenario
     * @param transition           The transition to apply when starting the scenario
     * @param startAt              The start at hour (0x7F for never)
     * @param duration             The duration that this scenario should have (0x7F for no duration)
     * @param daysInWeek           The days on which this scenario should be played (0x7F for never)
     * @param correlation          Correlation to link request / response
     * @param extra                RFU
     * @param tId                  Transaction id
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public DoozScenarioSet(@NonNull final ApplicationKey appKey,
                         final int scenarioId,
                         final int command,
                         final int io,
                         final boolean isActive,
                         final int unused,
                         final int value,
                         final int transition,
                         final int startAt,
                         final int duration,
                         final int daysInWeek,
                         final int correlation,
                         @Nullable final Integer extra,
                         final int tId) throws IllegalArgumentException {
        super(appKey);
        this.mScenarioId = scenarioId;
        this.mCommand = command;
        this.mIO = io;
        this.mIsActive = isActive;
        this.mUnused = unused;
        this.mValue = value;
        this.mTransition = transition;
        this.mStartAt = startAt;
        this.mDuration = duration;
        this.mDaysInWeek = daysInWeek;
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
        paramsBuffer = ByteBuffer.allocate(DOOZ_SCENARIO_SET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        paramsBuffer.put((byte) tId);
        paramsBuffer.putShort((short) mScenarioId);
        paramsBuffer.putShort((short) mCommand);
        paramsBuffer.putShort((short) mIO);
        paramsBuffer.putShort((short) (mIsActive ? 0b01 : 0b00));
        paramsBuffer.putShort((short) (mUnused));
        paramsBuffer.put((byte) mValue);
        paramsBuffer.put((byte) mTransition);
        paramsBuffer.put((byte) mStartAt);
        paramsBuffer.put((byte) mDuration);
        paramsBuffer.put((byte) mDaysInWeek);
        paramsBuffer.putInt(mCorrelation);
        if(null != mExtra) {
            paramsBuffer.putShort(mExtra.shortValue());
        }
        mParameters = paramsBuffer.array();
    }
}