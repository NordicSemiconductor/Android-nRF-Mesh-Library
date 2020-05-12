package no.nordicsemi.android.mesh;

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

import no.nordicsemi.android.mesh.utils.MeshAddress;

final class AllocatedUnicastRangeDeserializer implements JsonSerializer<List<AllocatedUnicastRange>>, JsonDeserializer<List<AllocatedUnicastRange>> {
    private static final String TAG = AllocatedUnicastRangeDeserializer.class.getSimpleName();

    @Override
    public List<AllocatedUnicastRange> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final List<AllocatedUnicastRange> unicastRanges = new ArrayList<>();
        try {
            final JsonArray jsonObject = json.getAsJsonArray();
            for (int i = 0; i < jsonObject.size(); i++) {
                final JsonObject unicastRangeJson = jsonObject.get(i).getAsJsonObject();
                final int lowAddress = Integer.parseInt(unicastRangeJson.get("lowAddress").getAsString(), 16);
                final int highAddress = Integer.parseInt(unicastRangeJson.get("highAddress").getAsString(), 16);
                unicastRanges.add(new AllocatedUnicastRange(lowAddress, highAddress));
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error while de-serializing allocated unicast range: " + ex.getMessage());
        }
        return unicastRanges;
    }

    @Override
    public JsonElement serialize(final List<AllocatedUnicastRange> ranges, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonArray jsonArray = new JsonArray();
        for (AllocatedUnicastRange range : ranges) {
            final JsonObject rangeJson = new JsonObject();
            rangeJson.addProperty("lowAddress", MeshAddress.formatAddress(range.getLowAddress(), false));
            rangeJson.addProperty("highAddress", MeshAddress.formatAddress(range.getHighAddress(), false));
            jsonArray.add(rangeJson);
        }
        return jsonArray;
    }
}
