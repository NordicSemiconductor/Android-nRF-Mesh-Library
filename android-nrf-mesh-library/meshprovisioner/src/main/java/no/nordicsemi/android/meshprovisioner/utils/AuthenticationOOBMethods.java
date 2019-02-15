package no.nordicsemi.android.meshprovisioner.utils;

public enum AuthenticationOOBMethods {
    NO_OOB_AUTHENTICATION((short) 0),
    STATIC_OOB_AUTHENTICATION((short) 1),
    OUTPUT_OOB_AUTHENTICATION((short) 2),
    INPUT_OOB_AUTHENTICATION((short) 3);

    private short authenticationMethod;

    AuthenticationOOBMethods(final short authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

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
