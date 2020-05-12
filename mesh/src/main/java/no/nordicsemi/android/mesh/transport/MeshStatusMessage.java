package no.nordicsemi.android.mesh.transport;

/**
 * Abstract wrapper class for mesh message.
 */
public abstract class MeshStatusMessage {

    final ProvisionedMeshNode mNode;
    final int mOpCode;
    final byte[] mParameters;

    MeshStatusMessage(final ProvisionedMeshNode node, final int opCode, final byte[] parameters) {
        this.mNode = node;
        this.mOpCode = opCode;
        this.mParameters = parameters;
    }

    /**
     * Returns the mesh node this message must be sent to.
     *
     * @return provisioned mesh node
     */
    public abstract ProvisionedMeshNode getMeshNode();

    /**
     * Returns the opcode for this message.
     *
     * @return opcode
     */
    public abstract int getOpCode();

    /**
     * Returns the parameters of this message.
     *
     * @return parameters
     */
    public abstract byte[] getParameters();
}
