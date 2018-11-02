package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import no.nordicsemi.android.meshprovisioner.Group;
import no.nordicsemi.android.meshprovisioner.Provisioner;
import no.nordicsemi.android.meshprovisioner.ProvisioningSettings;
import no.nordicsemi.android.meshprovisioner.Scene;
import no.nordicsemi.android.meshprovisioner.utils.AddressUtils;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

abstract class BaseMeshNetwork {

    private static final String PROVISIONED_NODES_FILE = "PROVISIONED_FILES";
    private static final String CONFIGURATION_SRC = "CONFIGURATION_SRC";
    private static final String SRC = "SRC";
    private Context mContext;
    private Gson mGson;

    @SerializedName("$schema")
    @Expose
    String schema;
    @SerializedName("id")
    @Expose
    String id;
    @SerializedName("version")
    @Expose
    String version;
    @SerializedName("meshUUID")
    @Expose
    String meshUUID;
    @SerializedName("meshName")
    @Expose
    String meshName;
    @SerializedName("timestamp")
    @Expose
    String timestamp;
    @SerializedName("netKeys")
    @Expose
    List<NetworkKey> netKeys = null;
    @SerializedName("appKeys")
    @Expose
    List<ApplicationKey> appKeys = null;
    @SerializedName("provisioners")
    @Expose
    List<Provisioner> provisioners = null;
    @SerializedName("nodes")
    @Expose
    List<ProvisionedMeshNode> nodes = null;
    @SerializedName("groups")
    @Expose
    List<Group> groups = null;
    @SerializedName("scenes")
    @Expose
    List<Scene> scenes = null;

    private ProvisioningSettings mProvisioningSettings;
    private Map<Integer, ProvisionedMeshNode> mProvisionedNodes = new LinkedHashMap<>();
    private byte[] mConfigurationSrc = {0x07, (byte) 0xFF}; //0x07FF;

    BaseMeshNetwork(final Context context) {
        mContext = context;
        this.mProvisioningSettings = new ProvisioningSettings(context);
        initGson();
        initConfigurationSrc();
        initProvisionedNodesForMigration();
    }

    BaseMeshNetwork() {

    }

    public final Map<Integer, ProvisionedMeshNode> getProvisionedNodes() {
        return Collections.unmodifiableMap(mProvisionedNodes);
    }

    public void addMeshNode(final ProvisionedMeshNode node) {
        mProvisionedNodes.put(node.getUnicastAddressInt(), node);
    }

    public ProvisioningSettings getProvisioningSettings() {
        return mProvisioningSettings;
    }

