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
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.ProxyFilter;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

@SuppressWarnings({"unused", "WeakerAccess"})
abstract class BaseMeshNetwork {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NORMAL_OPERATION, IV_UPDATE_ACTIVE})
    public @interface IvUpdateStates {
    }

    // Key refresh phases
    public static final int NORMAL_OPERATION = 0; //Distribution of new keys
    public static final int IV_UPDATE_ACTIVE = 1; //Switching to the new keys

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

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "mesh_uuid")
    @SerializedName("meshUUID")
    @Expose
    final String meshUUID;

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
    protected MeshNetworkCallbacks mCallbacks;

    @Ignore
    private final Comparator<ApplicationKey> appKeyComparator = (key1, key2) -> Integer.compare(key1.getKeyIndex(), key2.getKeyIndex());


    @Ignore
    private final Comparator<NetworkKey> netKeyComparator = (key1, key2) -> Integer.compare(key1.getKeyIndex(), key2.getKeyIndex());

    @Ignore
    @Expose(serialize = false, deserialize = false)
    private ProxyFilter proxyFilter;

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
     * @throws IllegalArgumentException if app key already exists
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
     * Set provisioner address
     *
     * @param address Unicast address
     * @return true if success, false if the address is in use by another device
     */
    public boolean setProvisionerAddress(final int address) {
        if (!isAddressInUse(address)) {
            final Provisioner provisioner = getSelectedProvisioner();
            provisioner.setProvisionerAddress(address);
            notifyProvisionerUpdated(provisioner);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the next available unicast address
     *
     * @return unicast address
     */
    public int getUnicastAddress() {
        return unicastAddress;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setUnicastAddress(final int address) {
        this.unicastAddress = address;
    }

    /**
     * Set a unicast address, to be assigned to a node
     *
     * @param unicastAddress unicast address
     * @return true if success, false if the address is in use by another device
     */
    public boolean assignUnicastAddress(final int unicastAddress) {
        if (isAddressInUse(unicastAddress))
            return false;

        this.unicastAddress = unicastAddress;
        notifyNetworkUpdated();
        return true;
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
     * Selects a provisioner if there are multiple provisioners.
     *
     * @param provisioner {@link Provisioner}
     */
    public final void selectProvisioner(final Provisioner provisioner) {
        for (Provisioner prov : provisioners) {
            prov.setLastSelected(false);
        }
        provisioner.setLastSelected(true);
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

    final void notifyNetKeyAdded(final NetworkKey networkKey) {
        if (mCallbacks != null) {
            mCallbacks.onNetworkKeyAdded(networkKey);
        }
    }

    final void notifyNetKeyUpdated(final NetworkKey networkKey) {
        if (mCallbacks != null) {
            mCallbacks.onNetworkKeyUpdated(networkKey);
        }
    }

    final void notifyNetKeyDeleted(final NetworkKey networkKey) {
        if (mCallbacks != null) {
            mCallbacks.onNetworkKeyDeleted(networkKey);
        }
    }

    final void notifyAppKeyAdded(final ApplicationKey appKey) {
        if (mCallbacks != null) {
            mCallbacks.onApplicationKeyAdded(appKey);
        }
    }

    final void notifyAppKeyUpdated(final ApplicationKey appKey) {
        if (mCallbacks != null) {
            mCallbacks.onApplicationKeyUpdated(appKey);
        }
    }

    final void notifyAppKeyDeleted(final ApplicationKey appKey) {
        if (mCallbacks != null) {
            mCallbacks.onApplicationKeyDeleted(appKey);
        }
    }

    final void notifyProvisionerUpdated(final Provisioner provisioner) {
        if (mCallbacks != null) {
            mCallbacks.onProvisionerUpdated(provisioner);
        }
    }

    final void notifyProvisionerUpdated(final List<Provisioner> provisioner) {
        if (mCallbacks != null) {
            mCallbacks.onProvisionerUpdated(provisioner);
        }
    }

    final void notifyNodeDeleted(final ProvisionedMeshNode meshNode) {
        if (mCallbacks != null) {
            mCallbacks.onNodeDeleted(meshNode);
        }
    }

    final void notifyNodesUpdated() {
        if (mCallbacks != null) {
            mCallbacks.onNodesUpdated();
        }
    }

    final void notifySceneAdded(final Scene scene) {
        if (mCallbacks != null) {
            mCallbacks.onSceneAdded(scene);
        }
    }

    final void notifySceneUpdated(final Scene scene) {
        if (mCallbacks != null) {
            mCallbacks.onSceneUpdated(scene);
        }
    }

    final void notifySceneDeleted(final Scene scene) {
        if (mCallbacks != null) {
            mCallbacks.onSceneDeleted(scene);
        }
    }

    final void notifyGroupAdded(final Group group) {
        if (mCallbacks != null) {
            mCallbacks.onGroupAdded(group);
        }
    }

    final void notifyGroupUpdated(final Group group) {
        if (mCallbacks != null) {
            mCallbacks.onGroupUpdated(group);
        }
    }

    final void notifyGroupDeleted(final Group group) {
        if (mCallbacks != null) {
            mCallbacks.onGroupDeleted(group);
        }
    }
}
