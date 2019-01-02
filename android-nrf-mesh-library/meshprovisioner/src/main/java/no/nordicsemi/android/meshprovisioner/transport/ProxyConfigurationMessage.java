package no.nordicsemi.android.meshprovisioner.transport;

import android.support.annotation.NonNull;

/**
 * Abstract wrapper class for mesh message.
 */
public abstract class ProxyConfigurationMessage {

    byte[] mParameters;
    protected ControlMessage mMessage;

    ProxyConfigurationMessage() {
    }

    /**
     * Returns the opCode of this message
     *
     * @return opcode
     */
    abstract int getOpCode();

    /**
     * Set the access message
     * @param message access message
     */
    void setMessage(@NonNull final ControlMessage message){
        mMessage = message;
    }

    /**
     * Returns the message
     */
    public ControlMessage getMessage(){
        return mMessage;
    }

    /**
     * Returns the source address of the message
     */
    public byte[] getSrc(){
        return mMessage.getSrc();
    }

    /**
     * Returns the destination address of the message
     */
    public byte[] getDst(){
        return mMessage.getDst();
    }

}
