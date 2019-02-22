package no.nordicsemi.android.meshprovisioner.utils;

/**
 * OOB Authentication methods
 */
@SuppressWarnings("unused")
public enum AuthenticationOOBMethods {
    //Selecting one of the authentication methods defined below during the provisioning process will require the user to,

    // - Nothing
    NO_OOB_AUTHENTICATION((short) 0),
    // - enter a 16-bit value provided by the device manufacturer to be entered during hte provisioning process
    STATIC_OOB_AUTHENTICATION((short) 1),
    // - enter the number of times the device blinked, beeped, vibrated, displayed or an alphanumeric value displayed by the device
    OUTPUT_OOB_AUTHENTICATION((short) 2),
    // - push, twist, input a number or an alpha numeric value displayed on the provisioner app
    INPUT_OOB_AUTHENTICATION((short) 3);

    private short authenticationMethod;

    AuthenticationOOBMethods(final short authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    /**
     * Returns the authentication method
     */
    public short getAuthenticationMethod() {
        return authenticationMethod;
    }

    /**
     * Parses the authentication method used.
     */
    public static String parseStaticOOBMethodInformation(final AuthenticationOOBMethods type) {
        switch (type) {
            case NO_OOB_AUTHENTICATION:
                return "No OOB authentication is used";
            case STATIC_OOB_AUTHENTICATION:
                return "Static OOB authentication is used";
            case OUTPUT_OOB_AUTHENTICATION:
                return "Output OOB authentication is used";
            case INPUT_OOB_AUTHENTICATION:
                return "Input OOB authentication is used";
            default:
                return "Prohibited";
        }
    }
}
