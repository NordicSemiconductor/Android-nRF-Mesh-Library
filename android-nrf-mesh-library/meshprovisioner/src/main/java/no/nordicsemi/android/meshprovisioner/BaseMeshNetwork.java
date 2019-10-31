package no.nordicsemi.android.meshprovisioner;

import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
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
import androidx.room.TypeConverters;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.ProxyFilter;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
abstract class BaseMeshNetwork {
    private static final String TAG = "BaseMeshNetwork";
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
    protected final Comparator<ApplicationKey> appKeyComparator = (key1, key2) -> Integer.compare(key1.getKeyIndex(), key2.getKeyIndex());
    @Ignore
    protected final Comparator<NetworkKey> netKeyComparator = (key1, key2) -> Integer.compare(key1.getKeyIndex(), key2.getKeyIndex());
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
    @Ignore
    @ColumnInfo(name = "unicast_address")
    @Expose
    int unicastAddress = 0x0001;
    @ColumnInfo(name = "last_selected")
    @Expose
    boolean lastSelected;
    @NonNull
    @TypeConverters(MeshTypeConverters.class)
    @ColumnInfo(name = "sequence_numbers")
    protected SparseIntArray sequenceNumbers = new SparseIntArray();
    @Ignore
    @Expose(serialize = false, deserialize = false)
    private ProxyFilter proxyFilter;
    @Ignore
    protected final Comparator<ProvisionedMeshNode> nodeComparator = (node1, node2) ->
            Integer.compare(node1.getUnicastAddress(), node2.getUnicastAddress());
    @Ignore
    protected final Comparator<Group> groupComparator = (group1, group2) ->
            Integer.compare(group1.getAddress(), group2.getAddress());
    @Ignore
    protected final Comparator<AllocatedUnicastRange> unicastRangeComparator = (range1, range2) ->
            Integer.compare(range1.getLowAddress(), range2.getLowAddress());
    @Ignore
    protected final Comparator<AllocatedGroupRange> groupRangeComparator = (range1, range2) ->
            Integer.compare(range1.getLowAddress(), range2.getLowAddress());
    @Ignore
    protected final Comparator<AllocatedSceneRange> sceneRangeComparator = (range1, range2) ->
            Integer.compare(range1.getFirstScene(), range2.getFirstScene());

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
     * Update a network key with the given 16-byte hexadecimal string in the mesh network.
     *
     * @param networkKey Network key
     * @param newNetKey  16-byte hexadecimal string
     */
    public boolean updateNetKey(@NonNull final NetworkKey networkKey, @NonNull final String newNetKey) throws IllegalArgumentException {
        if (MeshParserUtils.validateKeyInput(newNetKey)) {
            final byte[] key = MeshParserUtils.toByteArray(newNetKey);
            if (isNetKeyExists(newNetKey)) {
                throw new IllegalArgumentException("Net key already in use");
            }

            final int keyIndex = networkKey.getKeyIndex();
            final NetworkKey netKey = getNetKey(keyIndex);
            if (!isKeyInUse(netKey)) {
                //We check if the contents of the key are the same
                //This will return true only if the key index and the key are the same
                if (netKey.equals(networkKey)) {
                    netKey.setKey(key);
                    return updateMeshKey(netKey);
                } else {
                    return false;
                }
            } else {
                throw new IllegalArgumentException("Unable to update a network key that's already in use");
            }
        }
        return false;
    }

