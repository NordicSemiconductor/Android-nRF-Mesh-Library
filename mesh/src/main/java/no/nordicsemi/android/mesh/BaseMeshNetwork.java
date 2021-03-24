package no.nordicsemi.android.mesh;

import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import no.nordicsemi.android.mesh.transport.ConfigAppKeyUpdate;
import no.nordicsemi.android.mesh.transport.ConfigKeyRefreshPhaseSet;
import no.nordicsemi.android.mesh.transport.ConfigNetKeyUpdate;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.mesh.utils.ProxyFilter;
import no.nordicsemi.android.mesh.utils.SecureUtils;

import static no.nordicsemi.android.mesh.NetworkKey.KeyRefreshPhase;
import static no.nordicsemi.android.mesh.NetworkKey.KeyRefreshPhaseTransition;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
abstract class BaseMeshNetwork {
    private static final String TAG = "BaseMeshNetwork";
    // Key refresh phases
    public static final int NORMAL_OPERATION = 0; //Normal operation
    public static final int IV_UPDATE_ACTIVE = 1; //IV Update active
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
    String id = "http://www.bluetooth.com/specifications/assigned-numbers/mesh-profile/cdb-schema.json#";
    @Ignore
    @SerializedName("version")
    @Expose
    String version = "1.0.0";
    @ColumnInfo(name = "mesh_name")
    @SerializedName("meshName")
    @Expose
    String meshName = "nRF Mesh Network";
    @ColumnInfo(name = "timestamp", defaultValue = "0")
    @SerializedName("timestamp")
    @Expose
    long timestamp = System.currentTimeMillis();
    @ColumnInfo(name = "partial", defaultValue = "0")
    @SerializedName("partial")
    @Expose
    boolean partial = false;
    @ColumnInfo(name = "iv_index")
    @TypeConverters(MeshTypeConverters.class)
    @Expose
    @NonNull
    IvIndex ivIndex = new IvIndex(0, false, Calendar.getInstance());
    //Properties with Ignore are stored in their own table with the network's UUID as the foreign key
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
    @SerializedName("networkExclusions")
    @TypeConverters(MeshTypeConverters.class)
    @NonNull
    @ColumnInfo(name = "network_exclusions", defaultValue = "{}")
    @Expose
    protected Map<Integer, ArrayList<Integer>> networkExclusions = new HashMap<>();
    //Library related attributes
    @Ignore
    @ColumnInfo(name = "unicast_address")
    @Expose
    int unicastAddress = 0x0001;
    @ColumnInfo(name = "last_selected")
    @Expose
    boolean lastSelected;
    @Ignore
    @Expose(serialize = false, deserialize = false)
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
    protected final Comparator<Scene> sceneComparator = (scene1, scene2) ->
            Integer.compare(scene1.getNumber(), scene2.getNumber());
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

