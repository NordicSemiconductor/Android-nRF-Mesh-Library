package no.nordicsemi.android.meshprovisioner;

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
import java.util.List;

import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;

public final class MeshNetworkDeserializer implements JsonSerializer<MeshNetwork>, JsonDeserializer<MeshNetwork> {
    @Override
    public MeshNetwork deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final MeshNetwork network = new MeshNetwork();

        final JsonObject jsonObject = json.getAsJsonObject();
        network.schema = jsonObject.get("$schema").getAsString();
        network.id = jsonObject.get("$schema").getAsString();
        network.version = jsonObject.get("version").getAsString();
        network.meshUUID = jsonObject.get("meshUUID").getAsString();
        network.timestamp = jsonObject.get("timestamp").getAsString();
        network.netKeys = deserializeNetKeys(context, jsonObject.getAsJsonArray("netKeys"), network.meshUUID);
        network.appKeys = deserializeAppKeys(context, jsonObject.getAsJsonArray("appKeys"), network.meshUUID);
        network.nodes = deserializeNodes(context, jsonObject.getAsJsonArray("nodes"), network.meshUUID);
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
            key.setUuid(meshUuid);
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
            key.setUuid(meshUuid);
        }
        return applicationKeys;
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
}
