package no.nordicsemi.android.meshprovisioner.utils;

public class ParseInputOOBActions {

    private static final int NO_INPUT = 0x00;
    private static final int PUSH = 0x01;
    private static final int TWIST = 0x02;
    private static final int INPUT_NUMBER = 0x04;
    private static final int INPUT_ALPHA_NUMBERIC = 0x08;

    /**
     * Returns the Input OOB Action description
     *
     * @param type Input OOB type
     * @return Input OOB type descrption
     */
    public static String getInputOOBActionDescription(final int type) {
        switch (type) {
            case NO_INPUT:
                return "No Input";
            case PUSH:
                return "Push";
            case TWIST:
                return "Twist";
            case INPUT_NUMBER:
                return "Input number";
            case INPUT_ALPHA_NUMBERIC:
                return "Input alpha numeric";
            default:
                return "Unknown";
        }
    }

    /**
     * Parses the Input OOB Action
     *
     * @param type Input OOB type
     * @return Input OOB type descrption
     */
    public static int parseInputOOBActionValue(final int type) {
        switch (type) {
            case NO_INPUT:
                return 0;
            case PUSH:
                return 1;
            case TWIST:
                return 2;
            case INPUT_NUMBER:
                return 4;
            case INPUT_ALPHA_NUMBERIC:
                return 8;
            default:
                return -1;
        }
    }

    /**
     * Returns the Input OOB Action
     *
     * @param type input OOB type
     * @return Output OOB type description
     */
    public static int getOuputOOBActionValue(final int type) {
        switch (type) {
            case PUSH:
                return 0;
            case TWIST:
                return 1;
            case INPUT_NUMBER:
                return 2;
            case INPUT_ALPHA_NUMBERIC:
                return 3;
            default:
                return -1;
        }
    }
}
