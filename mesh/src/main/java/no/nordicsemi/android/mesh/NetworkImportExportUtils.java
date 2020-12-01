package no.nordicsemi.android.mesh;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
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

/**
 * Utility class to handle network imports and exports
 */
class NetworkImportExportUtils {

    private static final String TAG = NetworkImportExportUtils.class.getSimpleName();

    /**
     * Creates an AsyncTask to import the a mesh network
     *
     * @param context   context
     * @param uri       file path
     * @param callbacks internal callbacks to notify network import
     */
    static void importMeshNetwork(@NonNull final Context context,
                                  @NonNull final Uri uri,
                                  @NonNull final LoadNetworkCallbacks callbacks) throws JsonSyntaxException {
        new NetworkImportAsyncTask(context, uri, callbacks).execute();
    }

    /**
     * Creates an AsyncTask to import the a mesh network
     *
     * @param context     context
     * @param networkJson network json
     * @param callbacks   internal callbacks to notify network import
     */
    static void importMeshNetworkFromJson(@NonNull final Context context,
                                          @NonNull final String networkJson,
                                          @NonNull final LoadNetworkCallbacks callbacks) {
        new NetworkImportAsyncTask(context, networkJson, callbacks).execute();
    }

    /**
     * AsyncTask that reads and import a mesh network from the Mesh Provisioning/Configuration Database Json file
     */
    private static class NetworkImportAsyncTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<Context> context;
        private final Uri uri;
        private final LoadNetworkCallbacks callbacks;
        private MeshNetwork network;
        private String error;
        private final String networkJson;

        /**
         * Creates an AsyncTask to import the a m
         *
         * @param context   context
         * @param uri       file path
         * @param callbacks internal callbacks to notify network import
         */
        NetworkImportAsyncTask(@NonNull final Context context, @NonNull final Uri uri, @NonNull final LoadNetworkCallbacks callbacks) {
            this.context = new WeakReference<>(context);
            this.uri = uri;
            this.networkJson = null;
            this.callbacks = callbacks;
        }

