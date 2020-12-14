package no.nordicsemi.android.mesh;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.InternalElementListDeserializer;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.MeshModelListDeserializer;
import no.nordicsemi.android.mesh.transport.NodeDeserializer;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;

import static no.nordicsemi.android.mesh.utils.MeshAddress.isValidGroupAddress;

/**
 * Utility class to handle network imports and exports
 */
class ImportExportUtils {

    private static final String TAG = ImportExportUtils.class.getSimpleName();
    private final Gson mGson;

    ImportExportUtils() {
        mGson = initGson();
    }

    /**
     * Initializes the Gson based on the network export type.
     */
    private Gson initGson() {
        Type netKeyList = new TypeToken<List<NetworkKey>>() {
        }.getType();
        Type appKeyList = new TypeToken<List<ApplicationKey>>() {
        }.getType();
        Type allocatedUnicastRange = new TypeToken<List<AllocatedUnicastRange>>() {
        }.getType();
        Type allocatedGroupRange = new TypeToken<List<AllocatedGroupRange>>() {
        }.getType();
        Type allocatedSceneRange = new TypeToken<List<AllocatedSceneRange>>() {
        }.getType();
        Type nodeList = new TypeToken<List<ProvisionedMeshNode>>() {
        }.getType();
        Type meshModelList = new TypeToken<List<MeshModel>>() {
        }.getType();
        Type elementList = new TypeToken<List<Element>>() {
        }.getType();
        return new GsonBuilder().registerTypeAdapter(netKeyList, new NetKeyDeserializer())
                .registerTypeAdapter(appKeyList, new AppKeyDeserializer())
                .registerTypeAdapter(allocatedUnicastRange, new AllocatedUnicastRangeDeserializer())
                .registerTypeAdapter(allocatedGroupRange, new AllocatedGroupRangeDeserializer())
                .registerTypeAdapter(allocatedSceneRange, new AllocatedSceneRangeDeserializer())
                .registerTypeAdapter(nodeList, new NodeDeserializer())
                .registerTypeAdapter(elementList, new InternalElementListDeserializer())
                .registerTypeAdapter(meshModelList, new MeshModelListDeserializer())
                .registerTypeAdapter(MeshNetwork.class, new MeshNetworkDeserializer())
                .serializeNulls()
                .setPrettyPrinting()
                .create();
    }

    /**
     * Imports the network from the Mesh Provisioning/Configuration Database json file
     */
    protected MeshNetwork importNetwork(@NonNull final String networkJson) throws JsonSyntaxException {
        return mGson.fromJson(networkJson, MeshNetwork.class);
    }

