package no.nordicsemi.android.meshprovisioner;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

final class AllocatedSceneRangeDeserializer implements JsonSerializer<AllocatedSceneRange>, JsonDeserializer<AllocatedSceneRange> {
    private static final String TAG = AllocatedSceneRangeDeserializer.class.getSimpleName();

    @Override
    public JsonElement serialize(final AllocatedSceneRange src, final Type typeOfSrc, final JsonSerializationContext context) {
        return null;
    }

    @Override
    public AllocatedSceneRange deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        final int firstScene = jsonObject.get("firstScene").getAsInt();
        final int lastScene = jsonObject.get("lastScene").getAsInt();
        return new AllocatedSceneRange(firstScene, lastScene);
    }
}
