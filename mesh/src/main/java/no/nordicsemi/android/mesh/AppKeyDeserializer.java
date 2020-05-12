package no.nordicsemi.android.mesh;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.mesh.utils.MeshParserUtils;

final class AppKeyDeserializer implements JsonSerializer<List<ApplicationKey>>, JsonDeserializer<List<ApplicationKey>> {
    private static final String TAG = AppKeyDeserializer.class.getSimpleName();

    @Override
    public JsonElement serialize(final List<ApplicationKey> applicationKeys, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonArray jsonArray = new JsonArray();
        for(ApplicationKey applicationKey :  applicationKeys){
            final JsonObject appKeyObject = new JsonObject();
            appKeyObject.addProperty("name", applicationKey.getName());
            appKeyObject.addProperty("index", applicationKey.getKeyIndex());
            appKeyObject.addProperty("boundNetKey", applicationKey.getBoundNetKeyIndex());
            appKeyObject.addProperty("key", MeshParserUtils.bytesToHex(applicationKey.getKey(), false));
            if(applicationKey.getOldKey() != null) {
                appKeyObject.addProperty("oldKey", MeshParserUtils.bytesToHex(applicationKey.getOldKey(), false));
            }
            jsonArray.add(appKeyObject);
        }
        return jsonArray;
    }

    @Override
    public List<ApplicationKey> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final List<ApplicationKey> appKeys = new ArrayList<>();
        final JsonArray jsonArray = json.getAsJsonArray();
        for(int i = 0; i < jsonArray.size(); i++) {
            final JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            final String name = jsonObject.get("name").getAsString();
            final int index = jsonObject.get("index").getAsInt();
            final int boundNetKeyIndex = jsonObject.get("boundNetKey").getAsInt();
            final byte[] key = MeshParserUtils.toByteArray(jsonObject.get("key").getAsString());
            final byte[] oldKey = getOldKey(jsonObject);

            final ApplicationKey applicationKey = new ApplicationKey(index, key);
            applicationKey.setName(name);
            applicationKey.setBoundNetKeyIndex(boundNetKeyIndex);
            applicationKey.setOldKey(oldKey);
            appKeys.add(applicationKey);
        }
        return appKeys;
    }

    private byte[] getOldKey(final JsonObject jsonObject){
        if(jsonObject.has("oldKey")){
            return MeshParserUtils.toByteArray(jsonObject.get("oldKey").getAsString());
        }

        return null;
    }
}
