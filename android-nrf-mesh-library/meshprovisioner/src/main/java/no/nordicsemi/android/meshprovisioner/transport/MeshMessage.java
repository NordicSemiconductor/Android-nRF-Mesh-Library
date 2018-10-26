package no.nordicsemi.android.meshprovisioner.transport;

import android.support.annotation.NonNull;

/**
 * Abstract wrapper class for mesh message.
 */
public abstract class MeshMessage {

    final ProvisionedMeshNode mNode;
    private final int mAszmic;
    byte[] mParameters;
    AccessMessage mMessage;

    MeshMessage(final ProvisionedMeshNode node, final int aszmic) {
        this.mNode = node;
        if (aszmic != 1 && aszmic != 0)
            throw new IllegalArgumentException("Application size message integrity check (aszmic) can only be 0 or 1");
        this.mAszmic = 0; //Currently the library defaults to 0
    }

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
     * Returns the mesh node this message must be sent to.
     *
     * @return provisioned mesh node
     */
    public final ProvisionedMeshNode getMeshNode(){
        return mNode;
    }

    /**
     * Returns the size of message integrity check used for this message.
     *
     * @return aszmic
     */
    public final int getAszmic() {
        return mAszmic;
    }

    /**
     * Set the access message
     * @param message access message
     */
    void setMessage(@NonNull final AccessMessage message){
        mMessage = message;
    }

    AccessMessage getMessage(){
        return mMessage;
    }
}
