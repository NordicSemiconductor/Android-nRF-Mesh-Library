
package no.nordicsemi.android.meshprovisioner;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.room.Entity;
import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.AddressUtils;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

@SuppressWarnings({"WeakerAccess", "unused", "UnusedReturnValue"})
@Entity(tableName = "mesh_network")
public final class MeshNetwork extends BaseMeshNetwork {

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public MeshNetwork(final String meshUUID) {
        super(meshUUID);
    }

    void setCallbacks(final MeshNetworkCallbacks mCallbacks) {
        this.mCallbacks = mCallbacks;
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

    /**
     * Returns the name of the mesh network
     */
    public String getMeshName() {
        return meshName;
    }

    /**
     * Sets the name of the mesh network
     *
     * @param meshName name
     */
    public void setMeshName(String meshName) {
        this.meshName = meshName;
        notifyNetworkUpdated();
    }

    /**
     * Returns the time stamp of th e mesh network
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the time stamp
     *
     * @param timestamp timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    void setProvisioners(List<Provisioner> provisioners) {
        this.provisioners = provisioners;
    }

    /**
     * Returns the list of {@link ProvisionedMeshNode}
     */
    public List<ProvisionedMeshNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    /**
     * Sets the list of {@link ProvisionedMeshNode}
     *
     * @param nodes list of {@link ProvisionedMeshNode}
     */
    void setNodes(@NonNull List<ProvisionedMeshNode> nodes) {
        this.nodes = nodes;
    }

    public List<Group> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    void setGroups(final List<Group> groups) {
        this.groups = groups;
    }

    /**
     * Adds a group to the existing group list within the network
     *
     * @param group to be added
     * @return true if the group was successfully added and false otherwise since a group may already exist with the same group address
     */
    public boolean addGroup(@NonNull final Group group) {
        if (!isGroupExist(group)) {
            this.groups.add(group);
            if (mCallbacks != null) {
                mCallbacks.onGroupAdded(group);
            }
            return true;
        }
        return false;
    }

    /**
     * Adds a group to the existing group list within the network
     *
     * @param address Address of the group
     * @param name    Friendly name of the group
     * @return true if the group was successfully added and false otherwise since a group may already exist with the same group address
     */
    public boolean addGroup(final int address, @NonNull final String name) {
        final Group group = new Group(address, meshUUID);
        if (!TextUtils.isEmpty(name))
            group.setName(name);

        if (!isGroupExist(group)) {
            this.groups.add(group);
            if (mCallbacks != null) {
                mCallbacks.onGroupAdded(group);
            }
            return true;
        }
        return false;
    }

    public Group getGroup(final int address) {
        for (final Group group : groups) {
            if (address == group.getAddress()) {
                return group;
            }
        }
        return null;
    }

    /**
     * Updates a group in the mesh network
     *
     * @param group group to be updated
     */
    public boolean updateGroup(@NonNull final Group group) {
        if (isGroupExist(group)) {
            if (mCallbacks != null) {
                mCallbacks.onGroupUpdated(group);
            }
            return true;
        }
        return false;
    }

    /**
     * Removes a group from the mesh network
     *
     * @param group group to be removed
     */
    public boolean removeGroup(@NonNull final Group group) {
        if (groups.remove(group)) {
            if (mCallbacks != null) {
                mCallbacks.onGroupDeleted(group);
            }
            return true;
        }
        return false;
    }

    /**
     * Returns true if a group exists with the same address
     *
     * @param address Group address
     */
    public boolean isGroupExist(final int address) {
        for (final Group group : groups) {
            if (address == group.getAddress()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if a group exists with the given group. This is checked against the group address.
     *
     * @param group Group to check
     */
    public boolean isGroupExist(@NonNull final Group group) {
        for (final Group grp : groups) {
            if (group.getAddress() == grp.getAddress()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of elements assigned to a particular group
     *
     * @param group group
     */
    public List<Element> getElements(final Group group) {
        final List<Element> elements = new ArrayList<>();
        for (final ProvisionedMeshNode node : nodes) {
            for (Map.Entry<Integer, Element> elementEntry : node.getElements().entrySet()) {
                final Element element = elementEntry.getValue();
                for (Map.Entry<Integer, MeshModel> modelEntry : element.getMeshModels().entrySet()) {
                    final MeshModel model = modelEntry.getValue();
                    if (model != null) {
                        final List<Integer> subscriptionAddresses = model.getSubscribedAddresses();
                        for (Integer subscriptionAddress : subscriptionAddresses) {
                            if (group.getAddress() == subscriptionAddress) {
                                if (!elements.contains(element))
                                    elements.add(element);
                            }
                        }
                    }
                }
            }
        }
        return elements;
    }

    /**
     * Returns a list of models assigned to a particular group
     *
     * @param group group
     */
    public List<MeshModel> getModels(final Group group) {
        final List<MeshModel> models = new ArrayList<>();
        for (final ProvisionedMeshNode node : nodes) {
            for (Map.Entry<Integer, Element> elementEntry : node.getElements().entrySet()) {
                final Element element = elementEntry.getValue();
                for (Map.Entry<Integer, MeshModel> modelEntry : element.getMeshModels().entrySet()) {
                    final MeshModel model = modelEntry.getValue();
                    if (model != null) {
                        final List<Integer> subscriptionAddresses = model.getSubscribedAddresses();
                        for (Integer subscriptionAddress : subscriptionAddresses) {
                            if (group.getAddress() == subscriptionAddress) {
                                if (!models.contains(model))
                                    models.add(model);
                            }
                        }
                    }
                }
            }
        }
        return models;
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

    /**
     * Returns a list of {@link NetworkKey} belonging to the mesh network
     */
    public List<NetworkKey> getNetKeys() {
        return netKeys;
    }

    public NetworkKey getPrimaryNetworkKey() {
        for (NetworkKey networkKey : netKeys) {
            if (networkKey.getKeyIndex() == 0) {
                return networkKey;
            }
        }

        final NetworkKey networkKey = new NetworkKey(0, MeshParserUtils.toByteArray(SecureUtils.generateRandomNetworkKey()));
        netKeys.add(networkKey);
        return networkKey;
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
    public ProvisionedMeshNode getProvisionedNode(@NonNull final byte[] unicastAddress) {
        for (ProvisionedMeshNode node : nodes) {
            if (node.hasUnicastAddress(AddressUtils.getUnicastAddressInt(unicastAddress))) {
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
            if (meshNode.getUnicastAddress() == node.getUnicastAddress()) {
                nodes.remove(node);
                notifyNodeDeleted(meshNode);
                return true;
            }
        }
        return false;
    }

    boolean deleteResetNode(final ProvisionedMeshNode meshNode) {
        for (ProvisionedMeshNode node : nodes) {
            if (meshNode.getUnicastAddress() == node.getUnicastAddress()) {
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