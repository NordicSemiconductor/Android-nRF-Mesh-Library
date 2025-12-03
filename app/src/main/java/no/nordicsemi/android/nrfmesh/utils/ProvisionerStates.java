package no.nordicsemi.android.nrfmesh.utils;

public enum ProvisionerStates {

    PROVISIONING_INVITE(0),
    PROVISIONING_CAPABILITIES(1),
    PROVISIONING_START(2),
    PROVISIONING_PUBLIC_KEY_SENT(3),
    PROVISIONING_PUBLIC_KEY_WAITING(4),
    PROVISIONING_PUBLIC_KEY_RECEIVED(5),
    PROVISIONING_AUTHENTICATION_INPUT_OOB_WAITING(6),
    PROVISIONING_AUTHENTICATION_OUTPUT_OOB_WAITING(7),
    PROVISIONING_AUTHENTICATION_STATIC_OOB_WAITING(8),
    PROVISIONING_AUTHENTICATION_INPUT_ENTERED(9),
    PROVISIONING_INPUT_COMPLETE(10),
    PROVISIONING_CONFIRMATION_SENT(11),
    PROVISIONING_CONFIRMATION_RECEIVED(12),
    PROVISIONING_RANDOM_SENT(13),
    PROVISIONING_RANDOM_RECEIVED(14),
    PROVISIONING_DATA_SENT(15),
    PROVISIONING_COMPLETE(16),
    PROVISIONING_FAILED(17),
    COMPOSITION_DATA_GET_SENT(18),
    COMPOSITION_DATA_STATUS_RECEIVED(19),
    SENDING_DEFAULT_TTL_GET(20),
    DEFAULT_TTL_STATUS_RECEIVED(21),
    SENDING_APP_KEY_ADD(22),
    APP_KEY_STATUS_RECEIVED(23),
    SENDING_BLOCK_ACKNOWLEDGEMENT(98),
    BLOCK_ACKNOWLEDGEMENT_RECEIVED(99),
    PROVISIONER_UNASSIGNED(100);

    private final int state;

    ProvisionerStates(final int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public static ProvisionerStates fromStatusCode(final int statusCode) {
        for (ProvisionerStates state : ProvisionerStates.values()) {
            if (state.getState() == statusCode) {
                return state;
            }
        }
        throw new IllegalStateException("Invalid state");
    }
}
