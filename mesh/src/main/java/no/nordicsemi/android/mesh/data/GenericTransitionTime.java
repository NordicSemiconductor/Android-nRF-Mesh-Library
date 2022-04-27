package no.nordicsemi.android.mesh.data;

import java.nio.ByteBuffer;
import java.util.Objects;

import no.nordicsemi.android.mesh.utils.BitReader;

/**
 * Transition time (see Section 3.1.3 inside the specification)
 */
public class GenericTransitionTime {

    public static final int TRANSITION_TIME_BITS_LENGTH = TransitionStep.TRANSITION_NUMBER_STEP_BITS_LENGTH + TransitionResolution.TRANSITION_STEP_RESOLUTION_BITS_LENGTH;

    public final TransitionResolution resolution;
    public final TransitionStep transitionStep;

    public GenericTransitionTime(TransitionResolution transitionResolution, TransitionStep transitionSteps) {
        this.resolution = transitionResolution;
        this.transitionStep = transitionSteps;
    }

    public GenericTransitionTime(int value) {
        this.resolution = TransitionResolution.fromValue((value >> TransitionStep.TRANSITION_NUMBER_STEP_BITS_LENGTH) & 0x03);
        this.transitionStep = TransitionStep.Specific(value & 0x3F);
    }

    public int getValue() {
        return resolution.value << TransitionStep.TRANSITION_NUMBER_STEP_BITS_LENGTH | transitionStep.value;
    }

    /**
     * @return Time in milliseconds given any resolution or -1 if resolution is not set.
     */
    public long toMilliseconds() {
        switch (resolution) {
            case HUNDRED_MILLISECONDS:
                return 100L * transitionStep.value;
            case SECOND:
                return 1000L * transitionStep.value;
            case TEN_SECONDS:
                return 10000L * transitionStep.value;
            case TEN_MINUTES:
                 return 10L * 60 * 1000 * transitionStep.value;
            default:
                return -1;
        }
    }

    public static final class TransitionStep {

        public static final int TRANSITION_NUMBER_STEP_BITS_LENGTH = 6;
        public final int value;

        /**
         * The Generic Default Transition Time is immediate.
         */
        public static TransitionStep Immediate = new TransitionStep(0x00);

        /**
         * The number of steps.
         */
        public static TransitionStep Specific(int numberOfSteps) {
            return new TransitionStep(numberOfSteps);
        }

        /**
         * The value is unknown. The state cannot be set to this value, but an element 0x3F may report an unknown value if a transition is higher than 0x3E or not
         * determined.
         */
        public static TransitionStep Unknown = new TransitionStep(0x3F);

        TransitionStep(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TransitionStep that = (TransitionStep) o;
            return value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    public enum TransitionResolution {
        HUNDRED_MILLISECONDS(0b00),
        SECOND(0b01),
        TEN_SECONDS(0b10),
        TEN_MINUTES(0b11);

        public static final int TRANSITION_STEP_RESOLUTION_BITS_LENGTH = 2;

        public int value;

        public static TransitionResolution fromValue(int value) {
            for (TransitionResolution resolution : values()) {
                if (resolution.value == value) {
                    return resolution;
                }
            }
            return SECOND;
        }

        TransitionResolution(int value) {
            this.value = value;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericTransitionTime that = (GenericTransitionTime) o;
        return resolution == that.resolution && transitionStep.equals(that.transitionStep);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resolution, transitionStep);
    }
}
