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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

final class AllocatedSceneRangeDeserializer implements JsonSerializer<List<AllocatedSceneRange>>, JsonDeserializer<List<AllocatedSceneRange>> {
    private static final String TAG = AllocatedSceneRangeDeserializer.class.getSimpleName();

    @Override
    public List<AllocatedSceneRange> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final List<AllocatedSceneRange> sceneRanges = new ArrayList<>();
        try {
            final JsonArray jsonObject = json.getAsJsonArray();
            for (int i = 0; i < jsonObject.size(); i++) {
                final JsonObject unicastRangeJson = jsonObject.get(i).getAsJsonObject();
                final int firstScene = unicastRangeJson.get("firstScene").getAsInt();
                final int lastScene = unicastRangeJson.get("lastScene").getAsInt();
                sceneRanges.add(new AllocatedSceneRange(firstScene, lastScene));
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error while de-serializing allocated scene range: " + ex.getMessage());
        }
        return sceneRanges;
    }

    @Override
    public JsonElement serialize(final List<AllocatedSceneRange> ranges, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonArray jsonArray = new JsonArray();
        for(AllocatedSceneRange range :  ranges){
            final JsonObject rangeJson = new JsonObject();
            rangeJson.addProperty("firstScene", range.getFirstScene());
            rangeJson.addProperty("lastScene", range.getLastScene());
            jsonArray.add(rangeJson);
        }
        return jsonArray;
    }
}
