package no.nordicsemi.android.meshprovisioner;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

public final class MeshNetworkDeserializer implements JsonSerializer<MeshNetwork>, JsonDeserializer<MeshNetwork> {
    private static final String TAG = MeshNetworkDeserializer.class.getSimpleName();

    @Override
    public MeshNetwork deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        if (!isValidMeshObject(jsonObject))
            throw new JsonSyntaxException("Invalid Mesh Provisioning/Configuration Database JSON file, mesh network object must follow the Mesh Provisioning/Configuration Database format.");

        final String meshUuid = jsonObject.get("meshUUID").getAsString();

        final MeshNetwork network = new MeshNetwork(meshUuid);

        network.schema = jsonObject.get("$schema").getAsString();
        network.id = jsonObject.get("id").getAsString();
        network.version = jsonObject.get("version").getAsString();
        network.meshName = jsonObject.get("meshName").getAsString();
        network.timestamp = Long.parseLong(jsonObject.get("timestamp").getAsString(), 16);
        network.netKeys = deserializeNetKeys(context, jsonObject.getAsJsonArray("netKeys"), network.meshUUID);
        network.appKeys = deserializeAppKeys(context, jsonObject.getAsJsonArray("appKeys"), network.meshUUID);
        network.provisioners = deserializeProvisioners(context, jsonObject.getAsJsonArray("provisioners"), network.meshUUID);
        if (jsonObject.has("nodes"))
            network.nodes = deserializeNodes(context, jsonObject.getAsJsonArray("nodes"), network.meshUUID);

        if (jsonObject.has("groups"))
            network.groups = deserializeGroups(jsonObject, network.meshUUID);
        if (jsonObject.has("scenes"))
            network.scenes = deserializeScenes(jsonObject, network.meshUUID);

