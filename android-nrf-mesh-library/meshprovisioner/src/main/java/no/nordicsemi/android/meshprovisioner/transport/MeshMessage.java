package no.nordicsemi.android.meshprovisioner.transport;

import androidx.annotation.NonNull;

/**
 * Abstract wrapper class for mesh message.
 */
public abstract class MeshMessage {

    private final int mAszmic = 0;
    protected Message mMessage;
    byte[] mParameters;

    /**
     * Returns the application key flag used for this message.
     *
     * @return application key flag
     */
    abstract int getAkf();

    /**
     * Returns application key identifier used for this message.
     *
     * @return application key identifier
     */
    abstract int getAid();

    /**
     * Returns the opCode of this message
     *
     * @return opcode
     */
    abstract int getOpCode();

    /**
     * Returns the parameters of this message.
     *
     * @return parameters
     */
    abstract byte[] getParameters();

    /**
     * Returns the size of message integrity check used for this message.
     *
     * @return aszmic
     */
    public final int getAszmic() {
        return mAszmic;
    }

    /**
     * Returns the message
     */
    public Message getMessage() {
        return mMessage;
    }

    /**
     * Set the access message
     *
     * @param message access message
     */
    void setMessage(@NonNull final Message message) {
        mMessage = message;
    }

    /**
     * Returns the source address of the message
     */
    public int getSrc() {
        return mMessage.getSrc();
    }

    /**
     * Returns the destination address of the message
     */
    public int getDst() {
        return mMessage.getDst();
    }

}
