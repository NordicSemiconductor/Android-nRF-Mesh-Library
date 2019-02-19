
package no.nordicsemi.android.meshprovisioner;

import androidx.room.Entity;
import androidx.annotation.RestrictTo;

import java.util.Collections;
import java.util.List;

import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;

@SuppressWarnings({"WeakerAccess", "unused"})
@Entity(tableName = "mesh_network")
public final class MeshNetwork extends BaseMeshNetwork {

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public MeshNetwork(final String meshUUID) {
        super(meshUUID);
    }

    void setCallbacks(final MeshNetworkCallbacks mCallbacks) {
        this.mCallbacks = mCallbacks;
    }

    public final List<ProvisionedMeshNode> getProvisionedNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public void setIvIndex(final int ivIndex) {
        this.ivIndex = ivIndex;
        notifyNetworkUpdated();
    }

    public int getIvIndex() {
        return ivIndex;
    }

    public String getSchema() {
        return schema;
    }

    void setSchema(String schema) {
        this.schema = schema;
    }

    public String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    void setVersion(String version) {
        this.version = version;
    }

    public String getMeshUUID() {
        return meshUUID;
    }

    public String getMeshName() {
        return meshName;
    }

    public void setMeshName(String meshName) {
        this.meshName = meshName;
        notifyNetworkUpdated();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    void setProvisioners(List<Provisioner> provisioners) {
        this.provisioners = provisioners;
    }

    public List<ProvisionedMeshNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    void setNodes(List<ProvisionedMeshNode> nodes) {
        this.nodes = nodes;
    }

    public List<Group> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<Scene> getScenes() {
        return scenes;
    }

    void setScenes(List<Scene> scenes) {
        this.scenes = scenes;
    }

    public boolean isLastSelected() {
        return lastSelected;
    }

    public void setLastSelected(final boolean lastSelected) {
        this.lastSelected = lastSelected;
    }

    public List<Provisioner> getProvisioners() {
        return Collections.unmodifiableList(provisioners);
    }

    public List<NetworkKey> getNetKeys() {
        return netKeys;
    }

    public NetworkKey getPrimaryNetworkKey() {
        for (NetworkKey networkKey : netKeys) {
            if (networkKey.getKeyIndex() == 0) {
                return networkKey;
            }
        }

        return null;
    }

    void setNetKeys(List<NetworkKey> netKeys) {
        this.netKeys = netKeys;
    }

    public List<ApplicationKey> getAppKeys() {
        return Collections.unmodifiableList(appKeys);
    }

    void setAppKeys(List<ApplicationKey> appKeys) {
        this.appKeys = appKeys;
    }

    /**
     * Returns the mesh node with the corresponding unicast address
     *
     * @param unicastAddress unicast address of the node
     */
    public ProvisionedMeshNode getProvisionedNode(final byte[] unicastAddress) {
        for (ProvisionedMeshNode node : nodes) {
            if (node.hasUnicastAddress(unicastAddress)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Returns the mesh node with the corresponding unicast address
     *
     * @param unicastAddress unicast address of the node
     */
    public ProvisionedMeshNode getProvisionedNode(final int unicastAddress) {
        for (ProvisionedMeshNode node : nodes) {
            if (node.hasUnicastAddress(unicastAddress)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Deletes a mesh node from the list of provisioned nodes
     *
     * <p>
     * Note that deleting a node manually will not reset the node, but only be deleted from the stored list of provisioned nodes.
     * However you may still be able to connect to the same node, if it was not reset since the network may still exist. This
     * would be useful to in case if a node was manually reset and needs to be removed from the mesh network/db
     * </p>
     *
     * @param meshNode node to be deleted
     * @return true if deleted and false otherwise
     */
    public boolean deleteNode(final ProvisionedMeshNode meshNode) {
        for (ProvisionedMeshNode node : nodes) {
            if (meshNode.getUnicastAddressInt() == node.getUnicastAddressInt()) {
                nodes.remove(node);
                notifyNodeDeleted(meshNode);
                return true;
            }
        }
        return false;
    }

    boolean deleteResetNode(final ProvisionedMeshNode meshNode) {
        for (ProvisionedMeshNode node : nodes) {
            if (meshNode.getUnicastAddressInt() == node.getUnicastAddressInt()) {
                nodes.remove(node);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the current iv update state {@link IvUpdateStates}
     *
     * @return 1 if iv update is active or 0 otherwise
     */
    @IvUpdateStates
    public int getIvUpdateState() {
        return ivUpdateState;
    }

    /**
     * Sets the iv update state.
     * <p>
     * This is not currently supported by the library
     * </p>
     *
     * @param ivUpdateState 0 if normal operation and 1 if iv update is active
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setIvUpdateState(@IvUpdateStates final int ivUpdateState) {
        this.ivUpdateState = ivUpdateState;
    }

    /**
     * Returns the provisioning flags
     */
    public final int getProvisioningFlags() {
        int flags = 0;
        if (getPrimaryNetworkKey().getPhase() == NetworkKey.PHASE_2) {
            flags |= 1 << 7;
        }

        if (ivUpdateState == IV_UPDATE_ACTIVE) {
            flags |= 1 << 6;
        }

        return flags;
    }
}