        network.unicastAddress = getNextAvailableAddress(network.nodes);
        populateNetworkKeys(network.getNodes(), network.getNetKeys());
        populateAddedAppKeysInNodes(network.getNodes(), network.getAppKeys());
        populateBoundAppKeysInNodes(network.getNodes(), network.getAppKeys());
        return network;
    }

    @Override
    public JsonElement serialize(final MeshNetwork network, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("$schema", network.getSchema());
        jsonObject.addProperty("id", network.getId());
        jsonObject.addProperty("version", network.getVersion());
        jsonObject.addProperty("meshUUID", network.getMeshUUID());
        jsonObject.addProperty("meshName", network.getMeshName());
        jsonObject.addProperty("timestamp", Long.toString(network.getTimestamp(), 16));
        jsonObject.add("provisioners", serializeProvisioners(context, network.getProvisioners()));
        jsonObject.add("netKeys", serializeNetKeys(context, network.getNetKeys()));
        jsonObject.add("appKeys", serializeAppKeys(context, network.getAppKeys()));

        //Optional properties
        if (!network.getNodes().isEmpty()) {
            jsonObject.add("nodes", serializeNodes(context, network.getNodes()));
        }

        //Optional properties
        if (!network.getGroups().isEmpty())
            jsonObject.add("groups", serializeGroups(context, network.getGroups()));

        //Optional properties
        if (!network.getScenes().isEmpty())
            jsonObject.add("scenes", serializeScenes(context, network.getScenes()));

        return jsonObject;
    }

    /**
     * Validates the mesh object by checking if the document contains the mandatory fields
     *
     * @param mesh json
     * @return true if valid and false otherwise
     */
    private boolean isValidMeshObject(final JsonObject mesh) {
        return mesh.has("meshUUID") &&
                mesh.has("meshName") &&
                mesh.has("timestamp") &&
                mesh.has("provisioners") &&
                mesh.has("netKeys");
    }

    /**
     * Returns a JsonElement of application keys keys after serializing the network keys
     *
     * @param context     Serializer context
     * @param networkKeys Network key list
     * @return JsonElement
     */
    private JsonElement serializeNetKeys(final JsonSerializationContext context, final List<NetworkKey> networkKeys) {
        final Type networkKey = new TypeToken<List<NetworkKey>>() {
        }.getType();
        return context.serialize(networkKeys, networkKey);
    }

    /**
     * Returns a JsonElement of network keys after de-serializing the json array containing the network keys
     *
     * @param context  deserializer context
     * @param json     json array containing the netkeys
     * @param meshUuid network provisionerUuid
     * @return List of network keys
     */
    private List<NetworkKey> deserializeNetKeys(final JsonDeserializationContext context, final JsonArray json, final String meshUuid) {
        final Type networkKey = new TypeToken<List<NetworkKey>>() {
        }.getType();
        final List<NetworkKey> networkKeys = context.deserialize(json, networkKey);
        for (NetworkKey key : networkKeys) {
            key.setMeshUuid(meshUuid);
        }
        return networkKeys;
    }

    /**
     * Returns a list of network keys after serializing application keys list
     *
     * @param context         Serializer context
     * @param applicationKeys Application key list
     * @return JsonElement
     */
    private JsonElement serializeAppKeys(final JsonSerializationContext context, final List<ApplicationKey> applicationKeys) {
        final Type networkKey = new TypeToken<List<ApplicationKey>>() {
        }.getType();
        return context.serialize(applicationKeys, networkKey);
    }

    /**
     * Returns a list of application keys after de-serializing the json array containing the application keys
     *
     * @param context  deserializer context
     * @param json     json array containing the app keys
     * @param meshUuid network provisionerUuid
     * @return List of app keys
     */
    private List<ApplicationKey> deserializeAppKeys(final JsonDeserializationContext context, final JsonArray json, final String meshUuid) {
        final Type applicationKeyList = new TypeToken<List<ApplicationKey>>() {
        }.getType();
        final List<ApplicationKey> applicationKeys = context.deserialize(json, applicationKeyList);
        for (ApplicationKey key : applicationKeys) {
            key.setMeshUuid(meshUuid);
        }
        return applicationKeys;
    }

    /**
     * Returns serialized json element containing the provisioners
     *
     * @param context      Serializer context
     * @param provisioners Provisioners list
     * @return JsonElement
     */
    private JsonElement serializeProvisioners(final JsonSerializationContext context, final List<Provisioner> provisioners) {
        final Type networkKey = new TypeToken<List<Provisioner>>() {
        }.getType();
        return context.serialize(provisioners, networkKey);
    }

    /**
     * Returns a list of nodes de-serializing the json array containing the provisioners
     *
     * @param context  deserializer context
     * @param json     json array object containing the provisioners
     * @param meshUuid network provisionerUuid
     * @return List of nodes
     */
    private List<Provisioner> deserializeProvisioners(final JsonDeserializationContext context, final JsonArray json, final String meshUuid) {
        final Type provisionerList = new TypeToken<List<Provisioner>>() {
        }.getType();
        final List<Provisioner> provisioners = context.deserialize(json, provisionerList);
        for (Provisioner provisioner : provisioners) {
            provisioner.setMeshUuid(meshUuid);
        }
        return provisioners;
    }

    /**
     * Returns serialized json element containing the nodes
     *
     * @param context Serializer context
     * @param nodes   Nodes list
     * @return JsonElement
     */
    private JsonElement serializeNodes(final JsonSerializationContext context, final List<ProvisionedMeshNode> nodes) {
        final Type nodeList = new TypeToken<List<ProvisionedMeshNode>>() {
        }.getType();
        return context.serialize(nodes, nodeList);
    }

    /**
     * Returns a list of nodes deserializing the json array containing the provisioned mesh nodes
     *
     * @param context  deserializer context
     * @param json     json array containing the nodes
     * @param meshUuid network provisionerUuid
     * @return List of nodes
     */
    private List<ProvisionedMeshNode> deserializeNodes(final JsonDeserializationContext context, final JsonArray json, final String meshUuid) {
        final Type nodeList = new TypeToken<List<ProvisionedMeshNode>>() {
        }.getType();
        final List<ProvisionedMeshNode> nodes = context.deserialize(json, nodeList);
        for (ProvisionedMeshNode node : nodes) {
            node.setMeshUuid(meshUuid);
        }
        return nodes;
    }

    /**
     * Returns serialized json element containing the groups
     *
     * @param context Serializer context
     * @param groups  Group list
     * @return JsonElement
     */
    private JsonElement serializeGroups(final JsonSerializationContext context, final List<Group> groups) {
        final Type group = new TypeToken<List<Group>>() {
        }.getType();
        return context.serialize(groups, group);
    }

    /**
     * Returns a list of groups de-serializing the json array containing the groups
     *
     * @param jsonNetwork json network object containing the groups
     * @param meshUuid    network provisionerUuid
     * @return List of nodes
     */
    private List<Group> deserializeGroups(final JsonObject jsonNetwork, final String meshUuid) {
        final List<Group> groups = new ArrayList<>();
        try {
            if (!jsonNetwork.has("groups"))
                return groups;

            final JsonArray jsonGroups = jsonNetwork.getAsJsonArray("groups");
            for (int i = 0; i < jsonGroups.size(); i++) {
                final JsonObject jsonGroup = jsonGroups.get(i).getAsJsonObject();
                final String name = jsonGroup.get("name").getAsString();
                final int address = jsonGroup.get("address").getAsInt();
                final int parentAddress = jsonGroup.get("parentAddress").getAsInt();
                final Group group = new Group(address, meshUuid);
                group.setParentAddress(parentAddress);
                group.setName(name);
                groups.add(group);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error while de-serializing groups: " + ex.getMessage());
        }
        return groups;
    }

    /**
     * Returns serialized json element containing the scenes
     *
     * @param context Serializer context
     * @param scenes  Group list
     * @return JsonElement
     */
    private JsonElement serializeScenes(final JsonSerializationContext context, final List<Scene> scenes) {
        final Type scene = new TypeToken<List<Scene>>() {
        }.getType();
        return context.serialize(scenes, scene);
    }

    /**
     * Returns a list of scenes de-serializing the json array containing the scenes
     *
     * @param jsonNetwork json array containing the scenes
     * @param meshUuid    network provisionerUuid
     * @return List of nodes
     */
    private List<Scene> deserializeScenes(final JsonObject jsonNetwork, final String meshUuid) {
        final List<Scene> scenes = new ArrayList<>();
        try {
            if (!jsonNetwork.has("scenes"))
                return scenes;

            final JsonArray jsonScenes = jsonNetwork.getAsJsonArray("scenes");
            for (int i = 0; i < jsonScenes.size(); i++) {
                final JsonObject jsonScene = jsonScenes.get(i).getAsJsonObject();
                final String name = jsonScene.get("name").getAsString();
                final List<byte[]> addresses = new ArrayList<>();
                if (jsonScene.has("addresses")) {
                    final JsonArray addressesArray = jsonScene.get("addresses").getAsJsonArray();
                    for (int j = 0; j < addressesArray.size(); j++) {
                        addresses.add(MeshParserUtils.toByteArray(addressesArray.get(j).getAsString()));
                    }
                }
                final int number = jsonScene.get("number").getAsInt();
                final Scene scene = new Scene(number, addresses, meshUuid);
                scene.setName(name);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error while de-serializing scenes: " + ex.getMessage());
        }
        return scenes;
    }

    /**
     * Returns the next available address based on the nodes/elements in the network
     *
     * @param nodes provisioned nodes
     */
    private int getNextAvailableAddress(final List<ProvisionedMeshNode> nodes) {
        //We set the next available unicast address here, this is a library attribute
        int unicast = 1;
        if (nodes != null && !nodes.isEmpty()) {
            final int index = nodes.size() - 1;
            final ProvisionedMeshNode node = nodes.get(index);
            Map<Integer, Element> elements = node.getElements();
            if (elements != null && !elements.isEmpty()) {
                unicast = node.getUnicastAddress() + elements.size();
            } else {
                unicast = node.getUnicastAddress() + 1;
            }
        }
        return unicast;
    }

    /**
     * Populates the added net keys for nodes
     *
     * @param nodes       list of nodes
     * @param networkKeys list of keys
     */
    private void populateNetworkKeys(final List<ProvisionedMeshNode> nodes, final List<NetworkKey> networkKeys) {
        for (ProvisionedMeshNode node : nodes) {
            for (NetworkKey networkKey : networkKeys) {
                node.getAddedNetworkKeys().add(networkKey);
            }
        }
    }

    /**
     * Populates the added app keys for nodes
     *
     * @param nodes           list of nodes
     * @param applicationKeys list of keys
     */
    private void populateAddedAppKeysInNodes(final List<ProvisionedMeshNode> nodes, final List<ApplicationKey> applicationKeys) {
        for (ProvisionedMeshNode node : nodes) {
            final Map<Integer, ApplicationKey> applicationKeyMap = new LinkedHashMap<>();
            for (Integer index : node.getAddedAppKeyIndexes()) {
                if (!applicationKeys.isEmpty()) {
                    final ApplicationKey applicationKey = applicationKeys.get(index);
                    applicationKeyMap.put(applicationKey.getKeyIndex(), applicationKey);
                }
            }
            node.setAddedApplicationKeys(applicationKeyMap);
        }
    }

    /**
     * Populates the bound app keys in the nodes
     *
     * @param nodes           list of nodes
     * @param applicationKeys list of keys
     */
    private void populateBoundAppKeysInNodes(final List<ProvisionedMeshNode> nodes, final List<ApplicationKey> applicationKeys) {
        for (ProvisionedMeshNode node : nodes) {
            for (Map.Entry<Integer, Element> elementEntry : node.getElements().entrySet()) {
                final Element element = elementEntry.getValue();
                for (Map.Entry<Integer, MeshModel> modelEntry : element.getMeshModels().entrySet()) {
                    final MeshModel model = modelEntry.getValue();
                    for (Integer index : model.getBoundAppKeyIndexes()) {
                        model.getBoundApplicationKeys().put(index, applicationKeys.get(index));
                    }
                }
            }
        }
    }
}
