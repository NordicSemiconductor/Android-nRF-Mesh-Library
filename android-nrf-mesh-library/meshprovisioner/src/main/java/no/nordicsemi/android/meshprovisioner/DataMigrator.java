package no.nordicsemi.android.meshprovisioner;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.AddressUtils;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

class DataMigrator {

    private static final String APPLICATION_KEYS = "APPLICATION_KEYS";
    private static final String PROVISIONING_DATA = "PROVISIONING_DATA";
    private static final String PROVISIONED_NODES_FILE = "PROVISIONED_FILES";
    private static final String CONFIGURATION_SRC = "CONFIGURATION_SRC";
    private static final String SRC = "SRC";
    private static final String PREFS_SEQUENCE_NUMBER = "PREFS_SEQUENCE_NUMBER";
    private static final String SEQUENCE_NUMBER_KEY = "NRF_MESH_SEQUENCE_NUMBER";

    /**
     * Load serialized provisioned nodes from preferences
     */
    final MeshNetwork migrateData(final Context context, final Gson gson, final ProvisioningSettings provisioningSettings) {
        if (sharedPrefsExists(context)) {
            final MeshNetwork meshNetwork = new MeshNetwork();
            final SharedPreferences preferences = context.getSharedPreferences(PREFS_SEQUENCE_NUMBER, Context.MODE_PRIVATE);
            final int sequenceNumber = preferences.getInt(SEQUENCE_NUMBER_KEY, 0);
            final byte[] srcAddress = initConfigurationSrc(context);

            final SharedPreferences preferencesNodes = context.getSharedPreferences(PROVISIONED_NODES_FILE, Context.MODE_PRIVATE);
            final Map<String, ?> nodes = preferencesNodes.getAll();

            if (TextUtils.isEmpty(meshNetwork.getMeshUUID())) {
                meshNetwork.setMeshUUID(MeshParserUtils.generateRandomUuid());
            }

            if (!nodes.isEmpty()) {
                final List<Integer> orderedKeys = reOrderProvisionedNodes(nodes);
                final List<ProvisionedMeshNode> tempNodes = new ArrayList<>();
                for (int orderedKey : orderedKeys) {
                    final String key = String.format(Locale.US, "0x%04X", orderedKey);
                    final String json = preferencesNodes.getString(key, null);
                    if (json != null) {
                        try {
                            final ProvisionedMeshNode node = gson.fromJson(json, ProvisionedMeshNode.class);
                            //TODO Temporary check to handle data migration this is to be removed in the version after next
                            if (node.getNetworkKeys().isEmpty()) {
                                final Method tempMigrateNetworkKey = node.getClass().getDeclaredMethod("tempMigrateNetworkKey");
                                tempMigrateNetworkKey.setAccessible(true);
                                tempMigrateNetworkKey.invoke(node);

                                final Method tempMigrateAddedApplicationKeys = node.getClass().getDeclaredMethod("tempMigrateAddedApplicationKeys");
                                tempMigrateAddedApplicationKeys.setAccessible(true);
                                tempMigrateAddedApplicationKeys.invoke(node);

                                final Method tempMigrateBoundApplicationKeys = node.getClass().getDeclaredMethod("tempMigrateBoundApplicationKeys");
                                tempMigrateBoundApplicationKeys.setAccessible(true);
                                tempMigrateBoundApplicationKeys.invoke(node);
                            } else {
                                //TODO Temporary check to handle data migration this is to be removed in the version after next
                                final GsonBuilder gsonBuilder = new GsonBuilder();
                                gsonBuilder.excludeFieldsWithoutExposeAnnotation();
                                gsonBuilder.enableComplexMapKeySerialization();
                                gsonBuilder.setPrettyPrinting();
                            }
                            final int unicastAddress = AddressUtils.getUnicastAddressInt(node.getUnicastAddress());
                            //Since the current version of the app does not store the unique provisionerUuid of the
                            //device we manually generate one for exporting purposes as a work around
                            node.setUuid(MeshParserUtils.generateRandomUuid());
                            node.setMeshUuid(meshNetwork.meshUUID);

                            tempNodes.add(node);

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                meshNetwork.nodes = tempNodes;
                meshNetwork.netKeys = migrateNetKeys(meshNetwork, provisioningSettings);
                meshNetwork.appKeys = migrateAppKeys(meshNetwork, provisioningSettings);
                meshNetwork.provisioners = migrateProvisioner(meshNetwork, srcAddress, sequenceNumber, provisioningSettings.getGlobalTtl());

            }
            //Remove redundant preferences file
            removeSharedPrefs(context);
            return meshNetwork;
        }
        return null;
    }

    private boolean sharedPrefsExists(final Context context) {
        final File directory = new File(context.getCacheDir().getParent() + "/shared_prefs");
        if (directory.exists()) {
            final File[] files = directory.listFiles();
            for (final File file : files) {
                final String name = file.getName();
                if (name.startsWith(PROVISIONING_DATA) ||
                        name.startsWith(PROVISIONED_NODES_FILE) ||
                        name.startsWith(PREFS_SEQUENCE_NUMBER) ||
                        name.startsWith(APPLICATION_KEYS)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void removeSharedPrefs(final Context context) {
        final File directory = new File(context.getCacheDir().getParent() + "/shared_prefs");
        if (directory.exists()) {
            final File[] files = directory.listFiles();
            for (final File file : files) {
                final String name = file.getName();
                if (name.startsWith(PROVISIONING_DATA) ||
                        name.startsWith(PROVISIONED_NODES_FILE) ||
                        name.startsWith(PREFS_SEQUENCE_NUMBER) ||
                        name.startsWith(APPLICATION_KEYS)) {
                    file.delete();
                }
            }
        }
    }

    private List<NetworkKey> migrateNetKeys(final MeshNetwork meshNetwork, final ProvisioningSettings settings) {
        final NetworkKey networkKey = new NetworkKey(settings.keyIndex, MeshParserUtils.toByteArray(settings.getNetworkKey()));
        networkKey.setMeshUuid(meshNetwork.getMeshUUID());
        final List<NetworkKey> netKeys = new ArrayList<>();
        netKeys.add(networkKey);
        return netKeys;
    }

    private List<ApplicationKey> migrateAppKeys(final MeshNetwork meshNetwork, final ProvisioningSettings settings) {
        final List<ApplicationKey> appKeys = new ArrayList<>();
        for (int i = 0; i < settings.getAppKeys().size(); i++) {
            final ApplicationKey applicationKey = new ApplicationKey(i, MeshParserUtils.toByteArray(settings.getAppKeys().get(i)));
            applicationKey.setMeshUuid(meshNetwork.getMeshUUID());
            appKeys.add(applicationKey);
        }
        return appKeys;
    }

    private List<Provisioner> migrateProvisioner(final MeshNetwork meshNetwork, final byte[] srcAddress, final int sequenceNumber, final int globalTtl) {

        final List<Provisioner> provisioners = new ArrayList<>();
        if (meshNetwork.provisioners == null || meshNetwork.provisioners.isEmpty()) {

            final Provisioner provisioner = new Provisioner();

            provisioner.setProvisionerAddress(srcAddress);
            provisioner.setSequenceNumber(sequenceNumber);
            provisioner.setGlobalTtl(globalTtl);
            if (TextUtils.isEmpty(provisioner.getProvisionerUuid())) {
                provisioner.setProvisionerUuid(MeshParserUtils.generateRandomUuid());
            }

            final AllocatedGroupRange groupRange = new AllocatedGroupRange(new byte[]{(byte) 0xC0, 0x00}, new byte[]{(byte) 0xFE, (byte) 0xFF});
            groupRange.setProvisionerUuid(provisioner.getProvisionerUuid());
            final AllocatedUnicastRange unicastRange = new AllocatedUnicastRange(new byte[]{(byte) 0x00, 0x00}, new byte[]{(byte) 0x7F, (byte) 0xFF});
            unicastRange.setProvisionerUuid(provisioner.getProvisionerUuid());
            final AllocatedSceneRange sceneRange = new AllocatedSceneRange(0, 0);
            sceneRange.setProvisionerUuid(provisioner.getProvisionerUuid());

            provisioner.setMeshUuid(meshNetwork.getMeshUUID());

            final List<AllocatedGroupRange> groupRanges = new ArrayList<>();
            groupRanges.add(groupRange);
            final List<AllocatedUnicastRange> unicastRanges = new ArrayList<>();
            unicastRanges.add(unicastRange);
            final List<AllocatedSceneRange> sceneRanges = new ArrayList<>();
            sceneRanges.add(sceneRange);

            provisioner.setAllocatedGroupRange(groupRanges);
            provisioner.setAllocatedUnicastRange(unicastRanges);
            provisioner.setAllocatedSceneRange(sceneRanges);
            provisioner.setMeshUuid(meshNetwork.getMeshUUID());

            provisioners.add(provisioner);
        }
        return provisioners;
    }

    private byte[] initConfigurationSrc(final Context context) {
        final SharedPreferences preferences = context.getSharedPreferences(CONFIGURATION_SRC, Context.MODE_PRIVATE);
        final int tempSrc = preferences.getInt(SRC, 0);
        if (tempSrc != 0)
            return new byte[]{(byte) ((tempSrc >> 8) & 0xFF), (byte) (tempSrc & 0xFF)};
        return null;
    }

    /**
     * Order the keys so that the nodes are read in insertion order
     *
     * @param nodes list containing unordered nodes
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
}
