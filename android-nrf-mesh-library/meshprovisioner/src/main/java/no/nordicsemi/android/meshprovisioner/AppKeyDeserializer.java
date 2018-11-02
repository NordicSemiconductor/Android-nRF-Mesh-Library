package no.nordicsemi.android.meshprovisioner;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

final class AppKeyDeserializer implements JsonSerializer<ApplicationKey>, JsonDeserializer<ApplicationKey> {
    private static final String TAG = AppKeyDeserializer.class.getSimpleName();

    @Override
    public JsonElement serialize(final ApplicationKey src, final Type typeOfSrc, final JsonSerializationContext context) {
        return null;
    }

    @Override
    public ApplicationKey deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        final String name = jsonObject.get("name").getAsString();
        final int index = jsonObject.get("index").getAsInt();
        final int boundNetKeyIndex = jsonObject.get("boundNetKey").getAsInt();
        final byte[] key = MeshParserUtils.toByteArray(jsonObject.get("key").getAsString());
        final byte[] oldKey = getOldKey(jsonObject);

        final ApplicationKey applicationKey = new ApplicationKey(index, key);
        applicationKey.setName(name);
        applicationKey.setBoundNetKeyIndex(boundNetKeyIndex);
        applicationKey.setOldKey(oldKey);
        return applicationKey;
    }

    private byte[] getOldKey(final JsonObject jsonObject){
        if(jsonObject.has("oldKey")){
            return MeshParserUtils.toByteArray(jsonObject.get("oldKey").getAsString());
        }

        return null;
    }
}