    /**
     * Update a network key in the mesh network.
     *
     * @param networkKey Network key
     * @throws IllegalArgumentException if the key is already in use
     */
    public boolean updateNetKey(@NonNull final NetworkKey networkKey) throws IllegalArgumentException {
        final int keyIndex = networkKey.getKeyIndex();
        final NetworkKey key = getNetKey(keyIndex);
        //We check if the contents of the key are the same
        //This will return true only if the key index and the key are the same
        if (key.equals(networkKey)) {
            return updateMeshKey(networkKey);
        } else {
            //If the keys are not the same we check if its in use before updating the key
            if (!isKeyInUse(key)) {
                //We check if the contents of the key are the same
                //This will return true only if the key index and the key are the same
                return updateMeshKey(networkKey);
            } else {
                throw new IllegalArgumentException("Unable to update a network key that's already in use");
            }
        }
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
                throw new IllegalArgumentException("Key does not exist.");
            }
        }
        throw new IllegalArgumentException("Unable to delete a network key that's already in use.");
    }

    /**
     * Returns an application key with a given key index
     *
     * @param keyIndex index
     */
    public NetworkKey getNetKey(final int keyIndex) {
        for (NetworkKey key : netKeys) {
            if (keyIndex == key.getKeyIndex()) {
                try {
                    return key.clone();
                } catch (CloneNotSupportedException e) {
                    Log.e(TAG, "Error while cloning key: " + e.getMessage());
                }
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
        if (netKeys.isEmpty()) {
            throw new IllegalStateException("Cannot create an App Key without a Network key. Consider creating a network key first");
        }

        final ApplicationKey key = new ApplicationKey(getAvailableAppKeyIndex(), MeshParserUtils.toByteArray(SecureUtils.generateRandomApplicationKey()));
        key.setMeshUuid(meshUUID);
        return key;
    }

    /**
     * Adds an app key to the list of keys with the given key index. If there is an existing key with the same index,
     * an illegal argument exception is thrown.
     *
     * @param newAppKey application key
     * @throws IllegalArgumentException if app key already exists
     */
    public boolean addAppKey(@NonNull final ApplicationKey newAppKey) {
        if (netKeys.isEmpty()) {
            throw new IllegalStateException("Cannot create an App Key without a Network key. Consider creating a network key first");
        }

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
                try {
                    return key.clone();
                } catch (CloneNotSupportedException e) {
                    Log.e(TAG, "Error while cloning key: " + e.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * Returns a list of application keys bound to a Network key
     *
     * @param boundNetKeyIndex Network Key index
     */
    protected List<ApplicationKey> getAppKeys(final int boundNetKeyIndex) {
        final List<ApplicationKey> applicationKeys = new ArrayList<>();
        for (ApplicationKey applicationKey : appKeys) {
            if (applicationKey.getBoundNetKeyIndex() == boundNetKeyIndex) {
                applicationKeys.add(applicationKey);
            }
        }
        return applicationKeys;
    }

    private boolean isAppKeyExists(@NonNull final String appKey) {
        for (int i = 0; i < appKeys.size(); i++) {
            final ApplicationKey applicationKey = appKeys.get(i);
            if (appKey.equalsIgnoreCase(MeshParserUtils.bytesToHex(applicationKey.getKey(), false))) {
                return true;
            }
        }
        return false;
    }

    private boolean isAppKeyExists(@NonNull final byte[] appKey) {
        for (int i = 0; i < appKeys.size(); i++) {
            final ApplicationKey applicationKey = appKeys.get(i);
            if (Arrays.equals(applicationKey.getKey(), appKey)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates an app key with a given key in the mesh network.
     *
     * @param applicationKey {@link ApplicationKey}
     * @param newAppKey      Application key
     */
    public boolean updateAppKey(@NonNull final ApplicationKey applicationKey, @NonNull final String newAppKey) throws IllegalArgumentException {
        if (MeshParserUtils.validateKeyInput(newAppKey)) {
            final byte[] key = MeshParserUtils.toByteArray(newAppKey);
            if (isNetKeyExists(newAppKey)) {
                throw new IllegalArgumentException("Net key already in use");
            }

            final int keyIndex = applicationKey.getKeyIndex();
            final ApplicationKey appKey = getAppKey(keyIndex);
            if (!isKeyInUse(appKey)) {
                //We check if the contents of the key are the same
                //This will return true only if the key index and the key are the same
                if (appKey.equals(applicationKey)) {
                    appKey.setKey(key);
                    return updateMeshKey(appKey);
                } else {
                    return false;
                }
            } else {
                throw new IllegalArgumentException("Unable to update a application key that's already in use");
            }
        }
        return false;
    }

    /**
     * Updates an app key in the mesh network.
     *
     * @param applicationKey {@link ApplicationKey}
     * @throws IllegalArgumentException if the key is already in use
     */
    public boolean updateAppKey(@NonNull final ApplicationKey applicationKey) throws IllegalArgumentException {
        final int keyIndex = applicationKey.getKeyIndex();
        final ApplicationKey key = getAppKey(keyIndex);
        //We check if the contents of the key are the same
        //This will return true only if the key index and the key are the same
        if (key.equals(applicationKey)) {
            return updateMeshKey(applicationKey);
        } else {
            //If the keys are not the same we check if its in use before updating the key
            if (!isKeyInUse(key)) {
                //We check if the contents of the key are the same
                //This will return true only if the key index and the key are the same
                return updateMeshKey(applicationKey);
            } else {
                throw new IllegalArgumentException("Unable to update a application key that's already in use");
            }
        }
    }

    private boolean updateMeshKey(@NonNull final MeshKey key) {
        if (key instanceof ApplicationKey) {
            ApplicationKey appKey = null;
            for (int i = 0; i < appKeys.size(); i++) {
                final ApplicationKey tempKey = appKeys.get(i);
                if (tempKey.getKeyIndex() == key.getKeyIndex()) {
                    appKey = (ApplicationKey) key;
                    appKeys.set(i, appKey);
                    break;
                }
            }
            if (appKey != null) {
                notifyAppKeyUpdated(appKey);
                return true;
            }
        } else {
            NetworkKey netKey = null;
            for (int i = 0; i < netKeys.size(); i++) {
                final NetworkKey tempKey = netKeys.get(i);
                if (tempKey.getKeyIndex() == key.getKeyIndex()) {
                    netKey = (NetworkKey) key;
                    netKeys.set(i, netKey);
                    break;
                }
            }
            if (netKey != null) {
                netKey.setTimestamp(System.currentTimeMillis());
                notifyNetKeyUpdated(netKey);
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
     * Checks if the app key is in use.
     *
     * <p>
     * This will check if the specified app key is added to a node other than the selected provisioner node
     * </p>
     *
     * @param meshKey {@link MeshKey}
     */
    public boolean isKeyInUse(@NonNull final MeshKey meshKey) {
        for (ProvisionedMeshNode node : nodes) {
            if (!node.getUuid().equalsIgnoreCase(getSelectedProvisioner().getProvisionerUuid())) {
                final int index = meshKey.getKeyIndex();
                //We need to check if a key index is in use by checking in the added net/app key indexes
                if (meshKey instanceof ApplicationKey) {
                    return MeshParserUtils.isNodeKeyExists(node.getAddedAppKeys(), index);
                } else {
                    return MeshParserUtils.isNodeKeyExists(node.getAddedNetKeys(), index);
                }
            }
        }
        return false;
    }

    @Nullable
    public Integer getProvisionerAddress() {
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

    private boolean isAddressInUse(@Nullable final Integer address) {
        if (address == null)
            return false;

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
     * Creates a provisioner with a given name
     *
     * @param name Provisioner name
     * @return {@link Provisioner}
     * @throws IllegalArgumentException if the name is empty
     */
    public Provisioner createProvisioner(@NonNull final String name) throws IllegalArgumentException {
        return createProvisioner(name,
                new AllocatedUnicastRange(0x0001, 0x199A),
                new AllocatedGroupRange(0xC000, 0xCC9A),
                new AllocatedSceneRange(0x0001, 0x3333));
    }

    /**
     * Creates a provisioner
     *
     * @param name         Provisioner name
     * @param unicastRange {@link AllocatedUnicastRange} for the provisioner
     * @param groupRange   {@link AllocatedGroupRange} for the provisioner
     * @param sceneRange   {@link AllocatedSceneRange} for the provisioner
     * @return {@link Provisioner}
     * @throws IllegalArgumentException if the name is empty
     */
    @SuppressWarnings("ConstantConditions")
    public Provisioner createProvisioner(@NonNull final String name,
                                         @NonNull final AllocatedUnicastRange unicastRange,
                                         @NonNull final AllocatedGroupRange groupRange,
                                         @NonNull final AllocatedSceneRange sceneRange) throws IllegalArgumentException {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        final List<AllocatedUnicastRange> unicastRanges = new ArrayList();
        final List<AllocatedGroupRange> groupRanges = new ArrayList();
        final List<AllocatedSceneRange> sceneRanges = new ArrayList();
        unicastRanges.add(unicastRange != null ? unicastRange : new AllocatedUnicastRange(0x0001, 0x7FFF));
        groupRanges.add(groupRange != null ? groupRange : new AllocatedGroupRange(0xC000, 0xFEFF));
        sceneRanges.add(sceneRange != null ? sceneRange : new AllocatedSceneRange(0x0001, 0xFFFF));
        final Provisioner provisioner = new Provisioner(UUID.randomUUID().toString(), unicastRanges, groupRanges, sceneRanges, meshUUID);
        provisioner.setProvisionerName(name);
        return provisioner;
    }

    /**
     * Adds a provisioner to the network
     *
     * @param provisioner {@link Provisioner}
     * @throws IllegalArgumentException if unicast address is invalid, in use by a node
     */
    public boolean addProvisioner(@NonNull final Provisioner provisioner) throws IllegalArgumentException {

        if (provisioner.allocatedUnicastRanges.isEmpty()) {
            if (provisioner.getProvisionerAddress() != null) {
                throw new IllegalArgumentException("Provisioner has no allocated unicast range assigned.");
            }
        }

        for (Provisioner other : provisioners) {
            if (provisioner.hasOverlappingUnicastRanges(other.getAllocatedUnicastRanges())
                    || provisioner.hasOverlappingGroupRanges(other.getAllocatedGroupRanges())
                    || provisioner.hasOverlappingSceneRanges(other.getAllocatedSceneRanges())) {
                throw new IllegalArgumentException("Provisioner ranges overlap.");
            }
        }

        if (!provisioner.isAddressWithinAllocatedRange(provisioner.getProvisionerAddress())) {
            throw new IllegalArgumentException("Unicast address assigned to a provisioner must be within an allocated unicast address range.");
        }

        if (isAddressInUse(provisioner.getProvisionerAddress())) {
            throw new IllegalArgumentException("Unicast address is in use by another node.");
        }

        if (provisioner.isNodeAddressInUse(nodes)) {
            throw new IllegalArgumentException("Unicast address is already in use.");
        }

        if (isProvisionerUuidInUse(provisioner.getProvisionerUuid())) {
            throw new IllegalArgumentException("Provisioner uuid already in use.");
        }

        provisioner.assignProvisionerAddress(provisioner.getProvisionerAddress());
        provisioners.add(provisioner);
        notifyProvisionerAdded(provisioner);
        if (provisioner.isLastSelected()) {
            selectProvisioner(provisioner);
        }
        if (provisioner.getProvisionerAddress() != null) {
            final ProvisionedMeshNode node = new ProvisionedMeshNode(provisioner, netKeys, appKeys);
            nodes.add(node);
            notifyNodeAdded(node);
        }
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
            throw new IllegalArgumentException("Provisioner does not exist, consider adding a provisioner first.");
        }

        if (provisioner.allocatedUnicastRanges.isEmpty()) {
            if (provisioner.getProvisionerAddress() != null) {
                throw new IllegalArgumentException("Provisioner has no allocated unicast range assigned.");
            }
        }

        for (Provisioner other : provisioners) {
            if (!other.getProvisionerUuid().equalsIgnoreCase(provisioner.getProvisionerUuid())) {
                if (provisioner.hasOverlappingUnicastRanges(other.getAllocatedUnicastRanges())
                        || provisioner.hasOverlappingGroupRanges(other.getAllocatedGroupRanges())
                        || provisioner.hasOverlappingSceneRanges(other.getAllocatedSceneRanges())) {
                    throw new IllegalArgumentException("Provisioner ranges overlap.");
                }
            }
        }

        if (!provisioner.isAddressWithinAllocatedRange(provisioner.getProvisionerAddress())) {
            throw new IllegalArgumentException("Unicast address assigned to a provisioner must be within an allocated unicast address range.");
        }

        for (ProvisionedMeshNode node : nodes) {
            if (!node.getUuid().equalsIgnoreCase(provisioner.getProvisionerUuid())) {
                if (provisioner.getProvisionerAddress() != null) {
                    if (node.getUnicastAddress() == provisioner.getProvisionerAddress()) {
                        throw new IllegalArgumentException("Unicast address is in use by another node.");
                    }
                }
            }
        }

        if (provisioner.isNodeAddressInUse(nodes)) {
            throw new IllegalArgumentException("Unicast address is already in use by another provisioner.");
        }

        boolean provisionerExists = false;
        for (int i = 0; i < provisioners.size(); i++) {
            final Provisioner p = provisioners.get(i);
            if (p.getProvisionerUuid().equalsIgnoreCase(provisioner.getProvisionerUuid())) {
                provisioners.set(i, provisioner);
                provisionerExists = true;
            }
        }
        boolean nodeExists = false;
        if (provisionerExists) {
            if (provisioner.getProvisionerAddress() != null) {
                ProvisionedMeshNode node = getNode(provisioner.getProvisionerUuid());
                if (node == null) {
                    node = new ProvisionedMeshNode(provisioner, netKeys, appKeys);
                    provisioner.setSequenceNumber(node.getSequenceNumber());
                    nodes.add(node);
                    notifyNodeAdded(node);
                } else {
                    for (int i = 0; i < nodes.size(); i++) {
                        final ProvisionedMeshNode meshNode = nodes.get(i);
                        if (meshNode.getUuid().equalsIgnoreCase(provisioner.getProvisionerUuid())) {
                            final int sequenceNumber;
                            if (meshNode.getUnicastAddress() != provisioner.getProvisionerAddress()) {
                                sequenceNumber = sequenceNumbers.get(provisioner.getProvisionerAddress());
                            } else {
                                sequenceNumber = sequenceNumbers.get(node.getUnicastAddress(), 0);
                            }
                            provisioner.setSequenceNumber(sequenceNumber);
                            node = new ProvisionedMeshNode(provisioner, netKeys, appKeys);
                            nodes.set(i, node);
                            notifyNodeUpdated(node);
                            break;
                        }
                    }
                }
            }
            if (provisioner.isLastSelected()) {
                selectProvisioner(provisioner);
            }
            return true;
        }
        return false;
    }

    /**
     * Update provisioner
     *
     * @param provisioner {@link Provisioner}
     * @return returns true if updated and false otherwise
     */
    public boolean disableConfigurationCapabilities(@NonNull final Provisioner provisioner) {
        final ProvisionedMeshNode node = getNode(provisioner.getProvisionerUuid());
        if (node == null)
            return true;
        else if (nodes.remove(node)) {
            provisioner.assignProvisionerAddress(null);
            notifyNodeDeleted(node);
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
            if (provisioner.getProvisionerAddress() != null) {
                final ProvisionedMeshNode node = getNode(provisioner.getProvisionerAddress());
                if (node != null) {
                    deleteNode(node);
                    notifyNodeDeleted(node);
                }
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
        notifyProvisionersUpdated(provisioners);
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
            if (node.hasUnicastAddress(MeshAddress.addressBytesToInt(unicastAddress))) {
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
     * Returns the mesh node with the corresponding unicast address
     *
     * @param uuid unicast address of the node
     */
    public ProvisionedMeshNode getNode(final String uuid) {
        for (ProvisionedMeshNode node : nodes) {
            if (node.getUuid().equalsIgnoreCase(uuid)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Update node name
     *
     * @param node {@link ProvisionedMeshNode}
     * @param name Name
     * @return true if successful and false otherwise
     */
    public boolean updateNodeName(@NonNull ProvisionedMeshNode node, @NonNull final String name) {
        if (TextUtils.isEmpty(name))
            return false;
        final ProvisionedMeshNode meshNode = getNode(node.getUuid());
        if (meshNode == null)
            return false;
        meshNode.setNodeName(name);
        notifyNodeUpdated(meshNode);
        return true;
    }

    /**
     * Deletes a mesh node from the list of provisioned nodes
     *
     * <p>
     * Note that deleting a node manually will not reset the node, but only be deleted from the stored list of provisioned nodes.
     * However you may still be able to connect to the same node, if it was not reset since the network may still exist. This
     * would be useful to in case if a node was physically reset and needs to be removed from the mesh network/db
     * </p>
     *
     * @param meshNode node to be deleted
     * @return true if deleted and false otherwise
     */
    public boolean deleteNode(@NonNull final ProvisionedMeshNode meshNode) {
        //Let's go through the nodes and delete if a node exists
        boolean nodeDeleted = false;
        for (ProvisionedMeshNode node : nodes) {
            if (node.getUuid().equalsIgnoreCase(meshNode.getUuid())) {
                nodes.remove(node);
                notifyNodeDeleted(meshNode);
                nodeDeleted = true;
                break;
            }
        }
        //We must also check if there is a provisioner based on the node we deleted
        if (nodeDeleted) {
            for (Provisioner provisioner : provisioners) {
                if (provisioner.getProvisionerUuid().equalsIgnoreCase(meshNode.getUuid())) {
                    provisioners.remove(provisioner);
                    notifyProvisionerDeleted(provisioner);
                    return true;
                }
            }
        }
        return nodeDeleted;
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
     * @param element {@link Element}
     * @param name    Name
     * @return true if successful and false otherwise
     * @throws IllegalArgumentException if name is empty
     */
    public boolean updateElementName(@NonNull final Element element, @NonNull final String name) throws IllegalArgumentException {
        if (TextUtils.isEmpty(name))
            throw new IllegalArgumentException("Element name cannot be empty.");

        final ProvisionedMeshNode node = getNode(element.getElementAddress());
        if (node != null) {
            if (node.getElements().containsKey(element.getElementAddress())) {
                element.setName(name);
                node.getElements().put(element.getElementAddress(), element);
                notifyNodeUpdated(node);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the sequence numbers used by the network where key is the address and the value being the sequence number.
     */
    public SparseIntArray getSequenceNumbers() {
        return sequenceNumbers.clone();
    }

    /**
     * Sets the sequence number. This method is used for internal use only, changing will result messages to fail
     *
     * @param sequenceNumbers Sequence numbers used in the network
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setSequenceNumbers(@NonNull final SparseIntArray sequenceNumbers) {
        this.sequenceNumbers = sequenceNumbers;
    }

    /**
     * Returns the sequence number for a given address
     *
     * @param address Address
     * @return sequence number or zero if address not found
     */
    protected Integer getSequenceNumber(final int address) {
        return sequenceNumbers.get(address, 0);
    }

    /**
     * Loads the sequence numbers known to the network
     */
    protected void loadSequenceNumbers() {
        for (ProvisionedMeshNode node : nodes) {
            sequenceNumbers.put(node.getUnicastAddress(), node.getSequenceNumber());
        }
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

    final void notifyProvisionersUpdated(@NonNull final List<Provisioner> provisioner) {
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
