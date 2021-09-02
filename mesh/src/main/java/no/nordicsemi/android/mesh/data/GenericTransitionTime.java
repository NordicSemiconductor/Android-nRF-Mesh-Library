package no.nordicsemi.android.mesh.data;

import java.nio.ByteBuffer;

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
        byte[] bytes = ByteBuffer.allocate(TRANSITION_TIME_BITS_LENGTH).putInt(value).array();
        final BitReader bitReader = new BitReader(bytes);
        this.resolution = TransitionResolution.fromValue(bitReader.getBits(TransitionResolution.TRANSITION_STEP_RESOLUTION_BITS_LENGTH));
        this.transitionStep = TransitionStep.Specific(bitReader.getBits(TransitionStep.TRANSITION_NUMBER_STEP_BITS_LENGTH));
    }

    public int getValue() {
        return (resolution.value << 6 | transitionStep.value);
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
}
