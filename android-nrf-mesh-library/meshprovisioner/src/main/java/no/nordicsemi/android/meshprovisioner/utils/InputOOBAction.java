package no.nordicsemi.android.meshprovisioner.utils;

import android.util.Log;

import java.util.ArrayList;

/**
 * Input OOB Actions
 */
@SuppressWarnings("unused")
public enum InputOOBAction {

    /**
     * Output OOB Actions
     */
    NO_INPUT((short) 0x0000),
    PUSH((short) 0x0001),
    TWIST((short) 0x0002),
    INPUT_NUMERIC((short) 0x0004),
    INPUT_ALPHA_NUMERIC((short) 0x0008);

    private static final String TAG = InputOOBAction.class.getSimpleName();
    private short inputOOBAction;

    InputOOBAction(final short outputOOBAction) {
        this.inputOOBAction = outputOOBAction;
    }

    public short getInputOOBAction() {
        return inputOOBAction;
    }

    /**
     * Returns the oob method used for authentication
     *
     * @param method auth method used
     */
    public static InputOOBAction fromValue(final short method) {
        switch (method) {
            default:
            case 0x0000:
                return NO_INPUT;
            case 0x0001:
                return PUSH;
            case 0x0002:
                return TWIST;
            case 0x0008:
                return INPUT_NUMERIC;
            case 0x0010:
                return INPUT_ALPHA_NUMERIC;
        }
    }

    /**
     * Returns the Input OOB Action description
     *
     * @param type Input OOB type
     * @return Input OOB type descrption
     */
    public static String getInputOOBActionDescription(final InputOOBAction type) {
        switch (type) {
            case NO_INPUT:
                return "Not supported";
            case PUSH:
                return "Push";
            case TWIST:
                return "Twist";
            case INPUT_NUMERIC:
                return "Input Number";
            case INPUT_ALPHA_NUMERIC:
                return "Input Alpha Numeric";
            default:
                return "Unknown";
        }
    }

    public static ArrayList<InputOOBAction> parseInputActionsFromBitMask(final int inputAction) {
        final InputOOBAction[] inputActions = {PUSH, TWIST, INPUT_NUMERIC, INPUT_ALPHA_NUMERIC};
        final ArrayList<InputOOBAction> supportedActionValues = new ArrayList<>();
        for (InputOOBAction action : inputActions) {
            if ((inputAction & action.inputOOBAction) == action.inputOOBAction) {
                supportedActionValues.add(action);
                Log.v(TAG, "Input oob action type value: " + getInputOOBActionDescription(action));
            }
        }
        return supportedActionValues;
    }

    /**
     * Returns the Input OOB Action
     *
     * @param type input OOB type
     * @return Output OOB type description
     */
    public static int getInputOOBActionValue(final InputOOBAction type) {
        switch (type) {
            case PUSH:
                return 0;
            case TWIST:
                return 1;
            case INPUT_NUMERIC:
                return 2;
            case INPUT_ALPHA_NUMERIC:
                return 3;
            case NO_INPUT:
            default:
                return -1;
        }
    }

    /**
     * Generates the Input OOB Authentication value
     *
     * @param inputActionType selected {@link InputOOBAction}
     * @param input           Input authentication
     * @param inputOOBSize    Input OOB size
     * @return 128-bit authentication value
     */
    public static byte[] generateInputOOBAuthenticationValue(final InputOOBAction inputActionType, final byte[] input, final byte inputOOBSize) {
        switch (inputActionType) {
            case PUSH:
            case TWIST:
            case INPUT_NUMERIC:
                return MeshParserUtils.createAuthenticationValue(true, input, inputOOBSize);
            case INPUT_ALPHA_NUMERIC:
                return MeshParserUtils.createAuthenticationValue(false, input, inputOOBSize);
            default:
                return null;
        }
    }
}
