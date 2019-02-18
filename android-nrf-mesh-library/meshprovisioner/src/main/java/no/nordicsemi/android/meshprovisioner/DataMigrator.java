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
import java.util.UUID;

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
    @SuppressWarnings("deprecation")
    static MeshNetwork migrateData(final Context context, final Gson gson) {
        final ProvisioningSettings provisioningSettings = new ProvisioningSettings(context);
        if (sharedPrefsExists(context)) {
            final MeshNetwork meshNetwork = new MeshNetwork(UUID.randomUUID().toString().toUpperCase(Locale.US));
            final SharedPreferences preferences = context.getSharedPreferences(PREFS_SEQUENCE_NUMBER, Context.MODE_PRIVATE);
            final int sequenceNumber = preferences.getInt(SEQUENCE_NUMBER_KEY, 0);
            final byte[] srcAddress = initConfigurationSrc(context);

            final SharedPreferences preferencesNodes = context.getSharedPreferences(PROVISIONED_NODES_FILE, Context.MODE_PRIVATE);
            final Map<String, ?> nodes = preferencesNodes.getAll();

            if (!nodes.isEmpty()) {
                final List<Integer> orderedKeys = reOrderProvisionedNodes(nodes);
                final List<ProvisionedMeshNode> tempNodes = new ArrayList<>();
                for (int orderedKey : orderedKeys) {
                    final String key = String.format(Locale.US, "0x%04X", orderedKey);
                    final String json = preferencesNodes.getString(key, null);
                    if (json != null) {
                        try {
                            final ProvisionedMeshNode node = gson.fromJson(json, ProvisionedMeshNode.class);
                            fixInvalidParsing(node);
                            //TODO Temporary check to handle data migration this is to be removed in the version after next
                            if (node.getAddedNetworkKeys().isEmpty()) {
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
                            //Since the current version of the app does not store the unique provisionerUuid of the
                            //device we manually generate one for exporting purposes as a work around
                            node.setUuid(UUID.randomUUID().toString().toUpperCase(Locale.US));
                            node.setMeshUuid(meshNetwork.meshUUID);
                            final Features features = new Features(node.isFriendFeatureSupported() ? Features.DISABLED : Features.UNSUPPORTED,
                                    node.isLowPowerFeatureSupported() ? Features.DISABLED : Features.UNSUPPORTED,
                                    node.isProxyFeatureSupported() ? Features.DISABLED : Features.UNSUPPORTED,
                                    node.isRelayFeatureSupported() ? Features.DISABLED : Features.UNSUPPORTED);
                            node.setNodeFeatures(features);
                            tempNodes.add(node);

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                meshNetwork.ivIndex = provisioningSettings.getIvIndex();
                meshNetwork.nodes = tempNodes;
                meshNetwork.netKeys = migrateNetKeys(meshNetwork, provisioningSettings);
                meshNetwork.appKeys = migrateAppKeys(meshNetwork, provisioningSettings);
                meshNetwork.unicastAddress = provisioningSettings.getUnicastAddress();
                meshNetwork.provisioners = migrateProvisioner(meshNetwork, srcAddress, sequenceNumber, provisioningSettings);

            }
            //Remove redundant preferences file
            removeSharedPrefs(context);
            return meshNetwork;
        }
        return null;
    }

    private static boolean sharedPrefsExists(final Context context) {
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void removeSharedPrefs(final Context context) {
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

    private static List<NetworkKey> migrateNetKeys(final MeshNetwork meshNetwork, final ProvisioningSettings settings) {
        final NetworkKey networkKey = new NetworkKey(settings.keyIndex, MeshParserUtils.toByteArray(settings.getNetworkKey()));
        networkKey.setMeshUuid(meshNetwork.getMeshUUID());
        final List<NetworkKey> netKeys = new ArrayList<>();
        netKeys.add(networkKey);
        return netKeys;
    }

    private static List<ApplicationKey> migrateAppKeys(final MeshNetwork meshNetwork, final ProvisioningSettings settings) {
        final List<ApplicationKey> appKeys = new ArrayList<>();
        for (int i = 0; i < settings.getAppKeys().size(); i++) {
            final ApplicationKey applicationKey = new ApplicationKey(i, MeshParserUtils.toByteArray(settings.getAppKeys().get(i)));
            applicationKey.setMeshUuid(meshNetwork.getMeshUUID());
            appKeys.add(applicationKey);
        }
        return appKeys;
    }

    private static List<Provisioner> migrateProvisioner(final MeshNetwork meshNetwork, final byte[] srcAddress, final int sequenceNumber, final ProvisioningSettings settings) {

        final List<Provisioner> provisioners = new ArrayList<>();
        if (meshNetwork.provisioners == null || meshNetwork.provisioners.isEmpty()) {

            final Provisioner provisioner = new Provisioner();
            provisioner.setProvisionerAddress(AddressUtils.getUnicastAddressInt(srcAddress));
            provisioner.setSequenceNumber(sequenceNumber);
            provisioner.setGlobalTtl(settings.getGlobalTtl());
            if (TextUtils.isEmpty(provisioner.getProvisionerUuid())) {
                provisioner.setProvisionerUuid(UUID.randomUUID().toString().toUpperCase(Locale.US));
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

            provisioner.setAllocatedGroupRanges(groupRanges);
            provisioner.setAllocatedUnicastRanges(unicastRanges);
            provisioner.setAllocatedSceneRanges(sceneRanges);
            provisioner.setMeshUuid(meshNetwork.getMeshUUID());

            provisioners.add(provisioner);
        }
        return provisioners;
    }

    private static byte[] initConfigurationSrc(final Context context) {
        final SharedPreferences preferences = context.getSharedPreferences(CONFIGURATION_SRC, Context.MODE_PRIVATE);
        final int tempSrc = preferences.getInt(SRC, 0);
        if (tempSrc != 0)
            return new byte[]{(byte) ((tempSrc >> 8) & 0xFF), (byte) (tempSrc & 0xFF)};
        return new byte[]{0x7F, (byte) 0xFF};
    }

    /**
     * Order the keys so that the nodes are read in insertion order
     *
     * @param nodes list containing unordered nodes
     */
    private static List<Integer> reOrderProvisionedNodes(final Map<String, ?> nodes) {
        final Set<String> unorderedKeys = nodes.keySet();
        final List<Integer> orderedKeys = new ArrayList<>();
        for (String k : unorderedKeys) {
            final int key = Integer.decode(k);
            orderedKeys.add(key);
        }
        Collections.sort(orderedKeys);
        return orderedKeys;
    }

    //TODO working around to be removed, very dirty fix and was added to stop breaking when importing existing networks.
    //This may only work for certain companies due to invalid parsing in the previous version of the app,
    //so please bear with the stupidity.
    private static void fixInvalidParsing(final ProvisionedMeshNode node){
        if(node.getProductIdentifier() != null) {
            if (node.getProductIdentifier() < 0) {
                node.setProductIdentifier(node.getProductIdentifier() & 0x00FF);
            }
        }

        if(node.getCompanyIdentifier() != null) {
            if (node.getCompanyIdentifier() < 0) {
                node.setCompanyIdentifier(node.getCompanyIdentifier() & 0x00FF);
            }
        }

        if(node.getVersionIdentifier() != null) {
            if (node.getVersionIdentifier() < 0) {
                node.setVersionIdentifier(node.getVersionIdentifier() & 0x00FF);
            }
        }

        if(node.getCrpl() != null) {
            if (node.getCrpl() < 0) {
                node.setCrpl(node.getCrpl() & 0x00FF);
            }
        }
    }
}
