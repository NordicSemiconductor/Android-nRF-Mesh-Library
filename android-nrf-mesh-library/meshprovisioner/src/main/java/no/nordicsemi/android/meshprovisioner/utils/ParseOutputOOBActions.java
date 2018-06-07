package no.nordicsemi.android.meshprovisioner.utils;

public class ParseOutputOOBActions {

    /**
     * Input OOB Actions
     */
    protected static final int NO_OUTPUT = 0x0000;
    protected static final int BLINK = 0x0001;
    protected static final int BEEP = 0x0002;
    protected static final int VIBRATE = 0x0004;
    protected static final int OUTPUT_NUMERIC = 0x0008;
    protected static final int OUTPUT_ALPHA_NUMERIC = 0x0010;

    /**
     * Returns the Output OOB Action description
     *
     * @param type Output OOB type
     * @return Input OOB type descrption
     */
    public static String getOuputOOBActionDescription(final int type) {
        switch (type) {
            case NO_OUTPUT:
                return "No Input";
            case BLINK:
                return "Blink";
            case BEEP:
                return "Beep";
            case VIBRATE:
                return "Vibrate";
            case OUTPUT_NUMERIC:
                return "Output numeric";
            case OUTPUT_ALPHA_NUMERIC:
                return "Output alpha numeric";
            default:
                return "Unknown";
        }
    }

    /**
     * Parses the Output OOB Action
     *
     * @param type output OOB type
     * @return Output OOB type descrption
     */
    public static int parseOuputOOBActionValue(final int type) {
        switch (type) {
            case NO_OUTPUT:
                return 0;
            case BLINK:
                return 1;
            case BEEP:
                return 2;
            case VIBRATE:
                return 3;
            case OUTPUT_NUMERIC:
                return 4;
            case OUTPUT_ALPHA_NUMERIC:
                return 10;
            default:
                return -1;
        }
    }

    /**
     * Returns the Output OOB Action
     *
     * @param type output OOB type
     * @return Output OOB type descrption
     */
    public static int getOuputOOBActionValue(final int type) {
        switch (type) {
            case BLINK:
                return 0;
            case BEEP:
                return 1;
            case VIBRATE:
                return 2;
            case OUTPUT_NUMERIC:
                return 3;
            case OUTPUT_ALPHA_NUMERIC:
                return 4;
            default:
                return 0;
        }
    }
}
