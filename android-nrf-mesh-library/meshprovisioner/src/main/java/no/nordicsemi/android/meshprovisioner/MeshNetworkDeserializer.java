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
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

public final class MeshNetworkDeserializer implements JsonSerializer<MeshNetwork>, JsonDeserializer<MeshNetwork> {
    private static final String TAG = MeshNetworkDeserializer.class.getSimpleName();

    @Override
    public MeshNetwork deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        final String meshUuid = jsonObject.get("meshUUID").getAsString();

        final MeshNetwork network = new MeshNetwork(meshUuid);

        network.schema = jsonObject.get("$schema").getAsString();
        network.id = jsonObject.get("$schema").getAsString();
        network.version = jsonObject.get("version").getAsString();
        network.timestamp = jsonObject.get("timestamp").getAsString();
        network.netKeys = deserializeNetKeys(context, jsonObject.getAsJsonArray("netKeys"), network.meshUUID);
        network.appKeys = deserializeAppKeys(context, jsonObject.getAsJsonArray("appKeys"), network.meshUUID);
        network.provisioners = deserializeProvisioners(context, jsonObject.getAsJsonArray("provisioners"), network.meshUUID);
        network.nodes = deserializeNodes(context, jsonObject.getAsJsonArray("nodes"), network.meshUUID);
        network.groups = deserializeGroups(jsonObject, network.meshUUID);
        network.scenes = deserializeScenes(jsonObject, network.meshUUID);
        return network;
    }

    @Override
    public JsonElement serialize(final MeshNetwork src, final Type typeOfSrc, final JsonSerializationContext context) {
        return null;
    }

    /**
     * Returns a list of network keys after deserializing the json array containing the network keys
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
     * Returns a list of application keys after deserializing the json array containing the application keys
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
                final byte[] address = MeshParserUtils.toByteArray(jsonGroup.get("address").getAsString());
                final byte[] parentAddress = MeshParserUtils.toByteArray(jsonGroup.get("parentAddress").getAsString());
                final Group group = new Group(address, parentAddress, meshUuid);
                group.setName(name);
                groups.add(group);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error while de-serializing groups: " + ex.getMessage());
        }
        return groups;
    }

    /**
     * Returns a list of scenes de-serializing the json array containing the scenes
     *
     * @param jsonNetwork     json array containing the scenes
     * @param meshUuid network provisionerUuid
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
}
