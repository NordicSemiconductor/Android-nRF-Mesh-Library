package no.nordicsemi.android.meshprovisioner.transport;

/**
 * Abstract wrapper class for mesh message.
 */
abstract class ProxyConfigMessage extends ProxyConfigurationMessage {

    ProxyConfigMessage() {
    }

    /**
     * Creates the parameters for a given mesh message.
     */
    abstract void assembleMessageParameters();

    /**
     * Returns the parameters of this message.
     *
     * @return parameters
     */
    abstract byte[] getParameters();

}
