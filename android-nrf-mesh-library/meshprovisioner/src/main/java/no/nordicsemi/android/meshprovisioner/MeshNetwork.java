
package no.nordicsemi.android.meshprovisioner;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.room.Entity;
import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
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

    public List<Group> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    void setGroups(final List<Group> groups) {
        this.groups = groups;
    }

    /**
     * Returns the next unicast address for a provisioner based on the allocated range and the number of elements
     *
     * @param elementCount Element count
     * @param provisioner  provisioner
     */
    public int nextAvailableUnicastAddress(final int elementCount, @NonNull final Provisioner provisioner) {
        Collections.sort(nodes, nodeComparator);
        // Iterate through all nodes just once, while iterating over ranges.
        int index = 0;
        for (AllocatedUnicastRange range : provisioner.getAllocatedUnicastRanges()) {
            // Start from the beginning of the current range.
            int address = range.getLowAddress();

            // Iterate through nodes that weren't checked yet.
            int currentIndex = index;
            for (int i = currentIndex; i < nodes.size(); i++) {
                final ProvisionedMeshNode node = nodes.get(i);
                index += i;
                final int lastUnicastInNode = node.getUnicastAddress() + (node.getNumberOfElements() - 1);

                // Skip nodes with addresses below the range.
                if (address > lastUnicastInNode) {
                    continue;
                }

                // If we found a space before the current node, return the address.
                if (node.getUnicastAddress() > address + (elementCount - 1)) {
                    return address;
                }

                // Else, move the address to the next available address.
                address = lastUnicastInNode + 1;

                // If the new address is outside of the range, go to the next one.
                if (range.highAddress < address + (elementCount - 1)) {
                    break;
                }
            }

            if (range.getHighAddress() >= address + (elementCount - 1)) {
                return address;
            }
        }

        // No address was found :(
        return -1;
    }

    /**
     * Returns the next available group  address for a provisioner based on the allocated group range
     *
     * @param provisioner {@link Provisioner}
     * @return Group address
     * @throws IllegalStateException if there is no allocated group range to this provisioner
     */
    public Integer nextAvailableGroupAddress(@NonNull final Provisioner provisioner) throws IllegalStateException {
        if (provisioner.getAllocatedGroupRanges().isEmpty()) {
            throw new IllegalStateException("Provisioner has no group range allocated.");
        }

        Collections.sort(groups, groupComparator);
        for (AllocatedGroupRange range : provisioner.getAllocatedGroupRanges()) {
            //If the list of groups are empty we can start with the lowest address of the range
            if (groups.isEmpty()) {
                return range.getLowAddress();
            }

            for (int address = range.lowAddress; address < range.getHighAddress(); address++) {
                //if the address is not in use, return it as the next available address to create a group
                if (!isGroupAddressInUse(address)) {
                    return address;
                }
            }
        }
        return null;
    }

    private boolean isGroupAddressInUse(final int address) {
        for (Group group : groups) {
            //if the address is not in use, return it as the next available address to create a group
            if (group.getAddress() == address) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a group using the next available group address based on the provisioners allocated group range
     *
     * @param provisioner provisioner
     * @return a group or null if creation failed
     */
    public Group createGroup(@NonNull final Provisioner provisioner) {
        final Integer address = nextAvailableGroupAddress(provisioner);
        if (address != null) {
            return new Group(address, meshUUID);
        }
        return null;
    }

    /**
     * Creates a group using the next available group address based on the provisioners allocated group range
     *
     * @param addressLabel UUID label
     * @return a group or null if creation failed
     */
    public Group createGroup(@NonNull final UUID addressLabel, @Nullable final UUID parentLabel) {
        final int address = MeshAddress.generateVirtualAddress(addressLabel);
        return new Group(addressLabel, parentLabel, meshUUID);
    }

    /**
     * Creates with a given address and name.
     *
     * @param address Address of the group which must be within the allocated range
     * @param name    Friendly name of the group
     * @return true if the group was successfully added and false otherwise since a group may already exist with the same group address
     * @throws IllegalArgumentException if there is no group range allocated or if the address is out of the range allocated to the provisioner
     */
    public Group createGroup(@NonNull final Provisioner provisioner, final int address, @NonNull final String name) throws IllegalArgumentException {
        if (MeshAddress.isValidVirtualAddress(address)) {
            throw new IllegalArgumentException("Call addGroup(@NonNull final Group group) to create a group with group address label");
        }

        if (provisioner.getAllocatedGroupRanges().isEmpty()) {
            throw new IllegalArgumentException("Unable to create group," +
                    " there is no group range allocated to the current provisioner");
        }

        for (AllocatedGroupRange range : provisioner.getAllocatedGroupRanges()) {
            if (range.getLowAddress() > address || range.getHighAddress() < address) {
                throw new IllegalArgumentException("Unable to create group, " +
                        "the address is outside the range allocated to the provisioner");
            }
        }

        final Group group = new Group(address, meshUUID);
        if (!TextUtils.isEmpty(name))
            group.setName(name);
        return group;
    }

    /**
     * Adds a group to the existing group list within the network
     *
     * @param group to be added
     * @return true if the group was successfully added and false otherwise since a group may already exist with the same group address
     * @throws IllegalArgumentException if there is no group range allocated or if the address is out of the range allocated to the provisioner
     */
    public boolean addGroup(@NonNull final Group group) throws IllegalArgumentException {

        final Provisioner provisioner = getSelectedProvisioner();
        if (provisioner.getAllocatedGroupRanges().isEmpty()) {
            throw new IllegalArgumentException("Unable to create group," +
                    " there is no group range allocated to the current provisioner");
        }

        //We check if the group is made of a virtual address
        if (group.getAddressLabel() == null) {
            for (AllocatedGroupRange range : provisioner.getAllocatedGroupRanges()) {
                if (range.getLowAddress() > group.getAddress() || range.getHighAddress() < group.getAddress()) {
                    throw new IllegalArgumentException("Unable to create group, " +
                            "the address is outside the range allocated to the provisioner");
                }
            }
        }
        return saveGroup(group);
    }

    private boolean saveGroup(@NonNull final Group group) {
        if (!isGroupExist(group)) {
            this.groups.add(group);
            notifyGroupAdded(group);
            return true;
        }
        throw new IllegalArgumentException("Group already exists");
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
            notifyGroupUpdated(group);
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
            notifyGroupDeleted(group);
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