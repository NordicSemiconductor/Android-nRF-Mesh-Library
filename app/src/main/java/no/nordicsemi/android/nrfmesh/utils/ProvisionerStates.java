package no.nordicsemi.android.nrfmesh.utils;

public enum ProvisionerStates {

    PROVISIONING_INVITE(0),
    PROVISIONING_CAPABILITIES(1),
    PROVISIONING_START(2),
    PROVISIONING_PUBLIC_KEY_SENT(3),
    PROVISIONING_PUBLIC_KEY_RECEIVED(4),
    PROVISIONING_AUTHENTICATION_INPUT_OOB_WAITING(5),
    PROVISIONING_AUTHENTICATION_OUTPUT_OOB_WAITING(6),
    PROVISIONING_AUTHENTICATION_STATIC_OOB_WAITING(7),
    PROVISIONING_AUTHENTICATION_INPUT_ENTERED(8),
    PROVISIONING_INPUT_COMPLETE(9),
    PROVISIONING_CONFIRMATION_SENT(10),
    PROVISIONING_CONFIRMATION_RECEIVED(11),
    PROVISIONING_RANDOM_SENT(12),
    PROVISIONING_RANDOM_RECEIVED(13),
    PROVISIONING_DATA_SENT(14),
    PROVISIONING_COMPLETE(15),
    PROVISIONING_FAILED(16),
    COMPOSITION_DATA_GET_SENT(17),
    COMPOSITION_DATA_STATUS_RECEIVED(18),
    SENDING_DEFAULT_TTL_GET(19),
    DEFAULT_TTL_STATUS_RECEIVED(20),
    SENDING_APP_KEY_ADD(21),
    APP_KEY_STATUS_RECEIVED(22),
    SENDING_NETWORK_TRANSMIT_SET(23),
    NETWORK_TRANSMIT_STATUS_RECEIVED(24),
    SENDING_BLOCK_ACKNOWLEDGEMENT(98),
    BLOCK_ACKNOWLEDGEMENT_RECEIVED(99),
    PROVISIONER_UNASSIGNED(100);

    private int state;

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
