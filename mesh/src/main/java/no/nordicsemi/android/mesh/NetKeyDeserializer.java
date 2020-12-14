package no.nordicsemi.android.mesh;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.mesh.utils.MeshParserUtils;

final class NetKeyDeserializer implements JsonSerializer<List<NetworkKey>>, JsonDeserializer<List<NetworkKey>> {
    private static final String TAG = NetKeyDeserializer.class.getSimpleName();

    @Override
    public JsonElement serialize(final List<NetworkKey> networkKeys, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonArray jsonArray = new JsonArray();
        for(NetworkKey networkKey :  networkKeys){
            final JsonObject networkKeyObject = new JsonObject();
            networkKeyObject.addProperty("name", networkKey.getName());
            networkKeyObject.addProperty("index", networkKey.getKeyIndex());
            networkKeyObject.addProperty("key", MeshParserUtils.bytesToHex(networkKey.getKey(), false));
            if(networkKey.getOldKey() != null) {
                networkKeyObject.addProperty("oldKey", MeshParserUtils.bytesToHex(networkKey.getOldKey(), false));
            }
            networkKeyObject.addProperty("phase", networkKey.getPhase());
            networkKeyObject.addProperty("minSecurity", networkKey.isMinSecurity() ? "secure" : "insecure");
            networkKeyObject.addProperty("timestamp", MeshParserUtils.formatTimeStamp(networkKey.getTimestamp()));
            jsonArray.add(networkKeyObject);
        }
        return jsonArray;
    }

    @Override
    public List<NetworkKey> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final List<NetworkKey> networkKeys = new ArrayList<>();
        final JsonArray jsonArray = json.getAsJsonArray();
        for(int i = 0; i < jsonArray.size(); i++) {
            final JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            final String name = jsonObject.get("name").getAsString();
            final int index = jsonObject.get("index").getAsInt();
            final byte[] key = MeshParserUtils.toByteArray(jsonObject.get("key").getAsString());
            final byte[] oldKey = getOldKey(jsonObject);
            final int phase = jsonObject.get("phase").getAsInt();
            final String minSec = jsonObject.get("minSecurity").getAsString();
            boolean minSecurity = true;
            if(!minSec.equalsIgnoreCase("low") && minSec.equalsIgnoreCase("insecure")){
                minSecurity = false;
            }

            final long timestamp;
            try {
                timestamp = 0;//MeshParserUtils.parseTimeStamp(jsonObject.get("timestamp").getAsString());
            } catch (Exception e) {
                throw new JsonSyntaxException("Invalid Mesh Provisioning/Configuration Database, mesh network timestamp must follow the Mesh Provisioning/Configuration Database format.");
            }

            final NetworkKey networkKey = new NetworkKey(index, key);
            networkKey.setName(name);
            networkKey.setPhase(phase);
            networkKey.setMinSecurity(minSecurity);
            networkKey.setOldKey(oldKey);
            networkKey.setTimestamp(timestamp);
            networkKeys.add(networkKey);
        }
        return networkKeys;
    }

    private byte[] getOldKey(final JsonObject jsonObject){
        if(jsonObject.has("oldKey")){
            return MeshParserUtils.toByteArray(jsonObject.get("oldKey").getAsString());
        }

        return null;
    }
}
