package no.nordicsemi.android.meshprovisioner.transport;

public interface UpperTransportLayerCallbacks {

    /**
     * Gets the application key that was use to encrypt the message
     *
     * @return application key
     */
    byte[] getApplicationKey();

}