    private boolean isNetKeyExists(@NonNull final byte[] key) {
        for (int i = 0; i < netKeys.size(); i++) {
            if (Arrays.equals(key, netKeys.get(i).getKey())) {
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
     * @param newNetKey Network key
     * @throws IllegalArgumentException if the key already exists.
     */
    public boolean addNetKey(@NonNull final NetworkKey newNetKey) {
        if (isNetKeyExists(newNetKey.getKey())) {
            throw new IllegalArgumentException("Net key already exists, check the contents of the key!");
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
     * <p>
     * Updating a NetworkKey's key value requires initiating a Key Refresh Procedure. A NetworkKey that's in use
     * would require a Key Refresh Procedure to update it's key contents. However a NetworkKey that's not in could
     * be updated without this procedure. If the key is in use, call {@link #distributeNetKey(NetworkKey, byte[])}
     * to initiate the Key Refresh Procedure.
     * </p>
     *
     * @param networkKey Network key
     * @param newNetKey  16-byte hexadecimal string
     * @throws IllegalArgumentException if the key is already in use
     */
    public boolean updateNetKey(@NonNull final NetworkKey networkKey, @NonNull final String newNetKey) throws IllegalArgumentException {
        if (MeshParserUtils.validateKeyInput(newNetKey)) {
            final byte[] key = MeshParserUtils.toByteArray(newNetKey);
            if (isNetKeyExists(key)) {
                throw new IllegalArgumentException("Net key value is already in use.");
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
                throw new IllegalArgumentException("Unable to update a network key that's already in use. ");
            }
        }
        return false;
    }

    /**
     * Update a network key in the mesh network.
     *
     * <p>
     * Updating a NetworkKey's key value requires initiating a Key Refresh Procedure. A NetworkKey that's in use
     * would require a Key Refresh Procedure to update it's key contents. However a NetworkKey that's not in could
     * be updated without this procedure. If the key is in use, call {@link #distributeNetKey(NetworkKey, byte[])}
     * to initiate the Key Refresh Procedure.
     * </p>
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
            // The name might be updated so we must update the key.
            return updateMeshKey(networkKey);
        } else {
            //If the keys are not the same we check if its in use before updating the key
            if (!isKeyInUse(key)) {
                //We check if the contents of the key are the same
                //This will return true only if the key index and the key are the same
                return updateMeshKey(networkKey);
            } else {
                throw new IllegalArgumentException("Unable to update a network key that's already in use.");
            }
        }
    }

    /**
     * Distribute Net Key will start the key refresh procedure and return the newly updated key.
     *
     * <p>
     * This process contains three phases.
     * {@link KeyRefreshPhase#KEY_DISTRIBUTION} - Distribution of the new Keys {@link #distributeNetKey(NetworkKey, byte[])}.
     * {@link KeyRefreshPhase#USING_NEW_KEYS} - Switching to the new keys {@link #switchToNewKey(NetworkKey)}.
     * {@link KeyRefreshPhase#REVOKE_OLD_KEYS} - Revoking old keys {@link #revokeOldKey(NetworkKey)}.
     * The new key is distributed to the provisioner node by setting the currently used key as the old key and setting the
     * currently used key to the new key value. This will change the phase of the network key to{@link KeyRefreshPhase#KEY_DISTRIBUTION}.
     * During this phase a node will transmit using the old key but may receive using both old and the new key. After a successful
     * distribution to the provisioner, the user may start sending {@link ConfigNetKeyUpdate} messages to the respective nodes in the
     * network that requires updating. In addition if the user wishes to update the AppKey call {@link #distributeAppKey(ApplicationKey, byte[])}
     * to update the Application Key on the provisioner and then distribute it to other nodes by sending {@link ConfigAppKeyUpdate} to
     * update an AppKey. However it shall be only successfully processed if the NetworkKey bound to the Application Key is in
     * {@link KeyRefreshPhase#KEY_DISTRIBUTION} and the received app key value is different or when the received AppKey value is the same as
     * previously received value. Also note that sending a ConfigNetKeyUpdate during {@link KeyRefreshPhase#NORMAL_OPERATION} will switch the
     * phase to {@link KeyRefreshPhase#KEY_DISTRIBUTION}. Once distribution is completed, call {@link #switchToNewKey(NetworkKey)} and
     * send {@link ConfigKeyRefreshPhaseSet} to other nodes.
     * </p>
     *
     * @param networkKey Network key
     * @param newNetKey  16-byte key
     * @throws IllegalArgumentException the key value is already in use.
     */
    public NetworkKey distributeNetKey(@NonNull final NetworkKey networkKey, @NonNull final byte[] newNetKey) throws IllegalArgumentException {
        if (validateKey(newNetKey)) {
            if (isNetKeyExists(newNetKey)) {
                throw new IllegalArgumentException("Net key value is already in use.");
            }

            final int keyIndex = networkKey.getKeyIndex();
            final NetworkKey netKey = getNetKey(keyIndex);
            if (netKey.equals(networkKey)) {
                if (netKey.distributeKey(newNetKey)) {
                    updateNodeKeyStatus(netKey);
                    if (updateMeshKey(netKey)) {
                        return netKey;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Updates the NodeKey object for Network or Application Keys
     *
     * @param meshKey Updated Key
     */
    private void updateNodeKeyStatus(@NonNull final MeshKey meshKey) {
        for (Provisioner provisioner : provisioners) {
            for (ProvisionedMeshNode node : nodes) {
                if (node.getUuid().equalsIgnoreCase(provisioner.getProvisionerUuid())) {
                    if (meshKey instanceof NetworkKey) {
                        for (NodeKey key : node.getAddedNetKeys()) {
                            if (key.getIndex() == meshKey.getKeyIndex()) {
                                key.setUpdated(true);
                            }
                        }
                    } else {
                        for (NodeKey key : node.getAddedAppKeys()) {
                            if (key.getIndex() == meshKey.getKeyIndex()) {
                                key.setUpdated(true);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Switches the new key, this will initiate the provisioner node transmitting messages using the new keys but will
     * support receiving messages using both old and the new key.
     *
     * <p>
     * This must be called after {@link #distributeNetKey(NetworkKey, byte[])}
     * </p>
     *
     * @param networkKey Network key to switch too
     * @return true if success or false otherwise
     * @throws IllegalArgumentException if the provided key is not the same as the distributed key.
     */
    public boolean switchToNewKey(@NonNull final NetworkKey networkKey) throws IllegalArgumentException {
        if (!netKeys.contains(networkKey)) {
            throw new IllegalArgumentException("Network Key not distributed");
        }
        return networkKey.switchToNewKey();
    }

    /**
     * Revokes the old key.
     * <p>
     * This initiates {@link KeyRefreshPhase#REVOKE_OLD_KEYS} of the Key Refresh Procedure in which user must send {@link ConfigKeyRefreshPhaseSet}
     * message with transition set to {@link KeyRefreshPhaseTransition#REVOKE_OLD_KEYS} to the other nodes going through the Key Refresh Procedure.
     * The library at this point will set the given Network Key's Phase to {@link KeyRefreshPhase#NORMAL_OPERATION}.
     * </p>
     *
     * @param networkKey Network key that was distributed
     * @return true if success or false otherwise
     */
    public boolean revokeOldKey(@NonNull final NetworkKey networkKey) {
        if (netKeys.contains(networkKey)) {
            return networkKey.revokeOldKey();
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

        if (isAppKeyExists(newAppKey.getKey())) {
            throw new IllegalArgumentException("App key already exists, check the contents of the key!");
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
     * <p>
     * Updates the Key if it is not use, if not Updating a Key's key value requires initiating a Key Refresh Procedure.
     * This requires the bound NetworkKey of the AppKey to be updated. A NetworkKey that's in use would require a
     * Key Refresh Procedure to update it's key contents. However a NetworkKey that's not in could be updated without this
     * procedure. If the key is in use, call {@link #distributeNetKey(NetworkKey, byte[])} to initiate the Key Refresh Procedure.
     * </p>
     *
     * @param applicationKey {@link ApplicationKey}
     * @param newAppKey      Application key
     * @throws IllegalArgumentException if the key is in use.
     */
    public boolean updateAppKey(@NonNull final ApplicationKey applicationKey, @NonNull final String newAppKey) throws IllegalArgumentException {
        if (MeshParserUtils.validateKeyInput(newAppKey)) {
            final byte[] key = MeshParserUtils.toByteArray(newAppKey);
            if (isNetKeyExists(key)) {
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
                throw new IllegalArgumentException("Unable to update a application key that's already in use.");
            }
        }
        return false;
    }

    /**
     * Updates an app key in the mesh network.
     *
     * <p>
     * Updates the Key if it is not use, if not Updating a Key's key value requires initiating a Key Refresh Procedure. This requires
     * the bound NetworkKey of the AppKey to be updated. A NetworkKey that's in use would require aKey Refresh Procedure to update
     * it's key contents. However a NetworkKey that's not in could be updated without this procedure. If the key is in use, call
     * {@link #distributeNetKey(NetworkKey, byte[])} to initiate the Key Refresh Procedure. After distributing the NetworkKey bound to
     * the Application Key, user may call {@link #distributeAppKey(ApplicationKey, byte[])} to update the corresponding ApplicationKey.
     * </p>
     *
     * @param applicationKey {@link ApplicationKey}
     * @throws IllegalArgumentException if the key is already in use
     */
    public boolean updateAppKey(@NonNull final ApplicationKey applicationKey) throws IllegalArgumentException {
        final int keyIndex = applicationKey.getKeyIndex();
        final ApplicationKey key = getAppKey(keyIndex);
        //If the keys are not the same we check if its in use before updating the key
        if (!isKeyInUse(key)) {
            //We check if the contents of the key are the same
            //This will return true only if the key index and the key are the same
            return updateMeshKey(applicationKey);
        } else {
            throw new IllegalArgumentException("Unable to update a application key that's already in use.");
        }
    }

    /**
     * Distributes/updates the provisioner node's the application key and returns the updated Application Key.
     *
     * <p>
     * This will only work if the NetworkKey bound to this ApplicationKey is in Phase 1 of the Key Refresh Procedure. Therefore the NetworkKey
     * must be updated first before updating it's bound application key. Call {@link #distributeNetKey(NetworkKey, byte[])} to initiate the
     * Key Refresh Procedure to update a Network Key that's in use by the provisioner or the nodes, if it has not been started already.
     * To update a key that's not in use call {@link #updateAppKey(ApplicationKey, String)}
     * <p>
     * Once the provisioner nodes' AppKey is updated user must distribute the updated AppKey to the nodes. This can be done by sending
     * {@link ConfigAppKeyUpdate} message with the new key.
     * </p>
     *
     * @param applicationKey Network key
     * @param newAppKey      16-byte key
     * @throws IllegalArgumentException the key value is already in use.
     */
    public ApplicationKey distributeAppKey(@NonNull final ApplicationKey applicationKey, @NonNull final byte[] newAppKey) throws IllegalArgumentException {
        if (validateKey(newAppKey)) {
            if (isAppKeyExists(newAppKey)) {
                throw new IllegalArgumentException("App key value is already in use.");
            }

            final int keyIndex = applicationKey.getKeyIndex();
            final ApplicationKey appKey = getAppKey(keyIndex);
            if (appKey.equals(applicationKey)) {
                if (appKey.distributeKey(newAppKey)) {
                    updateNodeKeyStatus(appKey);
                    if (updateMeshKey(appKey)) {
                        return appKey;
                    }
                }
            }
        }
        return null;
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
            throw new IllegalArgumentException("Unable to delete an app key that's in use.");
        } else {
            if (appKeys.remove(appKey)) {
                notifyAppKeyDeleted(appKey);
                return true;
            } else {
                throw new IllegalArgumentException("Key does not exist.");
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
    public Provisioner createProvisioner(@NonNull final String name,
                                         @NonNull final AllocatedUnicastRange unicastRange,
                                         @NonNull final AllocatedGroupRange groupRange,
                                         @NonNull final AllocatedSceneRange sceneRange) throws IllegalArgumentException {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        final List<AllocatedUnicastRange> unicastRanges = new ArrayList<>();
        final List<AllocatedGroupRange> groupRanges = new ArrayList<>();
        final List<AllocatedSceneRange> sceneRanges = new ArrayList<>();
        unicastRanges.add(unicastRange);
        groupRanges.add(groupRange);
        sceneRanges.add(sceneRange);
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
                                sequenceNumber = sequenceNumbers.get(node.getUnicastAddress(), node.getSequenceNumber());
                            }
                            node = new ProvisionedMeshNode(provisioner, netKeys, appKeys);
                            node.setSequenceNumber(sequenceNumber);
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

    protected boolean isProvisionerUuidInUse(@NonNull final String uuid) {
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
     * Returns the list of {@link ProvisionedMeshNode} containing the given network key
     *
     * @param networkKey Network Key
     */
    public List<ProvisionedMeshNode> getNodes(final NetworkKey networkKey) {
        final List<ProvisionedMeshNode> nodes = new ArrayList<>();
        for (ProvisionedMeshNode node : this.nodes) {
            for (NodeKey nodeKey : node.getAddedNetKeys()) {
                if (nodeKey.getIndex() == networkKey.getKeyIndex()) {
                    nodes.add(node);
                }
            }
        }
        return nodes;
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
                excludeNode(node);
                nodes.remove(node);
                notifyNodeDeleted(node);
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
                    break;
                }
            }
        }

        return nodeDeleted;
    }

    /**
     * Returns true if the given node is a provisioner node
     *
     * @param node {@link ProvisionedMeshNode}
     * @return True if the node is a provisioner or false otherwise
     */
    public boolean isProvisioner(@NonNull final ProvisionedMeshNode node) {
        for (Provisioner provisioner : provisioners) {
            if (provisioner.getProvisionerUuid().equalsIgnoreCase(node.getUuid())) {
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
     * Returns the map of network exclusions
     */
    public Map<Integer, ArrayList<Integer>> getNetworkExclusions() {
        return Collections.unmodifiableMap(networkExclusions);
    }

    /**
     * Setter required by room db and is restricted for internal use.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setNetworkExclusions(@NonNull final Map<Integer, ArrayList<Integer>> networkExclusions) {
        this.networkExclusions = networkExclusions;
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

    /**
     * Excludes a node from the mesh network.
     * The given node will marked as excluded and added to the exclusion list and the node will be removed once
     * the Key Refresh Procedure is completed. After the IV update procedure, when the network transitions to an
     * IV Normal Operation state with a higher IV index, the exclusionList object that has the ivIndex property
     * value that is lower by a count of two (or more) than the current IV index of the network is removed from
     * the networkExclusions property array.
     *
     * @param node Provisioned mesh node.
     */
    private void excludeNode(@NonNull final ProvisionedMeshNode node) {
        //Exclude node
        node.setExcluded(true);
        notifyNodeUpdated(node);
        ArrayList<Integer> addresses = networkExclusions.get(ivIndex.getIvIndex());
        if (addresses == null) {
            addresses = new ArrayList<>();
        }

        for (Integer address : node.getElements().keySet()) {
            if (!addresses.contains(address)) {
                addresses.add(address);
            }
        }

        networkExclusions.put(ivIndex.getIvIndex(), addresses);
        notifyNetworkUpdated();
    }

    private boolean validateKey(@NonNull final byte[] key) {
        if (key.length != 16)
            throw new IllegalArgumentException("Key must be 16 bytes");
        return true;
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
