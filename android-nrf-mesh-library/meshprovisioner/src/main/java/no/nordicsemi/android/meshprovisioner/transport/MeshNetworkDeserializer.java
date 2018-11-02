package no.nordicsemi.android.meshprovisioner.transport;

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
        network.netKeys = deserializeNetKeys(context, jsonObject);
        network.appKeys = deserializeAppKeys(context, jsonObject);
        return network;
    }

    @Override
    public JsonElement serialize(final MeshNetwork src, final Type typeOfSrc, final JsonSerializationContext context) {
        return null;
    }

    /**
     * Returns a list of network keys after deserializing the json array containing the network keys
     *
     * @param context deserializer context
     * @param json    json array containing the netkeys
     * @return List of network keys
     */
    private List<NetworkKey> deserializeNetKeys(final JsonDeserializationContext context, final JsonObject json) {
        final Type networkKey = new TypeToken<List<NetworkKey>>() {}.getType();
        return context.deserialize(json.getAsJsonArray("netKeys"), networkKey);
    }

    /**
     * Returns a list of application keys after deserializing the json array containing the application keys
     *
     * @param context deserializer context
     * @param json   json array containing the app keys
     * @return List of app keys
     */
    private List<ApplicationKey> deserializeAppKeys(final JsonDeserializationContext context, final JsonObject json) {
        final Type applicationKeys = new TypeToken<List<ApplicationKey>>() {}.getType();
        return context.deserialize(json.getAsJsonArray("appKeys"), applicationKeys);
    }
}
