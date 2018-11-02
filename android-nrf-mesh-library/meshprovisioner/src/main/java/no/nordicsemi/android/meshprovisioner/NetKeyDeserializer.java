package no.nordicsemi.android.meshprovisioner;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

final class NetKeyDeserializer implements JsonSerializer<NetworkKey>, JsonDeserializer<NetworkKey> {
    private static final String TAG = NetKeyDeserializer.class.getSimpleName();

    @Override
    public JsonElement serialize(final NetworkKey src, final Type typeOfSrc, final JsonSerializationContext context) {
        return null;
    }

    @Override
    public NetworkKey deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        final String name = jsonObject.get("name").getAsString();
        final int index = jsonObject.get("index").getAsInt();
        final byte[] key = MeshParserUtils.toByteArray(jsonObject.get("key").getAsString());
        final byte[] oldKey = getOldKey(jsonObject);
        final int phase = jsonObject.get("phase").getAsInt();
        final boolean minSecurity = jsonObject.get("minSecurity").getAsString().equalsIgnoreCase("low") ? true : false;
        final long timestamp = Long.parseLong(jsonObject.get("timestamp").getAsString(), 16);

        final NetworkKey networkKey = new NetworkKey(index, key);
        networkKey.setName(name);
        networkKey.setPhase(phase);
        networkKey.setMinSecurity(minSecurity);
        networkKey.setOldKey(oldKey);
        networkKey.setTimestamp(timestamp);
        return networkKey;
    }

    private byte[] getOldKey(final JsonObject jsonObject){
        if(jsonObject.has("oldKey")){
            return MeshParserUtils.toByteArray(jsonObject.get("oldKey").getAsString());
        }

        return null;
    }
}
