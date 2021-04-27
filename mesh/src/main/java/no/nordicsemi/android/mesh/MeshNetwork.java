
package no.nordicsemi.android.mesh;

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
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.MeshAddress;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
@Entity(tableName = "mesh_network")
public final class MeshNetwork extends BaseMeshNetwork {

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public MeshNetwork(final String meshUUID) {
        super(meshUUID);
    }

    void setCallbacks(final MeshNetworkCallbacks mCallbacks) {
        this.mCallbacks = mCallbacks;
    }

    /**
     * Returns true if the configuration contains full information about the mesh network, or false otherwise
     *
     * <p>
     * In a partial configuration may not have all the network keys, application keys, provisioners,
     * nodes(some nodes may contain their device keys and some may not).
     * Hence when shared a user will gain only limited access to the given mesh network.
     * </p>
     */
    public boolean isPartial() {
        return partial;
    }

    /**
     * Set the partial configuration flag to true. To be used internally
     *
     * @param partial true if the network is set to be exported partially.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setPartial(final boolean partial) {
        this.partial = partial;
    }

    public void setIvIndex(final IvIndex ivIndex) {
        this.ivIndex = ivIndex;
        notifyNetworkUpdated();
    }

    public IvIndex getIvIndex() {
        return ivIndex;
    }

    public String getSchema() {
        return schema;
    }

    public String getId() {
        return id;
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
        return groups;
    }

    void setGroups(final List<Group> groups) {
        this.groups = groups;
    }

    /**
     * Returns the next unicast address for a node based on the number of elements
     * and the range allocated to the provisioner.
     * P.S. When setting up a new network don't forget to assign an address to the provisioner.
     * When importing a network make sure to create a new provisioner with a different address
     * which is the recommended approach. However you can also use the same provisioner
     * with a different address.
     *
     * @param elementCount Element count
     * @param provisioner  provisioner
     * @return Allocated unicast address or -1 if none
     * @throws IllegalArgumentException if there is no allocated unicast range to the provisioner
     */
    public int nextAvailableUnicastAddress(final int elementCount, @NonNull final Provisioner provisioner) throws IllegalArgumentException {
        if (provisioner.getAllocatedUnicastRanges().isEmpty()) {
            throw new IllegalArgumentException("Please allocate a unicast address range to the provisioner");
        }

        // Populate all addresses that are currently in use
        final ArrayList<Integer> usedAddresses = new ArrayList<>();
        for (ProvisionedMeshNode node : nodes) {
            usedAddresses.addAll(node.getElements().keySet());
        }
        // Excluded addresses with the current IvIndex and current IvIndex - 1 must be considered as addresses in use.
        if (networkExclusions.get(ivIndex.getIvIndex()) != null)
            usedAddresses.addAll(networkExclusions.get(ivIndex.getIvIndex()));
        if (networkExclusions.get(ivIndex.getIvIndex() - 1) != null)
            usedAddresses.addAll(networkExclusions.get(ivIndex.getIvIndex() - 1));

        Collections.sort(usedAddresses);
        // Iterate through all nodes just once, while iterating over ranges.
        int index = 0;
        for (AllocatedUnicastRange range : provisioner.getAllocatedUnicastRanges()) {
            // Start from the beginning of the current range.
            int address = range.getLowAddress();

            // Iterate through nodes that weren't checked yet in essence the used addresses which include the excluded adresses
            for (int usedAddress : usedAddresses) {

                // Skip nodes with addresses below the range.
                if (address > usedAddress) {
                    continue;
                }

                // If we found a space before the current node, return the address.
                if (usedAddress > (address + (elementCount - 1))) {
                    return address;
                }

                // Else, move the address to the next available address.
                address = usedAddress + 1;

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
     * Returns the next unicast address for a provisioner based on the allocated range and the number of elements
     *
     * @param rangeSize Element count
     */
    public AllocatedUnicastRange nextAvailableUnicastAddressRange(final int rangeSize) {
        final List<AllocatedUnicastRange> ranges = new ArrayList<>();
        for (Provisioner provisioner : provisioners) {
            ranges.addAll(provisioner.getAllocatedUnicastRanges());
        }
        Collections.sort(ranges, unicastRangeComparator);
        return getNextAvailableUnicastRange(rangeSize,
                new AllocatedUnicastRange(MeshAddress.START_UNICAST_ADDRESS, MeshAddress.END_UNICAST_ADDRESS), ranges);
    }

    /**
     * Returns the next unicast address for a provisioner based on the allocated range and the number of elements
     *
     * @param rangeSize Range size
     */
    public AllocatedGroupRange nextAvailableGroupAddressRange(final int rangeSize) {
        final List<AllocatedGroupRange> ranges = new ArrayList<>();
        for (Provisioner provisioner : provisioners) {
            ranges.addAll(provisioner.getAllocatedGroupRanges());
        }
        Collections.sort(ranges, groupRangeComparator);
        return getNextAvailableGroupRange(rangeSize,
                new AllocatedGroupRange(MeshAddress.START_GROUP_ADDRESS, MeshAddress.END_GROUP_ADDRESS), ranges);
    }

    /**
     * Returns the next available scene range for a given size
     *
     * @param rangeSize Range size
     */
    @Nullable
    public AllocatedSceneRange nextAvailableSceneAddressRange(final int rangeSize) {
        final List<AllocatedSceneRange> ranges = new ArrayList<>();
        for (Provisioner provisioner : provisioners) {
            ranges.addAll(provisioner.getAllocatedSceneRanges());
        }
        Collections.sort(ranges, sceneRangeComparator);
        return getNextAvailableSceneRange(rangeSize, new AllocatedSceneRange(0x0001, 0xFFFF), ranges);
    }

    @Nullable
    private AllocatedUnicastRange getNextAvailableUnicastRange(final int size,
                                                               @NonNull final AllocatedUnicastRange bound,
                                                               @NonNull final List<AllocatedUnicastRange> ranges) {
        AllocatedUnicastRange bestRange = null;
        int lastUpperBound = bound.lowAddress - 1;

        // Go through all ranges looking for a gaps.
        for (AllocatedUnicastRange range : ranges) {
            // If there is a space available before this range, return it.
            if (lastUpperBound + size < range.lowAddress) {
                return new AllocatedUnicastRange(lastUpperBound + 1, lastUpperBound + size);
            }

            // If the space exists, but it's not as big as requested, compare
            // it with the best range so far and replace if it's bigger.
            if (range.lowAddress - lastUpperBound > 1) {
                final AllocatedUnicastRange newRange = new AllocatedUnicastRange(lastUpperBound + 1, range.lowAddress - 1);
                if (bestRange == null || newRange.range() > bestRange.range()) {
                    bestRange = newRange;
                }
            }
            lastUpperBound = range.highAddress;
        }

        // If if we didn't return earlier, check after the last range.
        if (lastUpperBound + size < bound.highAddress) {
            return new AllocatedUnicastRange(lastUpperBound + 1, lastUpperBound + size - 1);
        }
        // The gap of requested size hasn't been found. Return the best found.
        return bestRange;
    }

    @Nullable
    private AllocatedGroupRange getNextAvailableGroupRange(final int size,
                                                           @NonNull final AllocatedGroupRange bound,
                                                           @NonNull final List<AllocatedGroupRange> ranges) {
        AllocatedGroupRange bestRange = null;
        int lastUpperBound = bound.lowAddress - 1;

        // Go through all ranges looking for a gaps.
        for (AllocatedGroupRange range : ranges) {
            if (lastUpperBound + size < range.lowAddress) {
                return new AllocatedGroupRange(lastUpperBound + 1, lastUpperBound + size);
            }

            // If the space exists, but it's not as big as requested, compare
            // it with the best range so far and replace if it's bigger.
            if (range.lowAddress - lastUpperBound > 1) {
                final AllocatedGroupRange newRange = new AllocatedGroupRange(lastUpperBound + 1, range.lowAddress - 1);
                if (bestRange == null || newRange.range() > bestRange.range()) {
                    bestRange = newRange;
                }
            }
            lastUpperBound = range.highAddress;
        }

        // If if we didn't return earlier, check after the last range.
        if (lastUpperBound + size < bound.highAddress) {
            return new AllocatedGroupRange(lastUpperBound + 1, lastUpperBound + size - 1);
        }
        // The gap of requested size hasn't been found. Return the best found.
        return bestRange;
    }

    @Nullable
    private AllocatedSceneRange getNextAvailableSceneRange(final int size,
                                                           @NonNull final AllocatedSceneRange bound,
                                                           @NonNull final List<AllocatedSceneRange> ranges) {
        AllocatedSceneRange bestRange = null;
        int lastUpperBound = bound.getFirstScene() - 1;

        // Go through all ranges looking for a gaps.
        for (AllocatedSceneRange range : ranges) {
            // If there is a space available before this range, return it.
            if (lastUpperBound + size < range.getFirstScene()) {
                return new AllocatedSceneRange(lastUpperBound + 1, lastUpperBound + size);
            }

            // If the space exists, but it's not as big as requested, compare
            // it with the best range so far and replace if it's bigger.
            if (range.getFirstScene() - lastUpperBound > 1) {
                final AllocatedSceneRange newRange = new AllocatedSceneRange(lastUpperBound + 1, range.getFirstScene() - 1);
                if (bestRange == null || newRange.range() > bestRange.range()) {
                    bestRange = newRange;
                }
            }
            lastUpperBound = range.getLastScene();
        }

        // If if we didn't return earlier, check after the last range.
        if (lastUpperBound + size < bound.getLastScene()) {
            return new AllocatedSceneRange(lastUpperBound + 1, lastUpperBound + size - 1);
        }
        // The gap of requested size hasn't been found. Return the best found.
        return bestRange;
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
            throw new IllegalArgumentException("Provisioner has no group range allocated.");
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
    public Group createGroup(@NonNull final Provisioner provisioner, @NonNull final String name) {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Group name cannot be empty");
        }

        final Integer address = nextAvailableGroupAddress(provisioner);
        if (address != null) {
            final Group group = new Group(address, meshUUID);
            group.setName(name);
            return group;
        }
        return null;
    }

    /**
     * Creates a group using the next available group address based on the provisioners allocated group range
     *
     * @param addressLabel Label UUID
     * @param parentLabel  Label UUID for parent address
     * @param name         Group name
     * @return a group or null if creation failed
     */
    public Group createGroup(@NonNull final UUID addressLabel, @Nullable final UUID parentLabel, @NonNull final String name) {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Group name cannot be empty");
        }

        final int address = MeshAddress.generateVirtualAddress(addressLabel);
        final Group group = new Group(addressLabel, parentLabel, meshUUID);
        group.setName(name);
        return group;
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
        return insertGroup(group);
    }

    private boolean insertGroup(@NonNull final Group group) {
        if (!isGroupExist(group)) {
            this.groups.add(group);
            notifyGroupAdded(group);
            return true;
        }
        throw new IllegalArgumentException("Group already exists");
    }

    /**
     * Returns a group based on the group number
     */
    @Nullable
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
     * Returns the next available Scene number from the Provisioner's range
     * that can be assigned to a new Scene.
     *
     * @param provisioner provisioner
     * @return The next available Scene number that can be assigned to a new Scene,
     * or null, if there are no more available numbers in the allocated range.
     * @throws IllegalArgumentException if there is no allocated scene range to the provisioner
     */
    @Nullable
    public Integer nextAvailableSceneNumber(Provisioner provisioner) throws IllegalArgumentException {
        if (provisioner.getAllocatedSceneRanges().isEmpty()) {
            throw new IllegalArgumentException("Please allocate a scene range to the provisioner");
        }

        Collections.sort(scenes, sceneComparator);
        // Iterate through all nodes just once, while iterating over ranges.
        int index = 0;
        for (AllocatedSceneRange sceneRange : provisioner.getAllocatedSceneRanges()) {
            // Start from the beginning of the current range.
            int number = sceneRange.getFirstScene();

            // Iterate through nodes that weren't checked yet.
            int currentIndex = index;
            for (int i = currentIndex; i < scenes.size(); i++) {
                final Scene scene = scenes.get(i);
                index += i;

                // Skip scenes with number below the range.
                if (number > scene.getNumber()) {
                    continue;
                }

                // If we found a space before the current scene, return the scene number.
                if (number < scene.getNumber()) {
                    return number;
                }

                // Else, move the address to the next available address.
                number = scene.getNumber() + 1;

                // If the new address is outside of the range, go to the next one.
                if (number > sceneRange.getLastScene()) {
                    break;
                }
            }
            // If the range has available space, return the address.
            if (number <= sceneRange.getLastScene()) {
                return number;
            }
        }

        // No scene number was found :(
        return null;
    }

    /**
     * Creates a group using the next available group address based on the provisioners allocated group range
     *
     * @param provisioner provisioner
     * @return a group or null if creation failed
     */
    public Scene createScene(@NonNull final Provisioner provisioner) {
        final Integer address = nextAvailableSceneNumber(provisioner);
        if (address != null) {
            return new Scene(address, meshUUID);
        }
        return null;
    }

    /**
     * Creates a group using the next available group address based on the provisioners allocated group range
     *
     * @param provisioner provisioner
     * @return a group or null if creation failed
     */
    public Scene createScene(@NonNull final Provisioner provisioner, @NonNull final String name) {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Scene name cannot be empty");
        }

        final Integer address = nextAvailableSceneNumber(provisioner);
        if (address != null) {
            final Scene scene = new Scene(address, meshUUID);
            scene.setName(name);
            return scene;
        }
        return null;
    }

    /**
     * Creates with a given number and name.
     *
     * @param number Address of the group which must be within the allocated range
     * @param name   Friendly name of the group
     * @return true if the group was successfully added and false otherwise since a group may already exist with the same group address
     * @throws IllegalArgumentException if there is no group range allocated or if the address is out of the range allocated to the provisioner
     */
    public Scene createScene(@NonNull final Provisioner provisioner,
                             final int number,
                             @NonNull final String name) throws IllegalArgumentException {
        if (provisioner.getAllocatedSceneRanges().isEmpty()) {
            throw new IllegalArgumentException("Unable to create scene," +
                    " there is no scene range allocated to the current provisioner");
        }

        for (AllocatedSceneRange range : provisioner.getAllocatedSceneRanges()) {
            if (range.getLowerBound() > number || range.getUpperBound() < number) {
                throw new IllegalArgumentException("Unable to create a scene, " +
                        "the number is outside the range allocated to the provisioner");
            }
        }

        final Scene scene = new Scene(number, meshUUID);
        if (!TextUtils.isEmpty(name))
            scene.setName(name);
        return scene;
    }

    /**
     * Adds a scene to the existing scenes list within the network
     *
     * @param scene to be added
     * @return true if the scene was successfully added and false otherwise since a scene may already exist with the same group address.
     * @throws IllegalArgumentException if there is no group range allocated or if the address is out of the range allocated to the provisioner.
     */
    public boolean addScene(@NonNull final Scene scene) throws IllegalArgumentException {

        final Provisioner provisioner = getSelectedProvisioner();
        if (provisioner.getAllocatedSceneRanges().isEmpty()) {
            throw new IllegalArgumentException("Unable to create scene," +
                    " there is no scene range allocated to the current provisioner");
        }
        return insertScene(scene);
    }

    private boolean insertScene(@NonNull final Scene scene) {
        if (!isSceneExist(scene)) {
            this.scenes.add(scene);
            notifySceneAdded(scene);
            return true;
        }
        throw new IllegalArgumentException("Group already exists");
    }

    /**
     * Returns a scene based on the scene number.
     */
    @Nullable
    public Scene getScene(final int number) {
        for (final Scene scene : scenes) {
            if (number == scene.getNumber()) {
                return scene;
            }
        }
        return null;
    }

    /**
     * Updates a scene in the mesh network
     *
     * @param scene scene to be updated
     */
    public boolean updateScene(@NonNull final Scene scene) {
        if (isSceneExist(scene)) {
            notifySceneUpdated(scene);
            return true;
        }
        return false;
    }

    /**
     * Removes a scene from the mesh network.
     *
     * @param scene scene to be deleted
     * @throws IllegalArgumentException if the scene is already in use.
     */
    public boolean removeScene(@NonNull final Scene scene) throws IllegalArgumentException {
        if (!scene.addresses.isEmpty())
            throw new IllegalArgumentException("Scene is already in use!");
        if (scenes.remove(scene)) {
            notifySceneDeleted(scene);
            return true;
        }
        return false;
    }

    /**
     * Returns true if a scene exists with the same scene number
     *
     * @param number Group address
     */
    public boolean isSceneExist(final int number) {
        for (final Scene scene : scenes) {
            if (scene.getNumber() == number) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if a scene already exists with te same number
     *
     * @param scene Scene
     */
    public boolean isSceneExist(@NonNull final Scene scene) {
        for (final Scene scn : scenes) {
            if (scn.getNumber() == scene.getNumber()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of scenes applied on a given node.
     *
     * @param address node address
     */
    public ArrayList<Scene> getStoredScenes(final int address) {
        if (!MeshAddress.isValidUnicastAddress(address)) {
            throw new IllegalArgumentException("Invalid address, address must be a unicast address");
        }
        final ArrayList<Scene> scenesList = new ArrayList<>();
        for (Scene scene : scenes) {
            for (Integer addr : scene.getAddresses()) {
                if (addr == address) {
                    scenesList.add(scene);
                    break;
                }
            }
        }
        return scenesList;
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

    /**
     * Returns a list of scenes.
     */
    public List<Scene> getScenes() {
        return Collections.unmodifiableList(scenes);
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
        return Collections.unmodifiableList(netKeys);
    }

    public NetworkKey getPrimaryNetworkKey() {
        for (NetworkKey networkKey : netKeys) {
            if (networkKey.getKeyIndex() == 0) {
                return networkKey;
            }
        }
        return null;
    }

    void setNetKeys(@NonNull final List<NetworkKey> netKeys) {
        this.netKeys = netKeys;
    }

    /**
     * Returns a list of {@link ApplicationKey} belonging to the mesh network
     */
    public List<ApplicationKey> getAppKeys() {
        return Collections.unmodifiableList(appKeys);
    }

    /**
     * Returns the provisioning flags
     */
    public final int getProvisioningFlags() {
        int flags = 0;
        final NetworkKey key = getPrimaryNetworkKey();
        if (key != null) {
            if (key.getPhase() == NetworkKey.USING_NEW_KEYS) {
                flags |= 1; // << 0;
            }

            if (ivIndex.isIvUpdateActive()) {
                flags |= 1 << 1;
            }
        }

        return flags;
    }

    /**
     * Returns the uuid for a given virtual address
     *
     * @param address virtual address
     * @return The label uuid if it's known to the provisioner or null otherwise
     */
    public UUID getLabelUuid(final int address) throws IllegalArgumentException {
        if (!MeshAddress.isValidVirtualAddress(address)) {
            throw new IllegalArgumentException("Address type must be a virtual address ");
        }

        for (ProvisionedMeshNode node : nodes) {
            for (Map.Entry<Integer, Element> elementEntry : node.getElements().entrySet()) {
                final Element element = elementEntry.getValue();
                for (Map.Entry<Integer, MeshModel> modelEntry : element.getMeshModels().entrySet()) {
                    final MeshModel model = modelEntry.getValue();
                    if (model != null) {
                        if (model.getPublicationSettings() != null) {
                            if (model.getPublicationSettings().getLabelUUID() != null) {
                                if (address == MeshAddress.generateVirtualAddress(model.getPublicationSettings().getLabelUUID())) {
                                    return model.getPublicationSettings().getLabelUUID();
                                }
                            }
                        }
                        final UUID label = model.getLabelUUID(address);
                        if (label != null) {
                            return label;
                        }
                    }
                }
            }
        }
        return null;
    }
}