    /**
     * Reads and returns the json string from URI.
     *
     * @param contentResolver ContentResolver
     * @param uri             URI
     * @throws IOException in case of failure
     */
    protected String readJsonStringFromUri(@NonNull final ContentResolver contentResolver, @NonNull final Uri uri) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        final InputStream inputStream = contentResolver.openInputStream(uri);
        if (inputStream != null) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(
                    inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
            inputStream.close();
        }
        return stringBuilder.toString();
    }

    /**
     * Exports the mesh network to a Json file
     *
     * @param network Mesh network to be exported
     * @param partial True if the network is to be exported as partial.
     */
    @Nullable
    protected String export(@NonNull final MeshNetwork network, final boolean partial) {
        try {
            network.setPartial(partial);
            return mGson.toJson(network);
        } catch (final com.google.gson.JsonSyntaxException ex) {
            Log.e(TAG, "Error: " + ex.getMessage());
            return null;
        } catch (final Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
            return null;
        }
    }

    @Nullable
    protected String export(@NonNull final MeshNetwork network,
                            @NonNull final NetworkKeysConfig networkKeysConfig,
                            @NonNull final ApplicationKeysConfig applicationKeysConfig,
                            @NonNull final NodesConfig nodesConfig,
                            @NonNull final ProvisionersConfig provisionersConfig,
                            @NonNull final GroupsConfig groupsConfig,
                            @NonNull final ScenesConfig scenesConfig) {
        //Let's use Gson to make a temporary copy of the network and apply the export configurations.
        final MeshNetwork temp = mGson.fromJson(mGson.toJson(network), MeshNetwork.class);
        return export(prepareNetwork(temp, networkKeysConfig, applicationKeysConfig, nodesConfig,
                provisionersConfig, groupsConfig, scenesConfig), true);
    }

    /**
     * Configures and returns a network with the export configuration provided.
     *
     * @param network               MeshNetwork.
     * @param networkKeysConfig     Network Keys configuration.
     * @param applicationKeysConfig Application Keys configuration.
     * @param nodesConfig           Nodes configuration.
     * @param provisionersConfig    Provisioners configuration.
     * @param groupsConfig          Groups configuration.
     * @param scenesConfig          Scenes configuration.
     */
    private MeshNetwork prepareNetwork(@NonNull final MeshNetwork network,
                                       @NonNull final NetworkKeysConfig networkKeysConfig,
                                       @NonNull final ApplicationKeysConfig applicationKeysConfig,
                                       @NonNull final NodesConfig nodesConfig,
                                       @NonNull final ProvisionersConfig provisionersConfig,
                                       @NonNull final GroupsConfig groupsConfig,
                                       @NonNull final ScenesConfig scenesConfig) {

        // Initial list of nodes to export
        if (nodesConfig.getConfig() instanceof NodesConfig.ExportWithoutDeviceKey) {
            for (ProvisionedMeshNode node : network.nodes)
                node.setDeviceKey(null);
        } else if (nodesConfig.getConfig() instanceof NodesConfig.ExportSome) {
            network.nodes.clear();
            final List<ProvisionedMeshNode> withDeviceKey = ((NodesConfig.ExportSome) nodesConfig.getConfig()).getWithDeviceKey();
            final List<ProvisionedMeshNode> withoutDeviceKey = ((NodesConfig.ExportSome) nodesConfig.getConfig()).getWithoutDeviceKey();
            for (ProvisionedMeshNode node : withoutDeviceKey)
                node.setDeviceKey(null);
            network.nodes.addAll(withDeviceKey);
            network.nodes.addAll(withoutDeviceKey);

            // Add any missing provisioner nodes if they were not selected when selecting nodes.
            for (Provisioner provisioner : network.provisioners) {
                if (!isProvisionerExistsInNodes(provisioner, network.nodes)) {
                    network.nodes.add(new ProvisionedMeshNode(provisioner, network.netKeys, network.appKeys));
                }
            }
        }

        // Include the selected provisioners
        // List of provisioners to export
        if (provisionersConfig.getConfig() instanceof ProvisionersConfig.ExportSome) {
            // First Let's exclude provisioners that are not nodes
            final ListIterator<Provisioner> provisionerListIterator = network.provisioners.listIterator();
            while (provisionerListIterator.hasNext()) {
                final Provisioner provisioner = provisionerListIterator.next();
                if (!isProvisionerExistsInNodes(provisioner, network.nodes)) {
                    provisionerListIterator.remove();
                }
            }

            // Now let's add the selected ones
            // We must go through all items to ensure there are no duplicates
            final List<Provisioner> selectedProvisioners = ((ProvisionersConfig.ExportSome) provisionersConfig.getConfig()).getProvisioners();
            for (Provisioner provisioner : selectedProvisioners) {
                if (!network.isProvisionerUuidInUse(provisioner.getProvisionerUuid())) {
                    network.provisioners.add(provisioner);
                }
            }
        }

        // List of Network Keys to export
        if (networkKeysConfig.getConfig() instanceof NetworkKeysConfig.ExportSome) {
            network.setNetKeys(((NetworkKeysConfig.ExportSome) networkKeysConfig.getConfig()).getKeys());
        }

        // List of Application Keys to export
        if (applicationKeysConfig.getConfig() instanceof ApplicationKeysConfig.ExportSome) {
            network.appKeys.clear();
            // List of keys set in the configuration, but we must only export the keys that are bound to that application key.
            final List<ApplicationKey> keys = ((ApplicationKeysConfig.ExportSome) applicationKeysConfig.getConfig()).getKeys();
            for (ApplicationKey key : keys) {
                if (isApplicationKeyBound(network.getNetKeys(), key)) {
                    network.appKeys.add(key);
                }
            }
        }

        // Exclude nodes unknown to network keys
        // TODO what will happen to the provisioner if the node is to excluded due to an unknown network key?
        final ListIterator<ProvisionedMeshNode> nodeListIterator = network.nodes.listIterator();
        while (nodeListIterator.hasNext()) {
            final ProvisionedMeshNode node = nodeListIterator.next();
            if (!isNetworkKeyAdded(node, network.getNetKeys())) {
                nodeListIterator.remove();
                continue;
            }
            excludeAppKeys(node, network.appKeys);
        }

        if (groupsConfig.getConfig() instanceof GroupsConfig.ExportRelated) {
            excludeNonRelatedGroups(network);
        } else if (groupsConfig.getConfig() instanceof GroupsConfig.ExportSome) {
            network.groups = ((GroupsConfig.ExportSome) groupsConfig.getConfig()).getGroups();
            // If subscriptions/publications uses any excluded group addresses, let's remove them.
            for (ProvisionedMeshNode node : network.getNodes()) {
                for (Element element : node.getElements().values()) {
                    for (MeshModel model : element.getMeshModels().values()) {
                        for (Group group : network.groups) {
                            if (model.getPublicationSettings() != null &&
                                    isValidGroupAddress(model.getPublicationSettings().getPublishAddress()) &&
                                    model.getPublicationSettings().getPublishAddress() != group.getAddress()) {
                                model.setPublicationSettings(null);
                            }
                            model.getSubscribedAddresses().remove((Integer) group.getAddress());
                        }
                    }
                }
            }
        }

        if (scenesConfig.getConfig() instanceof ScenesConfig.ExportSome) {
            network.scenes = ((ScenesConfig.ExportSome) scenesConfig.getConfig()).getScenes();
        }
        removeExcludedNodesFromScenes(network.nodes, network.scenes);
        return network;
    }

    /**
     * Check if the provisioner exists in the nodes list
     *
     * @param provisioner Provisioner
     * @param nodes       List of nodes
     * @return returns true if the provisioner exists in the selected list of nodes or false otherwise.
     */
    private boolean isProvisionerExistsInNodes(@NonNull final Provisioner provisioner, @NonNull final List<ProvisionedMeshNode> nodes) {
        if (provisioner.getProvisionerAddress() != null) {
            for (ProvisionedMeshNode node : nodes) {
                if (node.getUuid().equalsIgnoreCase(provisioner.getProvisionerUuid()))
                    return true;
            }
        }
        return false;
    }

    /**
     * Excludes groups that are not related tot he list of nodes.
     *
     * @param network Mesh network.
     */
    private void excludeNonRelatedGroups(@NonNull final MeshNetwork network) {
        final List<Group> groups = new ArrayList<>();
        for (Group group : network.getGroups()) {
            for (ProvisionedMeshNode node : network.getNodes()) {
                if (isGroupInUse(node, group)) {
                    groups.add(group);
                }
            }
        }
        network.groups = groups;
    }

    /**
     * Checks if the node has subscribed or publishes to the group.
     *
     * @param node  Node
     * @param group Group
     * @return true if is in use or false otherwise.
     */
    private boolean isGroupInUse(@NonNull final ProvisionedMeshNode node, @NonNull final Group group) {
        for (final Element element : node.getElements().values()) {
            for (final MeshModel model : element.getMeshModels().values()) {
                if (model.getPublicationSettings().getPublishAddress() == group.getAddress() ||
                        model.getSubscribedAddresses().contains((Integer) group.getAddress())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Removes excluded node from a scene addresses.
     *
     * @param nodes  List of nodes in the network.
     * @param scenes List of scenes in the network.
     */
    private void removeExcludedNodesFromScenes(@NonNull List<ProvisionedMeshNode> nodes, @NonNull List<Scene> scenes) {
        ListIterator<Integer> addresses;
        Integer address;
        for (Scene scene : scenes) {
            addresses = scene.getAddresses().listIterator();
            while (addresses.hasNext()) {
                address = addresses.next();
                if (!isNodeAddressExistsInScene(nodes, address)) {
                    addresses.remove();
                }
            }
        }
    }

    /**
     * Checks if a scene address exists exists in the list of nodes in the network.
     *
     * @param nodes   List of nodes in the network.
     * @param address Scene address.
     * @return true if node address is included in scene.
     */
    private boolean isNodeAddressExistsInScene(@NonNull final List<ProvisionedMeshNode> nodes, @NonNull final Integer address) {
        for (ProvisionedMeshNode node : nodes) {
            if (address == node.getUnicastAddress()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Exclude application keys that may be bound or used for publication by the models in the node that are not in the list.
     *
     * @param node            Mesh node.
     * @param applicationKeys Selected Application keys.
     */
    private void excludeAppKeys(@NonNull final ProvisionedMeshNode node, @NonNull final List<ApplicationKey> applicationKeys) {
        int index;
        for (Element element : node.getElements().values()) {
            for (MeshModel model : element.getMeshModels().values()) {
                final ListIterator<Integer> boundKeyIndexes = model.getBoundAppKeyIndexes().listIterator();
                while (boundKeyIndexes.hasNext()) {
                    index = boundKeyIndexes.next();
                    if (!isApplicationKeyBound(index, applicationKeys)) {
                        boundKeyIndexes.remove();
                        if (model.getPublicationSettings() != null && model.getPublicationSettings().getAppKeyIndex() == index) {
                            model.setPublicationSettings(null);
                        }
                    }

                }
            }
        }
    }

    /**
     * Checks if the given Application Key is bound to the network keys in the list
     *
     * @param networkKeys    List of Network keys.
     * @param applicationKey Application key.
     * @return true if bound and false otherwise
     */
    private boolean isApplicationKeyBound(@NonNull final List<NetworkKey> networkKeys, @NonNull final ApplicationKey applicationKey) {
        for (NetworkKey networkKey : networkKeys) {
            if (networkKey.keyIndex == applicationKey.keyIndex) return true;
        }
        return false;
    }

    private boolean isApplicationKeyBound(@NonNull final Integer index, @NonNull final List<ApplicationKey> keys) {
        for (ApplicationKey key : keys) {
            if (index == key.getKeyIndex())
                return true;
        }
        return false;
    }

    /**
     * Checks if at least one Network Key is added to a node.
     *
     * @param node        Mesh node.
     * @param networkKeys Network keys.
     * @return true if bound and false otherwise
     */
    private boolean isNetworkKeyAdded(@NonNull final ProvisionedMeshNode node, @NonNull final List<NetworkKey> networkKeys) {
        for (NetworkKey networkKey : networkKeys) {
            for (NodeKey nodeKey : node.getAddedNetKeys()) {
                if (nodeKey.getIndex() == networkKey.getKeyIndex()) return true;
            }
        }
        return false;
    }
}
