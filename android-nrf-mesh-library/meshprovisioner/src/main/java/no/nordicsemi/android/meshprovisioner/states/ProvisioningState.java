package no.nordicsemi.android.meshprovisioner.states;

public abstract class ProvisioningState {

    static final byte TYPE_PROVISIONING_INVITE = 0x00;
    static final byte TYPE_PROVISIONING_CAPABILITIES = 0x01;
    static final byte TYPE_PROVISIONING_START = 0x02;
    static final byte TYPE_PROVISIONING_PUBLIC_KEY = 0x03;
    static final byte TYPE_PROVISIONING_INPUT_COMPLETE = 0x04;
    static final byte TYPE_PROVISIONING_CONFIRMATION = 0x05;
    static final byte TYPE_PROVISIONING_RANDOM_CONFIRMATION = 0x06;
    static final byte TYPE_PROVISIONING_DATA = 0x07;
    static final byte TYPE_PROVISIONING_COMPLETE = 0x08;

    protected String error;

    public ProvisioningState() {
    }

    public abstract State getState();

    public abstract void executeSend();

    public abstract boolean parseData(final byte[] data);

    public String getError() {
        return error;
    }

    public enum State {
        PROVISIONING_INVITE(0), PROVISIONING_CAPABILITIES(1), PROVISIONING_START(2), PROVISIONING_PUBLIC_KEY(3),
        PROVISINING_INPUT_COMPLETE(4), PROVISIONING_CONFIRMATION(5), PROVISINING_RANDOM(6),
        PROVISINING_DATA(7), PROVISINING_COMPLETE(8), PROVISINING_FAILED(9);


        private int state;


        State(final int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }

    }
}