        /**
         * Creates an AsyncTask to import the a m
         *
         * @param context     context
         * @param networkJson network json
         * @param callbacks   internal callbacks to notify network import
         */
        NetworkImportAsyncTask(@NonNull final Context context,
                               @NonNull final String networkJson,
                               @NonNull final LoadNetworkCallbacks callbacks) {
            this.context = new WeakReference<>(context);
            this.networkJson = networkJson;
            this.uri = null;
            this.callbacks = callbacks;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(final Void... voids) {
            try {
                importNetwork();
            } catch (Exception ex) {
                error = ex.getMessage() + "\n\nP.S. If the json file was exported using an older version 2.0.0 (Mesh Library), " +
                        "please retry exporting the json file and importing again.";
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void aVoid) {
            super.onPostExecute(aVoid);
            if (network != null) {
                callbacks.onNetworkImportedFromJson(network);
            } else {
                callbacks.onNetworkImportFailed(error);
            }
        }

        /**
         * Imports the network from the Mesh Provisioning/Configuration Database json file
         */
        private void importNetwork() throws JsonSyntaxException {
            try {

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

                final GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(MeshNetwork.class, new MeshNetworkDeserializer(false));
                gsonBuilder.registerTypeAdapter(netKeyList, new NetKeyDeserializer());
                gsonBuilder.registerTypeAdapter(appKeyList, new AppKeyDeserializer());
                gsonBuilder.registerTypeAdapter(allocatedUnicastRange, new AllocatedUnicastRangeDeserializer());
                gsonBuilder.registerTypeAdapter(allocatedGroupRange, new AllocatedGroupRangeDeserializer());
                gsonBuilder.registerTypeAdapter(allocatedSceneRange, new AllocatedSceneRangeDeserializer());
                gsonBuilder.registerTypeAdapter(nodeList, new NodeDeserializer());
                gsonBuilder.registerTypeAdapter(elementList, new InternalElementListDeserializer());
                gsonBuilder.registerTypeAdapter(meshModelList, new MeshModelListDeserializer());
                final Gson gson = gsonBuilder.serializeNulls().create();

                final String json = this.networkJson != null ? this.networkJson : readJsonStringFromUri();
                final MeshNetwork network = gson.fromJson(json, MeshNetwork.class);
                if (network != null) {
                    this.network = network;
                }
            } catch (final com.google.gson.JsonSyntaxException ex) {
                error = ex.getMessage();
                Log.e(TAG, " " + error);
            } catch (final IOException e) {
                error = e.getMessage();
                Log.e(TAG, " " + error);
            }
        }

        /**
         * Reads the complete json string from URI
         *
         * @return json string
         * @throws IOException in case of failure
         */
        private String readJsonStringFromUri() throws IOException {
            final StringBuilder stringBuilder = new StringBuilder();
            final InputStream inputStream = context.get().getContentResolver().openInputStream(uri);
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
    }


    /**
     * Exports the mesh network to a Json file
     *
     * @param network Mesh network to be exported
     * @param partial True if partial export
     */
    @Nullable
    static String export(@NonNull final MeshNetwork network, final boolean partial) {
        try {
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

            final GsonBuilder gsonBuilder = new GsonBuilder();

            gsonBuilder.registerTypeAdapter(netKeyList, new NetKeyDeserializer());
            gsonBuilder.registerTypeAdapter(appKeyList, new AppKeyDeserializer());
            gsonBuilder.registerTypeAdapter(allocatedUnicastRange, new AllocatedUnicastRangeDeserializer());
            gsonBuilder.registerTypeAdapter(allocatedGroupRange, new AllocatedGroupRangeDeserializer());
            gsonBuilder.registerTypeAdapter(allocatedSceneRange, new AllocatedSceneRangeDeserializer());
            gsonBuilder.registerTypeAdapter(nodeList, new NodeDeserializer());
            gsonBuilder.registerTypeAdapter(elementList, new InternalElementListDeserializer());
            gsonBuilder.registerTypeAdapter(meshModelList, new MeshModelListDeserializer());
            gsonBuilder.registerTypeAdapter(MeshNetwork.class, new MeshNetworkDeserializer(partial));

            final Gson gson = gsonBuilder
                    .serializeNulls()
                    .setPrettyPrinting()
                    .create();
            return gson.toJson(network);
        } catch (final com.google.gson.JsonSyntaxException ex) {
            Log.e(TAG, "Error: " + ex.getMessage());
            return null;
        } catch (final Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
            return null;
        }
    }

    @Nullable
    static String export(@NonNull final MeshNetwork network,
                         @NonNull final NetworkKeysConfig networkKeysConfig,
                         @NonNull final ApplicationKeysConfig applicationKeysConfig,
                         @NonNull final NodesConfig nodesConfig,
                         @NonNull final ProvisionersConfig provisionersConfig,
                         @NonNull final GroupsConfig groupsConfig,
                         @NonNull final ScenesConfig scenesConfig) {
        try {

            // List of Network Keys to export
            if (networkKeysConfig.getConfig() instanceof NetworkKeysConfig.ExportSome) {
                network.setNetKeys(((NetworkKeysConfig.ExportSome) networkKeysConfig.getConfig()).getKeys());
            }

            // List of Application Keys to export
            if (applicationKeysConfig.getConfig() instanceof ApplicationKeysConfig.ExportSome) {
                // List of keys set in the configuration, but we must only export the keys that are bound to that application key.
                final List<ApplicationKey> keys = ((ApplicationKeysConfig.ExportSome) applicationKeysConfig.getConfig()).getKeys();
                for (ApplicationKey key : keys) {
                    if (isApplicationKeyBound(network.getNetKeys(), key)) {
                        network.appKeys.add(key);
                    }
                }
            }

            // Initial list of nodes to export
            if (nodesConfig.getConfig() instanceof NodesConfig.ExportWithoutDeviceKey) {
                for (ProvisionedMeshNode node : network.nodes)
                    node.setDeviceKey(null);
            } else if (nodesConfig.getConfig() instanceof NodesConfig.ExportSome) {
                final List<ProvisionedMeshNode> withDeviceKey = ((NodesConfig.ExportSome) nodesConfig.getConfig()).getWithDeviceKey();
                final List<ProvisionedMeshNode> withoutDeviceKey = ((NodesConfig.ExportSome) nodesConfig.getConfig()).getWithoutDeviceKey();
                for (ProvisionedMeshNode node : withoutDeviceKey)
                    node.setDeviceKey(null);
                network.nodes.addAll(withDeviceKey);
                network.nodes.addAll(withoutDeviceKey);
            }

            //Exclude nodes unknown to network keys and Application keys
            final ListIterator<ProvisionedMeshNode> nodeListIterator = network.nodes.listIterator();
            while (nodeListIterator.hasNext()) {
                final ProvisionedMeshNode node = nodeListIterator.next();
                for (NetworkKey networkKey : network.getNetKeys()) {
                    if (!isNetworkKeyAdded(node, networkKey)) {
                        nodeListIterator.remove();
                    }
                }

                for (ApplicationKey applicationKey : network.getAppKeys()) {
                    if (!isApplicationKeyBound(node, applicationKey)) {
                        nodeListIterator.remove();
                    }
                }
            }


            if (provisionersConfig.getConfig() instanceof ProvisionersConfig.ExportSome) {
                network.provisioners = ((ProvisionersConfig.ExportSome) provisionersConfig.getConfig()).getProvisioners();
            }

            if (groupsConfig.getConfig() instanceof GroupsConfig.ExportRelated) {
                excludeNonRelatedGroups(network);
            } else if (groupsConfig.getConfig() instanceof GroupsConfig.ExportSome) {
                network.groups = ((GroupsConfig.ExportSome) groupsConfig.getConfig()).getGroups();
                for (ProvisionedMeshNode node : network.getNodes()) {
                    for (Element element : node.getElements().values()) {
                        for (MeshModel model : element.getMeshModels().values()) {
                            for (Group group : network.groups) {
                                if (model.getPublicationSettings().getPublishAddress() != group.getAddress()) {
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
            removeExcludedNodesFromScenes(network);

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

            final GsonBuilder gsonBuilder = new GsonBuilder();

            gsonBuilder.registerTypeAdapter(netKeyList, new NetKeyDeserializer());
            gsonBuilder.registerTypeAdapter(appKeyList, new AppKeyDeserializer());
            gsonBuilder.registerTypeAdapter(allocatedUnicastRange, new AllocatedUnicastRangeDeserializer());
            gsonBuilder.registerTypeAdapter(allocatedGroupRange, new AllocatedGroupRangeDeserializer());
            gsonBuilder.registerTypeAdapter(allocatedSceneRange, new AllocatedSceneRangeDeserializer());
            gsonBuilder.registerTypeAdapter(nodeList, new NodeDeserializer());
            gsonBuilder.registerTypeAdapter(elementList, new InternalElementListDeserializer());
            gsonBuilder.registerTypeAdapter(meshModelList, new MeshModelListDeserializer());
            gsonBuilder.registerTypeAdapter(MeshNetwork.class, new MeshNetworkDeserializer(true));

            final Gson gson = gsonBuilder
                    .serializeNulls()
                    .setPrettyPrinting()
                    .create();
            return gson.toJson(network);
        } catch (final com.google.gson.JsonSyntaxException ex) {
            Log.e(TAG, "Error: " + ex.getMessage());
            return null;
        } catch (final Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
            return null;
        }
    }

    final

    private static void excludeNonRelatedGroups(@NonNull final MeshNetwork network) {
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

    private static boolean isGroupInUse(@NonNull final ProvisionedMeshNode node, @NonNull final Group group) {
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

    private static void removeExcludedNodesFromScenes(@NonNull final MeshNetwork network) {
        for (Scene scene : network.getScenes()) {
            for (ProvisionedMeshNode node : network.getNodes()) {
                if (!isNodeAddressIncludedInScene(node, scene)) {
                    scene.getAddresses().remove((Integer) node.getUnicastAddress());
                }
            }
        }
    }

    private static boolean isNodeAddressIncludedInScene(@NonNull final ProvisionedMeshNode node, @NonNull final Scene scene) {
        for (int address : scene.getAddresses()) {
            return address == node.getUnicastAddress();
        }
        return false;
    }

    /**
     * Checks if the given Application Key is bound to the network keys in the list
     *
     * @param networkKeys    List of Network keys.
     * @param applicationKey Application key.
     * @return true if bound and false otherwise
     */
    private static boolean isApplicationKeyBound(@NonNull final List<NetworkKey> networkKeys, @NonNull final ApplicationKey applicationKey) {
        for (NetworkKey networkKey : networkKeys) {
            if (networkKey.keyIndex == applicationKey.keyIndex) return true;
        }
        return false;
    }

    /**
     * Checks if the given Application Key is bound to any of the models in the node.
     *
     * @param node           Mesh node.
     * @param applicationKey Application key.
     * @return true if bound and false otherwise
     */
    private static boolean isApplicationKeyBound(@NonNull final ProvisionedMeshNode node, @NonNull final ApplicationKey applicationKey) {
        for (Element element : node.getElements().values()) {
            for (MeshModel model : element.getMeshModels().values()) {
                if (model.getBoundAppKeyIndexes().contains(applicationKey.keyIndex) ||
                        model.getPublicationSettings().getAppKeyIndex() == applicationKey.keyIndex)
                    return true;
            }
        }
        return false;
    }

    /**
     * Checks if a Network Key is added to a node.
     *
     * @param node       Mesh node.
     * @param networkKey Network key.
     * @return true if bound and false otherwise
     */
    private static boolean isNetworkKeyAdded(@NonNull final ProvisionedMeshNode node, @NonNull final NetworkKey networkKey) {
        for (NodeKey nodeKey : node.getAddedNetKeys()) {
            if (nodeKey.getIndex() == networkKey.getKeyIndex()) return true;
        }
        return false;
    }

    private static boolean isNodeExist(@NonNull final List<ProvisionedMeshNode> nodes, @NonNull final ProvisionedMeshNode node) {
        for (ProvisionedMeshNode meshNode : nodes) {
            if (meshNode.getMeshUuid().equalsIgnoreCase(node.getMeshUuid())) return true;
        }
        return false;
    }
}