    private void initGson() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.excludeFieldsWithoutExposeAnnotation();
        gsonBuilder.enableComplexMapKeySerialization();
        gsonBuilder.registerTypeAdapter(MeshModel.class, new InternalMeshModelDeserializer());
        gsonBuilder.setPrettyPrinting();
        mGson = gsonBuilder.create();
    }

    /**
     * Load serialized provisioned nodes from preferences
     */
    private void initProvisionedNodes() {
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONED_NODES_FILE, Context.MODE_PRIVATE);
        final Map<String, ?> nodes = preferences.getAll();

        if (!nodes.isEmpty()) {
            final List<Integer> orderedKeys = reOrderProvisionedNodes(nodes);
            mProvisionedNodes.clear();
            for (int orderedKey : orderedKeys) {
                final String key = String.format(Locale.US, "0x%04X", orderedKey);
                final String json = preferences.getString(key, null);
                if (json != null) {
                    try {
                        final ProvisionedMeshNode node = mGson.fromJson(json, ProvisionedMeshNode.class);
                        final int unicastAddress = AddressUtils.getUnicastAddressInt(node.getUnicastAddress());
                        mProvisionedNodes.put(unicastAddress, node);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Load serialized provisioned nodes from preferences
     */
    private void initProvisionedNodesForMigration() {
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONED_NODES_FILE, Context.MODE_PRIVATE);
        final Map<String, ?> nodes = preferences.getAll();

        if (!nodes.isEmpty()) {
            final List<Integer> orderedKeys = reOrderProvisionedNodes(nodes);
            mProvisionedNodes.clear();
            for (int orderedKey : orderedKeys) {
                final String key = String.format(Locale.US, "0x%04X", orderedKey);
                final String json = preferences.getString(key, null);
                if (json != null) {
                    try {
                        final ProvisionedMeshNode node = mGson.fromJson(json, ProvisionedMeshNode.class);
                        //TODO Temporary check to handle data migration this is to be removed in the version after next
                        if (node.getNetworkKeys().isEmpty()) {
                            final Method tempMigrateNetworkKey = node.getClass().getDeclaredMethod("tempMigrateNetworkKey");
                            if (tempMigrateNetworkKey != null) {
                                tempMigrateNetworkKey.setAccessible(true);
                                tempMigrateNetworkKey.invoke(node);
                            }
                            final Method tempMigrateAddedApplicationKeys = node.getClass().getDeclaredMethod("tempMigrateAddedApplicationKeys");
                            if (tempMigrateAddedApplicationKeys != null) {
                                tempMigrateAddedApplicationKeys.setAccessible(true);
                                tempMigrateAddedApplicationKeys.invoke(node);
                            }
                            final Method tempMigrateBoundApplicationKeys = node.getClass().getDeclaredMethod("tempMigrateBoundApplicationKeys");
                            if (tempMigrateBoundApplicationKeys != null) {
                                tempMigrateBoundApplicationKeys.setAccessible(true);
                                tempMigrateBoundApplicationKeys.invoke(node);
                            }
                        } else {
                            //TODO Temporary check to handle data migration this is to be removed in the version after next
                            final GsonBuilder gsonBuilder = new GsonBuilder();
                            gsonBuilder.excludeFieldsWithoutExposeAnnotation();
                            gsonBuilder.enableComplexMapKeySerialization();
                            gsonBuilder.setPrettyPrinting();
                            mGson = gsonBuilder.create();
                        }
                        final int unicastAddress = AddressUtils.getUnicastAddressInt(node.getUnicastAddress());
                        mProvisionedNodes.put(unicastAddress, node);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public final ProvisionedMeshNode getMeshNode(final int unicastAddress) {
        for (Map.Entry<Integer, ProvisionedMeshNode> entry : mProvisionedNodes.entrySet()) {
            if (entry.getValue().getElements() != null && entry.getValue().getElements().containsKey(unicastAddress)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Order the keys so that the nodes are read in insertion order
     *
     * @param nodes list containing unordered nodes
     * @return node list
     */
    private List<Integer> reOrderProvisionedNodes(final Map<String, ?> nodes) {
        final Set<String> unorderedKeys = nodes.keySet();
        final List<Integer> orderedKeys = new ArrayList<>();
        for (String k : unorderedKeys) {
            final int key = Integer.decode(k);
            orderedKeys.add(key);
        }
        Collections.sort(orderedKeys);
        return orderedKeys;
    }

    /**
     * Serialize and save provisioned node
     */
    public void saveProvisionedNode(final ProvisionedMeshNode node) {
        mProvisionedNodes.put(node.getUnicastAddressInt(), node);
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONED_NODES_FILE, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        final String unicastAddress = MeshParserUtils.bytesToHex(node.getUnicastAddress(), true);
        final String provisionedNode = mGson.toJson(node);
        editor.putString(unicastAddress, provisionedNode);
        editor.apply();
    }

    /**
     * Serialize and save all provisioned nodes
     */
    private void saveProvisionedNodes() {
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONED_NODES_FILE, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        for (Map.Entry<Integer, ProvisionedMeshNode> entry : mProvisionedNodes.entrySet()) {
            final ProvisionedMeshNode node = entry.getValue();
            final String unicastAddress = MeshParserUtils.bytesToHex(node.getUnicastAddress(), true);
            final String provisionedNode = mGson.toJson(node);
            editor.putString(unicastAddress, provisionedNode);
        }
        editor.apply();
    }

    /**
     * Serialize and save provisioned node
     */
    public void deleteProvisionedNode(final ProvisionedMeshNode node) {
        mProvisionedNodes.remove(node.getUnicastAddressInt());
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONED_NODES_FILE, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        final String unicastAddress = MeshParserUtils.bytesToHex(node.getUnicastAddress(), true);
        editor.remove(unicastAddress);
        editor.apply();
    }

    /**
     * Clear provisioned ndoes
     */
    public void clearProvisionedNodes() {
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONED_NODES_FILE, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    private void initConfigurationSrc() {
        final SharedPreferences preferences = mContext.getSharedPreferences(CONFIGURATION_SRC, Context.MODE_PRIVATE);
        final int tempSrc = preferences.getInt(SRC, 0);
        if (tempSrc != 0)
            mConfigurationSrc = new byte[]{(byte) ((tempSrc >> 8) & 0xFF), (byte) (tempSrc & 0xFF)};
    }

    private void saveSrc() {
        final SharedPreferences preferences = mContext.getSharedPreferences(CONFIGURATION_SRC, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(SRC, (mConfigurationSrc[0] & 0xFF) << 8 | (mConfigurationSrc[1] & 0xFF));
        editor.apply();
    }

    /**
     * Set the source unicast address to the the library in the mesh network. This method will check if the addres is already taken by a node
     *
     * @return true is successful
     */
    public boolean setConfiguratorSrc(final byte[] configurationSrc) throws IllegalArgumentException {
        final int tempSrc = (configurationSrc[0] & 0xFF) << 8 | (configurationSrc[1] & 0xFF);
        if (MeshParserUtils.validateUnicastAddressInput(mContext, tempSrc)) {
            if (!mProvisionedNodes.containsKey(tempSrc)) {
                mConfigurationSrc = configurationSrc;
                saveSrc();

                //Set the configuration source for all provisioned nodes
                for (Map.Entry<Integer, ProvisionedMeshNode> entry : mProvisionedNodes.entrySet()) {
                    entry.getValue().setConfigurationSrc(mConfigurationSrc);
                }

                //Save all nodes
                saveProvisionedNodes();
                return true;
            } else {
                throw new IllegalArgumentException("Address already occupied by a node");
            }
        }
        return false;
    }

    /**
     * Returns the source unicast address set to the the library in the mesh network
     *
     * @return byte array containing the address
     */
    public byte[] getConfiguratorSrc() {
        return mConfigurationSrc;
    }

    public void incrementUnicastAddress(final ProvisionedMeshNode meshNode) {
        //Since we know the number of elements this node contains we can predict the next available address for the next node.
        int unicastAdd = (meshNode.getUnicastAddressInt() + meshNode.getNumberOfElements());
        //We check if the incremented unicast address is already taken by the app/configurator
        final int tempSrc = (mConfigurationSrc[0] & 0xFF) << 8 | (mConfigurationSrc[1] & 0xFF);
        if (unicastAdd == tempSrc) {
            unicastAdd = unicastAdd + 1;
        }
        getProvisioningSettings().setUnicastAddress(unicastAdd);
    }

}
