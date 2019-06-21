package no.nordicsemi.android.meshprovisioner;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.AddressUtils;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.ProxyFilter;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
abstract class BaseMeshNetwork {
    // Key refresh phases
    public static final int NORMAL_OPERATION = 0; //Distribution of new keys
    public static final int IV_UPDATE_ACTIVE = 1; //Switching to the new keys
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "mesh_uuid")
    @SerializedName("meshUUID")
    @Expose
    final String meshUUID;
    @Ignore
    private final Comparator<ApplicationKey> appKeyComparator = (key1, key2) -> Integer.compare(key1.getKeyIndex(), key2.getKeyIndex());
    @Ignore
    private final Comparator<NetworkKey> netKeyComparator = (key1, key2) -> Integer.compare(key1.getKeyIndex(), key2.getKeyIndex());
    @Ignore
    protected MeshNetworkCallbacks mCallbacks;
    @Ignore
    @SerializedName("$schema")
    @Expose
    String schema = "http://json-schema.org/draft-04/schema#";
    @Ignore
    @SerializedName("id")
    @Expose
    String id = "TBD";
    @Ignore
    @SerializedName("version")
    @Expose
    String version = "1.0";
    @ColumnInfo(name = "mesh_name")
    @SerializedName("meshName")
    @Expose
    String meshName = "nRF Mesh Network";
    @ColumnInfo(name = "timestamp")
    @SerializedName("timestamp")
    @Expose
    long timestamp = System.currentTimeMillis();
    @ColumnInfo(name = "iv_index")
    @Expose
    int ivIndex = 0;
    @ColumnInfo(name = "iv_update_state")
    @Expose
    int ivUpdateState = NORMAL_OPERATION;
    @Ignore
    @SerializedName("netKeys")
    @Expose
    List<NetworkKey> netKeys = new ArrayList<>();
    @Ignore
    @SerializedName("appKeys")
    @Expose
    List<ApplicationKey> appKeys = new ArrayList<>();
    @Ignore
    @SerializedName("provisioners")
    @Expose
    List<Provisioner> provisioners = new ArrayList<>();
    @Ignore
    @SerializedName("nodes")
    @Expose
    List<ProvisionedMeshNode> nodes = new ArrayList<>();
    @Ignore
    @SerializedName("groups")
    @Expose
    List<Group> groups = new ArrayList<>();
    @Ignore
    @SerializedName("scenes")
    @Expose
    List<Scene> scenes = new ArrayList<>();
    //Library related attributes
    @ColumnInfo(name = "unicast_address")
    @Expose
    int unicastAddress = 0x0001;
    @ColumnInfo(name = "last_selected")
    @Expose
    boolean lastSelected;
    @Ignore
    @Expose(serialize = false, deserialize = false)
    private ProxyFilter proxyFilter;

    @Ignore
    private Comparator<ProvisionedMeshNode> nodeComparator = (node1, node2) ->
            Integer.compare(node1.getUnicastAddress(), node2.getUnicastAddress());

    BaseMeshNetwork(@NonNull final String meshUUID) {
        this.meshUUID = meshUUID;
    }

    private boolean isNetKeyExists(final String appKey) {
        for (int i = 0; i < netKeys.size(); i++) {
            final NetworkKey networkKey = netKeys.get(i);
            if (appKey.equalsIgnoreCase(MeshParserUtils.bytesToHex(networkKey.getKey(), false))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates an Network key
     *
     * @return {@link NetworkKey}
     * @throws IllegalArgumentException in case the generated application key already exists
     */
    public NetworkKey createNetworkKey() throws IllegalArgumentException {
        final NetworkKey key = new NetworkKey(getAvailableNetKeyIndex(), MeshParserUtils.toByteArray(SecureUtils.generateRandomNetworkKey()));
        key.setMeshUuid(meshUUID);
        return key;
    }

    /**
     * Adds a network to the list of network keys in the network
     *
     * @param netKey key
     */
    public boolean addNetKey(@NonNull final String netKey) {
        if (MeshParserUtils.validateNetworkKeyInput(netKey)) {
            if (isNetKeyExists(netKey)) {
                throw new IllegalArgumentException("Network key already exists");
            } else {
                final NetworkKey key = new NetworkKey(getAvailableNetKeyIndex(), MeshParserUtils.toByteArray(netKey));
                key.setMeshUuid(meshUUID);
                netKeys.add(key);
                notifyNetKeyAdded(key);
                return true;
            }
        }
        return false;
    }

    /**
     * Adds an app key to the list of keys with the given key index. If there is an existing key with the same index,
     * an illegal argument exception is thrown.
     *
     * @param keyIndex      Index of the key
     * @param newNetworkKey key
     * @throws IllegalArgumentException if net key already exists
     */
    public boolean addNetKey(final int keyIndex, @NonNull final String newNetworkKey) throws IllegalArgumentException {
        if (MeshParserUtils.validateNetworkKeyInput(newNetworkKey)) {
            if (isNetKeyExists(newNetworkKey)) {
                throw new IllegalArgumentException("Net key already exists");
            } else {
                final NetworkKey networkKey = new NetworkKey(keyIndex, MeshParserUtils.toByteArray(newNetworkKey));
                networkKey.setMeshUuid(meshUUID);
                netKeys.add(networkKey);
                notifyNetKeyAdded(networkKey);
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a Net key to the list of net keys with the given key index
     *
     * @param newNetKey application key
     */
    public boolean addNetKey(@NonNull final NetworkKey newNetKey) {
        if (isNetKeyExists(MeshParserUtils.bytesToHex(newNetKey.getKey(), false))) {
            throw new IllegalArgumentException("Net key already exists");
        } else {
            newNetKey.setMeshUuid(meshUUID);
            netKeys.add(newNetKey);
            notifyNetKeyAdded(newNetKey);
        }
        return true;
    }

    private int getAvailableNetKeyIndex() {
        if (netKeys.isEmpty()) {
            return 0;
        } else {
            Collections.sort(netKeys, netKeyComparator);
            final int index = netKeys.size() - 1;
            return netKeys.get(index).getKeyIndex() + 1;
        }
    }

    /**
     * Updates a net key in the list of keys of the mesh network.
     *
     * @param keyIndex index of the key
     * @param netKey   key
     */
    public boolean updateNetKey(final int keyIndex, @NonNull final String netKey) throws IllegalArgumentException {
        if (MeshParserUtils.validateAppKeyInput(netKey)) {
            if (isNetKeyExists(netKey))
                throw new IllegalArgumentException("Net key already exists");

            final NetworkKey key = getNetKey(keyIndex);
            if (isKeyInUse(key)) {
                throw new IllegalArgumentException("Unable to update a net key that's already in use");
            } else {
                for (int i = 0; i < netKeys.size(); i++) {
                    final NetworkKey networkKey = netKeys.get(i);
                    if (keyIndex == networkKey.getKeyIndex()) {
                        networkKey.setKey(MeshParserUtils.toByteArray(netKey));
                        notifyNetKeyUpdated(networkKey);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Updates a net key name
     *
     * @param keyIndex Index of the key
     * @param name     Name
     */
    public boolean updateNetKeyName(final int keyIndex, @NonNull final String name) throws IllegalArgumentException {
        if (TextUtils.isEmpty(name))
            throw new IllegalArgumentException("Name cannot be empty!");

        final NetworkKey key = getNetKey(keyIndex);
        if (key == null)
            throw new IllegalArgumentException("Invalid key index, key does not exist");
        for (int i = 0; i < netKeys.size(); i++) {
            final NetworkKey netKey = netKeys.get(i);
            if (keyIndex == netKey.getKeyIndex()) {
                netKey.setName(name);
                notifyNetKeyAdded(netKey);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes a network key from the network key list
     *
     * @param networkKey key to be removed
     * @throws IllegalArgumentException if the key is in use or if it does not exist in the list of keys
     */
    public boolean removeNetKey(@NonNull final NetworkKey networkKey) throws IllegalArgumentException {
        if (!isKeyInUse(networkKey)) {
            if (netKeys.remove(networkKey)) {
                notifyNetKeyDeleted(networkKey);
                return true;
            } else {
                throw new IllegalArgumentException("Key does not exist");
            }
        }
        throw new IllegalArgumentException("Unable to delete a network key that's already in use");
    }

    /**
     * Checks if the key is in use. This will check if the specified key is added to a node
     *
     * @param netKey {@link NetworkKey}
     */
    public boolean isKeyInUse(@NonNull final NetworkKey netKey) {
        for (ProvisionedMeshNode node : nodes) {
            final int netKeyIndex = netKey.getKeyIndex();
            for (Integer keyIndex : node.getAddedNetKeyIndexes()) {
                if (netKeyIndex == keyIndex) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns an application key with a given key index
     *
     * @param keyIndex index
     */
    public NetworkKey getNetKey(final int keyIndex) {
        for (NetworkKey key : netKeys) {
            if (keyIndex == key.getKeyIndex()) {
                return key;
            }
        }
        return null;
    }

    /**
     * Creates an application key
     *
     * @return {@link ApplicationKey}
     * @throws IllegalArgumentException in case the generated application key already exists
     */
    public ApplicationKey createAppKey() throws IllegalArgumentException {
        final ApplicationKey key = new ApplicationKey(getAvailableAppKeyIndex(), MeshParserUtils.toByteArray(SecureUtils.generateRandomApplicationKey()));
        key.setMeshUuid(meshUUID);
        return key;
    }

    /**
     * Adds an application key to the list of application keys in the network.
     *
     * @param appKey application key to be added
     */
    public boolean addAppKey(@NonNull final String appKey) throws IllegalArgumentException {
        if (MeshParserUtils.validateAppKeyInput(appKey)) {
            if (isAppKeyExists(appKey)) {
                throw new IllegalArgumentException("App key already exists");
            } else {
                final ApplicationKey applicationKey = new ApplicationKey(getAvailableAppKeyIndex(), MeshParserUtils.toByteArray(appKey));
                applicationKey.setMeshUuid(meshUUID);
                appKeys.add(applicationKey);
                notifyAppKeyAdded(applicationKey);
                return true;
            }
        }
        return false;
    }

    /**
     * Adds an app key to the list of keys with the given key index. If there is an existing key with the same index,
     * an illegal argument exception is thrown.
     *
     * @param keyIndex  index of the key
     * @param newAppKey application key
     * @throws IllegalArgumentException if app key already exists
     */
    public boolean addAppKey(final int keyIndex, @NonNull final String newAppKey) {
        if (isAppKeyExists(newAppKey)) {
            throw new IllegalArgumentException("App key already exists");
        } else {
            final ApplicationKey applicationKey = new ApplicationKey(keyIndex, MeshParserUtils.toByteArray(newAppKey));
            appKeys.add(keyIndex, applicationKey);
            notifyAppKeyAdded(applicationKey);
        }
        return true;
    }

    /**
     * Adds an app key to the list of keys with the given key index. If there is an existing key with the same index,
     * an illegal argument exception is thrown.
     *
     * @param newAppKey application key
     * @throws IllegalArgumentException if app key already exists
     */
    public boolean addAppKey(@NonNull final ApplicationKey newAppKey) {
        if (isAppKeyExists(MeshParserUtils.bytesToHex(newAppKey.getKey(), false))) {
            throw new IllegalArgumentException("App key already exists");
        } else {
            newAppKey.setMeshUuid(meshUUID);
            appKeys.add(newAppKey);
            notifyAppKeyAdded(newAppKey);
        }
        return true;
    }

    private int getAvailableAppKeyIndex() {
        if (appKeys.isEmpty()) {
            return 0;
        } else {
            Collections.sort(appKeys, appKeyComparator);
            final int index = appKeys.size() - 1;
            return appKeys.get(index).getKeyIndex() + 1;
        }
    }

    /**
     * Returns an application key with a given key index
     *
     * @param keyIndex index
     */
    public ApplicationKey getAppKey(final int keyIndex) {
        for (ApplicationKey key : appKeys) {
            if (keyIndex == key.getKeyIndex()) {
                return key;
            }
        }
        return null;
    }

    private boolean isAppKeyExists(final String appKey) {
        for (int i = 0; i < appKeys.size(); i++) {
            final ApplicationKey applicationKey = appKeys.get(i);
            if (appKey.equalsIgnoreCase(MeshParserUtils.bytesToHex(applicationKey.getKey(), false))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates an app key in the mesh network.
     *
     * @param keyIndex Index of the key
     * @param appKey   Application key
     */
    public boolean updateAppKey(final int keyIndex, @NonNull final String appKey) throws IllegalArgumentException {
        if (MeshParserUtils.validateAppKeyInput(appKey)) {
            if (isAppKeyExists(appKey))
                throw new IllegalArgumentException("App key already exists");

            final ApplicationKey key = getAppKey(keyIndex);
            if (!isKeyInUse(key)) {
                for (int i = 0; i < appKeys.size(); i++) {
                    final ApplicationKey applicationKey = appKeys.get(i);
                    if (keyIndex == applicationKey.getKeyIndex()) {
                        applicationKey.setKey(MeshParserUtils.toByteArray(appKey));
                        notifyAppKeyUpdated(applicationKey);
                        return true;
                    }
                }
            } else {
                throw new IllegalArgumentException("Unable to update an app key that's already in use");
            }
        }
        return false;
    }

    /**
     * Updates an app key name
     * *
     *
     * @param keyIndex Index of the key
     * @param name     Name
     */
    public boolean updateAppKeyName(final int keyIndex, @NonNull final String name) throws IllegalArgumentException {
        if (TextUtils.isEmpty(name))
            throw new IllegalArgumentException("Name cannot be empty!");


        final ApplicationKey key = getAppKey(keyIndex);
        if (key == null)
            throw new IllegalArgumentException("Invalid key index, key does not exist");
        for (int i = 0; i < appKeys.size(); i++) {
            final ApplicationKey applicationKey = appKeys.get(i);
            if (keyIndex == applicationKey.getKeyIndex()) {
                applicationKey.setName(name);
                notifyAppKeyUpdated(applicationKey);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes an app key from the app key list
     *
     * @param appKey app key to be removed
     * @throws IllegalArgumentException if the key is in use or if it does not exist in the list of keys
     */
    public boolean removeAppKey(@NonNull final ApplicationKey appKey) throws IllegalArgumentException {
        if (isKeyInUse(appKey)) {
            throw new IllegalArgumentException("Unable to delete an app key that's in use");
        } else {
            if (appKeys.remove(appKey)) {
                notifyAppKeyDeleted(appKey);
                return true;
            } else {
                throw new IllegalArgumentException("Key does not exist");
            }
        }
    }

    /**
     * Checks if the app key is in use. This will check if the specified app key is added to a node
     *
     * @param appKey {@link ApplicationKey}
     */
    public boolean isKeyInUse(@NonNull final ApplicationKey appKey) {
        for (ProvisionedMeshNode node : nodes) {
            final int appKeyIndex = appKey.getKeyIndex();
            for (Integer keyIndex : node.getAddedAppKeyIndexes()) {
                if (appKeyIndex == keyIndex) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getProvisionerAddress() {
        return getSelectedProvisioner().getProvisionerAddress();
    }

    /**
     * Returns the next available unicast address
     *
     * @return unicast address
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public int getUnicastAddress() {
        return unicastAddress;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setUnicastAddress(final int address) {
        this.unicastAddress = address;
    }

    /**
     * Assigns a unicast address, to be used by a node
     *
     * @param unicastAddress Unicast address
     * @return true if success, false if the address is in use by another device
     */
    public boolean assignUnicastAddress(final int unicastAddress) throws IllegalArgumentException {
        if (getNode(unicastAddress) != null)
            throw new IllegalArgumentException("Unicast address is already in use.");

        this.unicastAddress = unicastAddress;
        notifyNetworkUpdated();
        return true;
    }

    /**
     * Returns the next unicast address available based on the number of elements
     *
     * @param elementCount element count
     */
    public int nextAvailableUnicastAddress(final int elementCount) {
        Collections.sort(nodes, nodeComparator);
        // Iterate through all nodes just once, while iterating over ranges.
        int index = 0;
        final Provisioner p = getSelectedProvisioner();
        for (AllocatedUnicastRange range : p.getAllocatedUnicastRanges()) {
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

    private boolean isAddressInUse(final int address) {
        for (ProvisionedMeshNode node : nodes) {
            if (address == node.getUnicastAddress()) {
                return true;
            }
        }
        return false;
    }

    public int getGlobalTtl() {
        return getSelectedProvisioner().getGlobalTtl();
    }

    /**
     * Sets the global ttl of the messages sent by the provisioner
     *
     * @param globalTtl ttl
     */
    public void setGlobalTtl(final int globalTtl) {
        final Provisioner provisioner = provisioners.get(0);
        provisioner.setGlobalTtl(globalTtl);
        notifyProvisionerUpdated(provisioner);
    }

    /**
     * Returns the list of {@link Provisioner}
     */
    public List<Provisioner> getProvisioners() {
        return Collections.unmodifiableList(provisioners);
    }

    void setProvisioners(List<Provisioner> provisioners) {
        this.provisioners = provisioners;
    }

    /**
     * Creates a provisioner
     *
     * @return returns true if updated and false otherwise
     */
    public Provisioner createProvisioner() {
        final List<AllocatedUnicastRange> unicastRange = new ArrayList();
        final AllocatedUnicastRange range1 = new AllocatedUnicastRange(0x0100, 0x1500);
        unicastRange.add(range1);
        final List<AllocatedGroupRange> groupRange = new ArrayList();
        groupRange.add(new AllocatedGroupRange(0xC000, 0xFEFF));
        final List<AllocatedSceneRange> sceneRange = new ArrayList();
        sceneRange.add(new AllocatedSceneRange(0x0001, 0xFFFF));
        return new Provisioner(UUID.randomUUID().toString(),
                unicastRange, groupRange, sceneRange, meshUUID);
    }

    /**
     * Adds a provisioner to the network
     *
     * @param provisioner {@link Provisioner}
     * @throws IllegalArgumentException if unicast address is invalid, in use by a node
     */
    public boolean addProvisioner(@NonNull final Provisioner provisioner) throws IllegalArgumentException {

        if (provisioner.allocatedUnicastRanges.isEmpty()) {
            throw new IllegalArgumentException("Provisioner has no allocated unicast range assigned");
        }

        for (Provisioner other : provisioners) {
            if (provisioner.hasOverlappingUnicastRanges(other.getAllocatedUnicastRanges())
                    || provisioner.hasOverlappingGroupRanges(other.getAllocatedGroupRanges())
                    || provisioner.hasOverlappingSceneRanges(other.getAllocatedSceneRanges())) {
                throw new IllegalArgumentException("Provisioner ranges overlap");
            }
        }

        if (!provisioner.isAddressWithinAllocatedRange(provisioner.getProvisionerAddress())) {
            throw new IllegalArgumentException("Unicast address assigned to a provisioner must be within an allocated unicast address range");
        }

        if (isAddressInUse(provisioner.getProvisionerAddress())) {
            throw new IllegalArgumentException("Unicast address is in use by another node");
        }

        if (provisioner.isNodeAddressInUse(nodes)) {
            throw new IllegalArgumentException("Unicast address is already in use!");
        }

        if (isProvisionerUuidInUse(provisioner.getProvisionerUuid())) {
            throw new IllegalArgumentException("Provisioner uuid already in use!");
        }

        provisioner.setProvisionerAddress(provisioner.getProvisionerAddress());
        provisioners.add(provisioner);
        notifyProvisionerAdded(provisioner);
        final ProvisionedMeshNode node = new ProvisionedMeshNode(provisioner, meshUUID, netKeys, appKeys);
        nodes.add(node);
        notifyNodeAdded(node);
        return true;
    }

    /**
     * Update provisioner
     *
     * @param provisioner {@link Provisioner}
     * @return returns true if updated and false otherwise
     */
    public boolean updateProvisioner(@NonNull final Provisioner provisioner) {
        if (!isProvisionerUuidInUse(provisioner.getProvisionerUuid())) {
            throw new IllegalArgumentException("Provisioner does not exist, consider adding a provisioner first!");
        }

        if (provisioner.allocatedUnicastRanges.isEmpty()) {
            throw new IllegalArgumentException("Provisioner has no allocated unicast range assigned");
        }

        for (Provisioner other : provisioners) {
            if (!other.getProvisionerUuid().equalsIgnoreCase(provisioner.getProvisionerUuid())) {
                if (provisioner.hasOverlappingUnicastRanges(other.getAllocatedUnicastRanges())
                        || provisioner.hasOverlappingGroupRanges(other.getAllocatedGroupRanges())
                        || provisioner.hasOverlappingSceneRanges(other.getAllocatedSceneRanges())) {
                    throw new IllegalArgumentException("Provisioner ranges overlap");
                }
            }
        }

        if (!provisioner.isAddressWithinAllocatedRange(provisioner.getProvisionerAddress())) {
            throw new IllegalArgumentException("Unicast address assigned to a provisioner must be within an allocated unicast address range");
        }

        for (ProvisionedMeshNode node : nodes) {
            if (!node.getUuid().equalsIgnoreCase(provisioner.getProvisionerUuid())) {
                if (node.getUnicastAddress() == provisioner.getProvisionerAddress()) {
                    throw new IllegalArgumentException("Unicast address is in use by another node");
                }
            }
        }

        if (provisioner.isNodeAddressInUse(nodes)) {
            throw new IllegalArgumentException("Unicast address is already in use by another provisioner!");
        }

        boolean flag = false;
        for (int i = 0; i < provisioners.size(); i++) {
            if (provisioners.get(i).getProvisionerUuid().equalsIgnoreCase(provisioner.getProvisionerUuid())) {
                provisioners.set(i, provisioner);
                notifyProvisionerUpdated(provisioner);
                flag = true;
            }
        }
        if (flag) {
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).getUuid().equalsIgnoreCase(provisioner.getProvisionerUuid())) {
                    final ProvisionedMeshNode node = new ProvisionedMeshNode(provisioner, meshUUID, netKeys, appKeys);
                    nodes.set(i, node);
                    notifyNodeUpdated(node);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Removes a provisioner from the mesh network
     *
     * @param provisioner {@link Provisioner}
     * @return true if the provisioner was deleted or false otherwise
     */
    public boolean removeProvisioner(@NonNull final Provisioner provisioner) {
        if (provisioners.remove(provisioner)) {
            notifyProvisionerDeleted(provisioner);
            final ProvisionedMeshNode node = getNode(provisioner.getProvisionerAddress());
            if (node != null) {
                deleteNode(node);
                notifyNodeDeleted(node);
            }
            return true;
        }
        return false;
    }

    /**
     * Selects a provisioner if there are multiple provisioners.
     *
     * @param provisioner {@link Provisioner}
     */
    public final void selectProvisioner(final Provisioner provisioner) {
        provisioner.setLastSelected(true);
        for (Provisioner prov : provisioners) {
            if (!prov.getProvisionerUuid().equalsIgnoreCase(provisioner.getProvisionerUuid())) {
                prov.setLastSelected(false);
            }
        }
        notifyProvisionerUpdated(provisioners);
    }

    /**
     * Checks if the provisioner is selected
     * <p> There could be networks that may contain more than one provisioner</p>
     *
     * @return true if a provisioner was selected or false otherwise
     */
    public final boolean isProvisionerSelected() {
        if (provisioners.size() == 1) {
            if (!provisioners.get(0).isLastSelected())
                selectProvisioner(provisioners.get(0));
            return true;
        }

        for (Provisioner provisioner : provisioners) {
            if (provisioner.isLastSelected()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the selected provisioner in the network
     */
    public Provisioner getSelectedProvisioner() {
        for (Provisioner provisioner : provisioners) {
            if (provisioner.isLastSelected()) {
                return provisioner;
            }
        }
        return null;
    }

    public boolean isProvisionerUuidInUse(@NonNull final String uuid) {
        for (Provisioner provisioner : provisioners) {
            if (provisioner.getProvisionerUuid().equalsIgnoreCase(uuid)) {
                return true;
            }
        }
        return false;
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

    /**
     * Returns the mesh node with the corresponding unicast address
     *
     * @param unicastAddress unicast address of the node
     */
    public ProvisionedMeshNode getNode(@NonNull final byte[] unicastAddress) {
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
    public ProvisionedMeshNode getNode(final int unicastAddress) {
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
    public boolean deleteNode(@NonNull final ProvisionedMeshNode meshNode) {
        for (ProvisionedMeshNode node : nodes) {
            if (meshNode.getUnicastAddress() == node.getUnicastAddress()) {
                nodes.remove(node);
                notifyNodeDeleted(meshNode);
                return true;
            }
        }
        return false;
    }

    boolean deleteResetNode(@NonNull final ProvisionedMeshNode meshNode) {
        for (ProvisionedMeshNode node : nodes) {
            if (meshNode.getUnicastAddress() == node.getUnicastAddress()) {
                nodes.remove(node);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the {@link ProxyFilter} set on the proxy
     */
    @Nullable
    public ProxyFilter getProxyFilter() {
        return proxyFilter;
    }

    /**
     * Sets the {@link ProxyFilter} settings on the proxy
     * <p>
     * Please note that this is not persisted within the node since the filter is reinitialized to a whitelist filter upon connecting to a proxy node.
     * Therefore after setting a proxy filter and disconnecting users will have to manually
     * <p/>
     */
    public void setProxyFilter(@Nullable final ProxyFilter proxyFilter) {
        this.proxyFilter = proxyFilter;
    }

    final void notifyNetworkUpdated() {
        if (mCallbacks != null) {
            mCallbacks.onMeshNetworkUpdated();
        }
    }

    final void notifyNetKeyAdded(@NonNull final NetworkKey networkKey) {
        if (mCallbacks != null) {
            mCallbacks.onNetworkKeyAdded(networkKey);
        }
    }

    final void notifyNetKeyUpdated(@NonNull final NetworkKey networkKey) {
        if (mCallbacks != null) {
            mCallbacks.onNetworkKeyUpdated(networkKey);
        }
    }

    final void notifyNetKeyDeleted(@NonNull final NetworkKey networkKey) {
        if (mCallbacks != null) {
            mCallbacks.onNetworkKeyDeleted(networkKey);
        }
    }

    final void notifyAppKeyAdded(@NonNull final ApplicationKey appKey) {
        if (mCallbacks != null) {
            mCallbacks.onApplicationKeyAdded(appKey);
        }
    }

    final void notifyAppKeyUpdated(@NonNull final ApplicationKey appKey) {
        if (mCallbacks != null) {
            mCallbacks.onApplicationKeyUpdated(appKey);
        }
    }

    final void notifyAppKeyDeleted(@NonNull final ApplicationKey appKey) {
        if (mCallbacks != null) {
            mCallbacks.onApplicationKeyDeleted(appKey);
        }
    }

    final void notifyProvisionerAdded(@NonNull final Provisioner provisioner) {
        if (mCallbacks != null) {
            mCallbacks.onProvisionerAdded(provisioner);
        }
    }

    final void notifyProvisionerUpdated(@NonNull final Provisioner provisioner) {
        if (mCallbacks != null) {
            mCallbacks.onProvisionerUpdated(provisioner);
        }
    }

    final void notifyProvisionerUpdated(@NonNull final List<Provisioner> provisioner) {
        if (mCallbacks != null) {
            mCallbacks.onProvisionersUpdated(provisioner);
        }
    }

    final void notifyProvisionerDeleted(@NonNull final Provisioner provisioner) {
        if (mCallbacks != null) {
            mCallbacks.onProvisionerDeleted(provisioner);
        }
    }

    final void notifyNodeAdded(@NonNull final ProvisionedMeshNode node) {
        if (mCallbacks != null) {
            mCallbacks.onNodeAdded(node);
        }
    }

    final void notifyNodeUpdated(@NonNull final ProvisionedMeshNode node) {
        if (mCallbacks != null) {
            mCallbacks.onNodeUpdated(node);
        }
    }

    final void notifyNodesUpdated() {
        if (mCallbacks != null) {
            mCallbacks.onNodesUpdated();
        }
    }

    final void notifyNodeDeleted(@NonNull final ProvisionedMeshNode meshNode) {
        if (mCallbacks != null) {
            mCallbacks.onNodeDeleted(meshNode);
        }
    }

    final void notifySceneAdded(@NonNull final Scene scene) {
        if (mCallbacks != null) {
            mCallbacks.onSceneAdded(scene);
        }
    }

    final void notifySceneUpdated(@NonNull final Scene scene) {
        if (mCallbacks != null) {
            mCallbacks.onSceneUpdated(scene);
        }
    }

    final void notifySceneDeleted(@NonNull final Scene scene) {
        if (mCallbacks != null) {
            mCallbacks.onSceneDeleted(scene);
        }
    }

    final void notifyGroupAdded(@NonNull final Group group) {
        if (mCallbacks != null) {
            mCallbacks.onGroupAdded(group);
        }
    }

    final void notifyGroupUpdated(@NonNull final Group group) {
        if (mCallbacks != null) {
            mCallbacks.onGroupUpdated(group);
        }
    }

    final void notifyGroupDeleted(@NonNull final Group group) {
        if (mCallbacks != null) {
            mCallbacks.onGroupDeleted(group);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NORMAL_OPERATION, IV_UPDATE_ACTIVE})
    public @interface IvUpdateStates {
    }
}
